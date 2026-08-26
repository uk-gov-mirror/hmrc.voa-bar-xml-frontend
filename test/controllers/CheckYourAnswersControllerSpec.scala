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

import play.api.test.Helpers.*
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction}
import play.api.mvc.MessagesControllerComponents
import viewmodels.AnswerSection

class CheckYourAnswersControllerSpec extends ControllerSpecBase:

  private def controllerComponents = inject[MessagesControllerComponents]
  private def checkYourAnswerView  = inject[views.html.check_your_answers]

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    CheckYourAnswersController(
      messagesApi,
      dataRetrievalAction,
      DataRequiredActionImpl(ec),
      checkYourAnswerView,
      controllerComponents
    )

  "Check Your Answers Controller" should {
    "return 200 and the correct view for a GET" in {
      val result = controller().onPageLoad()(getRequest)
      status(result)          shouldBe OK
      contentAsString(result) shouldBe checkYourAnswerView(Seq(AnswerSection(None, Seq())))(using getRequest, messages).toString
    }

    "redirect to Session Expired for a GET if not existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad()(getRequest)

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SessionExpiredController.onPageLoad.url)
    }
  }
