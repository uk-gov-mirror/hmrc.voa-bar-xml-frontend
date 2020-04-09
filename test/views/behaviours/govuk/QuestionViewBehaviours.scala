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

package views.behaviours.govuk

import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat

trait QuestionViewBehaviours[A] extends ViewBehaviours {

  val errorKey = "value"
  val errorMessage = "error.number"
  val error = FormError(errorKey, errorMessage)

  val form: Form[A]

  def pageWithTextFields(createView: (Form[A]) => HtmlFormat.Appendable,
                         messageKeyPrefix: String,
                         expectedFormAction: String,
                         fields: String*) = {

    "behave like a question page" when {
      "rendered" must {
        for(field <- fields) {
          s"contain an input for $field" in {
            val doc = asDocument(createView(form))
            assertRenderedById(doc, field)
          }
        }

        "not render an error summary" in {
          val doc = asDocument(createView(form))
          assertNotRenderedById(doc, "error-summary-heading")
        }
      }

      for(field <- fields) {
        s"rendered with an error with field '$field'" must {
          "show an error summary" in {
            val doc = asDocument(createView(form.withError(FormError(field, "error"))))
            assertRenderedByCssSelector(doc, ".govuk-error-summary")
          }

          s"show an error in the label for field '$field'" in {
            val doc = asDocument(createView(form.withError(FormError(field, "error"))))
            val errorSpan = doc.getElementsByClass("govuk-error-message").first
            errorSpan.parent.children.first.attr("for") mustBe field
          }
        }
      }
    }
  }

  def labelDefinedAndUsedOnce(option: String, prefix: String, view: () => HtmlFormat.Appendable) = {
    val doc = asDocument(view())
    assert(messages.isDefinedAt(s"$prefix.$option"))
    val label = doc.select(s"label[for=$prefix.$option]")
    assert(label.size() == 1)
  }
}
