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

package connectors

import java.time.OffsetDateTime

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import models.{Error, ReportStatus}
import play.api.{Configuration, Environment, Logger}
import play.api.Mode.Mode
import play.api.http.Status
import play.api.libs.json.JsValue
import reactivemongo.bson.BSONDocument
import repositories.ReportStatusRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DefaultReportStatusConnector @Inject()(http: HttpClient,
                                      val configuration: Configuration,
                                      reportStatusRepository: ReportStatusRepository,
                                      environment: Environment)
                                     (implicit ec: ExecutionContext)
  extends ServicesConfig with ReportStatusConnector {

  override protected def mode: Mode = environment.mode
  override protected def runModeConfiguration: Configuration = configuration

  val serviceUrl = baseUrl("voa-bar")
  val baseSegment = "/voa-bar/"

  def request(authorisedUsername: String)(implicit hc: HeaderCarrier): Future[Try[JsValue]] = {
    http.GET(s"$serviceUrl${baseSegment}reports/${authorisedUsername}")
      .map {
        response =>
          response.status match {
            case Status.OK => Success(response.json)
            case status => {
              Logger.warn("Received status of " + status + " from upstream service when requesting report status")
              Failure(new RuntimeException("Received status of " + status + " from upstream service when requesting report status"))
            }
          }
      } recover {
      case e =>
        Logger.warn("Received exception " + e.getMessage + " from upstream service when requesting report status")
        Failure(new RuntimeException("Received exception " + e.getMessage + " from upstream service when requesting report status"))
    }
  }

  import ReportStatus._
  def save(reportStatus: ReportStatus): Future[Either[Error, Unit.type]] = {
    reportStatusRepository().atomicUpsert(
      BSONDocument("_id"  -> reportStatus._id),
      BSONDocument("$set" -> BSONDocument(
        "date" -> reportStatus.date.toString,
        "checksum" -> reportStatus.checksum,
        "url" -> reportStatus.url,
        "errors" -> reportStatus.errors.map(e => BSONDocument(
          "detail" -> e.detail,
          "message" -> e.errorCode,
          "error_code" -> e.errorCode
        )),
        "status" -> reportStatus.status)
      ),
      ReportStatus.key
    ).map(_ => Right(Unit))
      .recover {
        case ex: Throwable => {
          Logger.warn(s"Received exception while saving reportStatus.\n${ex.getMessage}")
          Left(Error(ex.getMessage, Seq()))
        }
      }
  }
  def saveUserInfo(reference: String, userId: String): Future[Either[Error, Unit.type]] = {
    reportStatusRepository().atomicUpsert(
      BSONDocument("_id"  -> reference),
      BSONDocument("$set" -> BSONDocument(
        "user_id" -> userId,
        "date" -> OffsetDateTime.now.toString,
        "errors" -> Seq[BSONDocument]()
      )),
      ReportStatus.key
    ).map(_ =>Right(Unit))
      .recover {
        case ex: Throwable => {
          Logger.warn(s"Received exception while saving reportStatus user info.\n${ex.getMessage}")
          Left(Error(ex.getMessage, Seq()))
        }
      }
  }
}

@ImplementedBy(classOf[DefaultReportStatusConnector])
trait ReportStatusConnector {
  def saveUserInfo(reference: String, userId: String): Future[Either[Error, Unit.type]]
  def save(reportStatus: ReportStatus): Future[Either[Error, Unit.type]]
  def request(authorisedUsername: String)(implicit hc: HeaderCarrier): Future[Try[JsValue]]
}
