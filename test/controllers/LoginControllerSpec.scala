/*
 * Copyright 2026 HM Revenue & Customs
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

import connectors.{FakeDataCacheConnector, LoginConnector}
import controllers.actions.*
import forms.LoginFormProvider
import identifiers.{LoginId, VOAuthorisedId}
import models.{CacheMap, Login, NormalMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import views.ViewSpecBase

import scala.concurrent.Future
import scala.util.{Failure, Success}

class LoginControllerSpec extends ControllerSpecBase with ViewSpecBase:

  private def onwardRoute = routes.LoginController.onPageLoad(NormalMode)

  private val formProvider = LoginFormProvider()
  private val form         = formProvider()
  private val validBACode  = "ba0114"

  private def controllerComponents = inject[MessagesControllerComponents]
  private def login                = inject[views.html.login]

  private val loginConnector = mock[LoginConnector]
  when(loginConnector.doLogin(any[Login])(using any[HeaderCarrier]))
    .thenReturn(Future.successful(Success(OK)))

  private val loginConnectorFailed = mock[LoginConnector]
  when(loginConnectorFailed.doLogin(any[Login])(using any[HeaderCarrier]))
    .thenReturn(Future.successful(Failure(RuntimeException("Received exception from upstream service"))))

  private def controller(connector: LoginConnector, dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    FakeDataCacheConnector.resetCaptures()
    LoginController(
      messagesApi,
      FakeDataCacheConnector,
      FakeNavigator(desiredRoute = onwardRoute),
      dataRetrievalAction,
      formProvider,
      connector,
      controllerComponents,
      login,
      configuration
    )

  private def viewAsString(form: Form[Login] = form) = login(form, NormalMode)(using getRequest, messages).toString

  "Login Controller" should {
    "return OK and the correct view for a GET" in {
      val result = controller(loginConnector).onPageLoad(NormalMode)(getRequest)

      status(result)          shouldBe OK
      contentAsString(result) shouldBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData       = Map(LoginId.toString -> Json.toJson(Login("username", "password")))
      val getRelevantData = FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(loginConnector, getRelevantData).onPageLoad(NormalMode)(getRequest)

      contentAsString(result) shouldBe viewAsString(form.fill(Login("username", "")))
    }

    "redirect to the next page when valid data is submitted" in {
      val result = controller(loginConnector).onSubmit(NormalMode)(
        postRequest.withFormUrlEncodedBody(("username", validBACode), ("password", "value 2"))
      )

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(onwardRoute.url)
    }

    "logging in should cache an authorization token" in {
      val result = controller(loginConnector).onSubmit(NormalMode)(
        postRequest.withMethod("POST").withFormUrlEncodedBody(("username", validBACode), ("password", "value 2"))
      )
      status(result)                                             shouldBe SEE_OTHER
      FakeDataCacheConnector.getCapture(VOAuthorisedId.toString) shouldBe Some(validBACode)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(loginConnector).onSubmit(NormalMode)(
        postRequest.withMethod("POST").withFormUrlEncodedBody(("value", "invalid value"))
      )

      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) shouldBe viewAsString(boundForm)
    }

    "return a Bad Request and errors when valid bacode is submitted but no Council Name can be found related to the bacode" in {
      val boundForm =
        form
          .withError("username", messages("error.invalid_username"))
          .withError("password", messages("error.invalid_password"))

      val result = controller(loginConnector).onSubmit(NormalMode)(
        postRequest.withMethod("POST").withFormUrlEncodedBody(("username", "ba0000"), ("password", "value"))
      )

      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) shouldBe viewAsString(boundForm)
    }

    "return a Bad Request and errors when the backend service call fails" in {
      val boundForm = form.bind(Map("username" -> "value 1", "password" -> "value2"))

      intercept[Exception] {
        val result = controller(loginConnectorFailed).onSubmit(NormalMode)(
          postRequest.withMethod("POST").withFormUrlEncodedBody(("username", "value 1"), ("password", "value 2"))
        )
        status(result)                                             shouldBe BAD_REQUEST
        contentAsString(result)                                    shouldBe viewAsString(boundForm)
        redirectLocation(result)                                   shouldBe Some(onwardRoute.url)
        FakeDataCacheConnector.getCapture(VOAuthorisedId.toString) shouldBe None
      }
    }
  }
