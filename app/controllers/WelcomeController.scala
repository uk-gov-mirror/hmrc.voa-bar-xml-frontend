/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.actions._
import config.FrontendAppConfig
import connectors.DataCacheConnector
import identifiers.{CouncilTaxStartId, TaskListId, VOAAuthorisedId, WelcomeFormId}
import models.NormalMode
import play.api.Configuration
import play.api.mvc.MessagesControllerComponents
import utils.Navigator

import scala.concurrent.ExecutionContext

class WelcomeController @Inject()(appConfig: FrontendAppConfig,
                                  config: Configuration,
                                  getData: DataRetrievalAction,
                                  requireData: DataRequiredAction,
                                  navigator: Navigator,
                                  dataCacheConnector: DataCacheConnector,
                                  controllerComponents: MessagesControllerComponents,
                                  welcome: views.html.welcome
                                 ) (implicit ec: ExecutionContext)
  extends FrontendController(controllerComponents) with I18nSupport {
  val cr05FeatureEnabled = config.getOptional[Boolean]("feature.cr05.enabled").contains(true)


  def onPageLoad = getData.async {
    implicit request =>
      dataCacheConnector.getEntry[String](request.externalId, VOAAuthorisedId.toString) map {
        case Some(username) => Ok(welcome(appConfig, username, cr05FeatureEnabled))
        case None => Redirect(routes.LoginController.onPageLoad(NormalMode))
      }
  }

  def goToCouncilTaxUploadPage() = (getData andThen requireData) { implicit request =>
    Redirect(navigator.nextPage(CouncilTaxStartId, NormalMode)(request.userAnswers))
  }

  def goToTaskListLoadPage() = (getData andThen requireData) { implicit request =>
    Redirect(navigator.nextPage(TaskListId, NormalMode)(request.userAnswers))
  }
}
