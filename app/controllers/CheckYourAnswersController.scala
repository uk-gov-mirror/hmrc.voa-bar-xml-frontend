/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import controllers.actions.{DataRequiredAction, DataRetrievalAction}
import viewmodels.AnswerSection
import views.html.check_your_answers
import config.FrontendAppConfig
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           checkYourAnswer: check_your_answers,
                                           controllerComponents: MessagesControllerComponents
                                          ) extends FrontendController(controllerComponents) with I18nSupport {

  def onPageLoad() = (getData andThen requireData) { implicit request =>

      val sections = Seq(AnswerSection(None, Seq()))
      Ok(checkYourAnswer(appConfig, sections))
  }
}
