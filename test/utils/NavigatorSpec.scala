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

package utils

import controllers.routes
import identifiers.*
import models.*
import uk.gov.hmrc.vo.unit.test.BaseAppSpec

class NavigatorSpec extends BaseAppSpec:

  private val navigator         = Navigator()
  private val mockUserAnswers   = mock[UserAnswers]
  private val formUserAnswers   = FakeUserAnswers(Login("", ""))
  private val uploadUserAnswers = FakeUserAnswers(Login("", ""))

  "Navigator" when {
    "in Normal mode" should {
      "go to Login page from an identifier that doesn't exist in the route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, NormalMode)(mock[UserAnswers]) shouldBe routes.LoginController.onPageLoad(NormalMode)
      }

      "on a valid submit from Login page go to Welcome page" in {
        when(mockUserAnswers.login).thenReturn(Some(Login("username", "pass")))
        navigator.nextPage(LoginId, NormalMode)(mockUserAnswers) shouldBe routes.WelcomeController.onPageLoad
      }

      "on choosing Council Tax web form should redirect to web form Start Page" in {
        WelcomeFormId.toString                                         shouldBe "welcomeForm"
        navigator.nextPage(WelcomeFormId, NormalMode)(formUserAnswers) shouldBe routes.UniformController.myJourney("ba-report")
      }

      "on selecting Council Tax Upload link should redirect to Council Tax Upload Page" in {
        CouncilTaxStartId.toString                                           shouldBe "counciltaxstart"
        navigator.nextPage(CouncilTaxStartId, NormalMode)(uploadUserAnswers) shouldBe routes.CouncilTaxUploadController.onPageLoad()
      }

      "on selecting Add Property Report Detail Journey link should redirect to Add Property Journey Report Details Page" in {
        AddPropertyReportDetailsId.toString                                           shouldBe "addpropertyreportdetailsid"
        navigator.nextPage(AddPropertyReportDetailsId, NormalMode)(uploadUserAnswers) shouldBe
          routes.UniformController.addCommonSectionJourney("add-property-ba-report")
      }

      "on selecting Add Property Journey link should redirect to Add Property Journey Page" in {
        AddPropertyId.toString                                           shouldBe "addproperty"
        navigator.nextPage(AddPropertyId, NormalMode)(uploadUserAnswers) shouldBe routes.UniformController.propertyJourney(
          "add-property-UPRN",
          PropertyType.PROPOSED
        )
      }

      "on selecting Add Comments Journey link should redirect to Add Comment Journey Page" in {
        AddCommentId.toString                                           shouldBe "addcomment"
        navigator.nextPage(AddCommentId, NormalMode)(uploadUserAnswers) shouldBe routes.UniformController.addCommentJourney()
      }

      "on selecting Task List should redirect to Task List Page" in {
        TaskListId.toString                                           shouldBe "tasklist"
        navigator.nextPage(TaskListId, NormalMode)(uploadUserAnswers) shouldBe routes.TaskListController.onPageLoad
      }

      "on selecting Check your answer should redirect to Check your answer Page" in {
        CheckYourAnswersId.toString                                           shouldBe "checkyouranswer"
        navigator.nextPage(CheckYourAnswersId, NormalMode)(uploadUserAnswers) shouldBe routes.UniformController.cr05CheckAnswerJourney()
      }
    }

    "in Check mode" should {
      "go to CheckYourAnswers from an identifier that doesn't exist in the edit route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, CheckMode)(mock[UserAnswers]) shouldBe routes.CheckYourAnswersController.onPageLoad
      }
    }
  }
