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

package controllers

import connectors.{DataCacheConnector, FakeDataCacheConnector, ReportStatusConnector}
import controllers.actions._
import identifiers.VOAAuthorisedId
import models.NormalMode
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.libs.json.{Format, JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import views.html.reportStatus

import scala.concurrent.Future

class ReportStatusControllerSpec extends ControllerSpecBase with MockitoSugar {

  implicit val hc = mock[HeaderCarrier]
  val configuration = injector.instanceOf[Configuration]
  val environment = injector.instanceOf[Environment]
  val username = "AUser"

  def getHttpMock(returnedStatus: Int, returnedJson: Option[JsValue]) = {
    val httpMock = mock[HttpClient]
    when(httpMock.GET(anyString)(any[HttpReads[Any]], any[HeaderCarrier], any())) thenReturn Future.successful(HttpResponse(returnedStatus, returnedJson))
    httpMock
  }

  val jsonStr =
    """[{"Reference": "A34DF1", "Type": "CT", "FileName": "FILE1.XML", "TotalReports": 10, "FailedReports": 0, "Errors": [], "SubmissionDate": "1-Mar-2018 09:15:26"},
        {"Reference": "B23SD1", "Type": "CT", "FileName": "FILE2.XML", "TotalReports": 16, "FailedReports": 2, "Errors": [{"Code": 1001, "Details": ["32182", "Postcode", "NW111NW"]}, {"Code": 1002, "Details": ["32183", "DateSent", "28-02-2018"]}], "SubmissionDate": "28-Feb-2018 14:28:36"},
        {"Reference": "DFG123", "Type": "CT", "FileName": "FILE3.XML", "TotalReports": 20, "FailedReports": 2, "Errors": [{"Code": 1000, "Details": ["111", "Town", ""]}, {"Code": 1003, "Details": ["112", "DateSent", "27-01-2018"]}], "SubmissionDate": "27-Feb-2018 11:12:45"},
        {"Reference": "G53DF1", "Type": "CT", "FileName": "FILE4.XML", "TotalReports": 45, "FailedReports": 0, "Errors": [], "SubmissionDate": "1-Jan-2018 17:08:53"}]""".stripMargin

  val json = Json.parse(jsonStr)

  val fakeReportStatusConnector = new ReportStatusConnector(getHttpMock(200, Some(json)), configuration, environment)

  def loggedInController(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap): ReportStatusController = {
    FakeDataCacheConnector.resetCaptures()
    FakeDataCacheConnector.save[String]("", VOAAuthorisedId.toString, username)
    new ReportStatusController(frontendAppConfig, messagesApi, FakeDataCacheConnector, fakeReportStatusConnector, dataRetrievalAction, new DataRequiredActionImpl)
  }

  def notLoggedInController(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) = {
    FakeDataCacheConnector.resetCaptures()
    new ReportStatusController(frontendAppConfig, messagesApi, FakeDataCacheConnector, fakeReportStatusConnector, dataRetrievalAction, new DataRequiredActionImpl)
  }
  def viewAsString() = reportStatus(username, frontendAppConfig)(fakeRequest, messages).toString

  "ReportStatus Controller" must {

    "return OK and the correct view for a GET" in {
      val result = loggedInController().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "if not authorized by VOA must go to the login page" in {
      val result = notLoggedInController().onPageLoad()(fakeRequest)
      def onwardRoute = routes.LoginController.onPageLoad(NormalMode)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "if authorized must request the LoginConnector for reports currently associated with this account" in {}
  }
}




