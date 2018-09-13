/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package repositories

import java.time.OffsetDateTime

import com.typesafe.config.ConfigException
import javax.inject.{Inject, Singleton}
import models.{Error, ReportStatus}
import play.api.libs.json.{Format, Json}
import play.api.{Configuration, Logger}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import uk.gov.hmrc.mongo.{BSONBuilderHelpers, ReactiveRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReportStatusRepository @Inject()
(
  mongo: ReactiveMongoComponent,
  config: Configuration
)(implicit ec: ExecutionContext)
  extends
    ReactiveRepository[ReportStatus, String](
      collectionName = ReportStatus.name,
      mongo = mongo.mongoConnector.db,
      domainFormat = ReportStatus.format,
      idFormat = implicitly[Format[String]]
    )
    with BSONBuilderHelpers
{
  private val indexName = ReportStatus.name
  private val expireAfterSeconds = "expireAfterSeconds"
  private val ttlPath = s"${ReportStatus.name}.timeToLiveInSeconds"
  private val ttl = config.getInt(ttlPath)
    .getOrElse(throw new ConfigException.Missing(ttlPath))
  createIndex()
  private def createIndex(): Unit = {
    collection.indexesManager.ensure(Index(Seq(
        (ReportStatus.key, IndexType.Text),
        ("userId", IndexType.Text)
      ), Some(indexName),
      options = BSONDocument(expireAfterSeconds -> ttl),
      background = true)) map {
      result => {
        Logger.debug(s"set [$indexName] with value $ttl -> result : $result")
        result
      }
    } recover {
      case e => Logger.error("Failed to set TTL index", e)
        false
    }
  }

  def atomicSaveOrUpdate(reportStatus: ReportStatus, upsert: Boolean)
    (implicit ec: ExecutionContext)
  : Future[Either[Error, Unit.type]] = {
    val finder = BSONDocument(ReportStatus.key -> reportStatus._id)
    val modifierBson = set(BSONDocument(
      "date" -> reportStatus.date.toString,
      "checksum" -> reportStatus.checksum,
      "url" -> reportStatus.url,
      "errors" -> reportStatus.errors.getOrElse(Seq()).map(e => BSONDocument(
        "detail" -> e.detail,
        "message" -> e.message,
        "errorCode" -> e.errorCode
      )),
      "filename" -> reportStatus.filename.getOrElse(""),
      "status" -> reportStatus.status)
    )

    atomicSaveOrUpdate(reportStatus._id, upsert, finder, modifierBson)
  }

  def atomicSaveOrUpdate(userId: String, reference: String, upsert: Boolean)
                        (implicit ec: ExecutionContext)
  : Future[Either[Error, Unit.type]] = {
    val finder = BSONDocument(ReportStatus.key -> reference)
    val modifierBson = set(BSONDocument(
      "date" -> OffsetDateTime.now.toString,
      "userId" -> userId)
    )

    atomicSaveOrUpdate(reference, upsert, finder, modifierBson)
  }

  def getByUser(userId: String)(implicit ec: ExecutionContext)
  : Future[Either[Error, Seq[ReportStatus]]] = {
    val finder = BSONDocument("userId" -> userId)
    collection.find(finder).sort(Json.obj("date" -> -1)).cursor[ReportStatus](ReadPreference.primary)
      .collect[Seq](-1, Cursor.FailOnError[Seq[ReportStatus]]())
      .map(Right(_))
      .recover {
        case ex: Throwable => {
          val errorMsg = "Couldn't retrieve BA reports"
          Logger.warn(s"$errorMsg\n${ex.getMessage}")
          Left(Error(errorMsg, Seq()))
        }
      }
  }
  protected def atomicSaveOrUpdate(reference: String, upsert: Boolean, finder: BSONDocument, modifierBson: BSONDocument) = {
    val updateDocument = if (upsert) {
      modifierBson ++ setOnInsert(BSONDocument(ReportStatus.key -> reference))
    } else {
      modifierBson
    }
    val modifier = collection.updateModifier(updateDocument, upsert = upsert)
    collection.findAndModify(finder, modifier)
      .map(response => Either.cond(
        !response.lastError.isDefined || !response.lastError.get.err.isDefined,
        Unit,
        getError(response.lastError.get.err.get))
      )
      .recover {
        case ex: Throwable => Left(Error(ex.getMessage, Seq()))
      }
  }

  private def getError(error: String): Error = {
    val errorMsg = "Error while saving report status"
    Logger.warn(s"$errorMsg\n$error")
    Error(error, Seq())
  }
}
