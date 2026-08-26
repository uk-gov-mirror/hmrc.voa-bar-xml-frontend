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

package views

import forms.FileUploadDataFormProvider
import models.FileUploadData
import models.UpScanRequests.{InitiateResponse, UploadRequest}
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.behaviours.ViewBehaviours

class CouncilTaxUploadViewSpec extends ViewBehaviours with ViewSpecBase:

  private def councilTaxUpload = inject[views.html.councilTaxUpload]

  private val username         = "BA0345"
  private val messageKeyPrefix = "councilTaxUpload"

  private val form: Form[FileUploadData] = FileUploadDataFormProvider()()

  private val councilTaxUploadFakeRequest = FakeRequest(GET, "/service-root/some-page")

  private val initiateResponse = InitiateResponse(
    reference = "foo",
    uploadRequest = UploadRequest(
      href = "http://www.bar.foo",
      fields = Map(
        ("acl", "private"),
        ("key", "xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"),
        ("policy", "xxxxxxxx=="),
        ("x-amz-algorithm", "AWS4-HMAC-SHA256"),
        ("x-amz-credential", "ASIAxxxxxxxxx/20180202/eu-west-2/s3/aws4_request"),
        ("x-amz-date", "yyyyMMddThhmmssZ"),
        ("x-amz-meta-callback-url", "https://myservice.com/callback"),
        ("x-amz-signature", "xxxx"),
        ("x-amz-meta-consuming-service", "something"),
        ("x-amz-meta-session-id", "session-1234567890"),
        ("x-amz-meta-request-id", "request-12345789")
      )
    )
  )

  private def createView(displayInitiateResponse: Boolean = true) =
    if displayInitiateResponse then
      councilTaxUpload(username, form, Some(initiateResponse))(using councilTaxUploadFakeRequest, messages)
    else
      councilTaxUpload(username, form)(using councilTaxUploadFakeRequest, messages)

  private val doc = asDocument(createView())

  "CouncilTaxUpload view" should {
    behave like normalPage(() => createView(), messageKeyPrefix, "title", "submit.button")

    "include an username element displaying the BA name based on given BA Code" in {
      val user = doc.select("#account-info-header > li:nth-child(2) > span:nth-child(2)").text
      user shouldBe "Reading"
    }

    "include a sign out link which redirects the users to the login page" in {
      val href = doc.getElementsByClass("hmrc-sign-out-nav__link").first.attr("href")
      href shouldBe controllers.routes.SignOutController.signOut.url
    }

    "contain Submit button with the value Upload" in {
      val doc          = asDocument(createView())
      val submitButton = doc.getElementById("submit").text()
      submitButton shouldBe messages("councilTaxUpload.submit.button")
    }

    "contain Upscan expected hidden inputs" in {
      val doc          = asDocument(createView())
      val upscanInputs = doc.getElementById("councilTaxUploadForm").getElementsByAttributeValue("type", "hidden")
      Option(upscanInputs.select("[name='policy']"))                       shouldBe defined
      upscanInputs.select("[name='policy']").`val`                         shouldBe initiateResponse.uploadRequest.fields("policy")
      Option(upscanInputs.select("[name='x-amz-algorithm']"))              shouldBe defined
      upscanInputs.select("[name='x-amz-algorithm']").`val`                shouldBe initiateResponse.uploadRequest.fields("x-amz-algorithm")
      Option(upscanInputs.select("[name='x-amz-credential']"))             shouldBe defined
      upscanInputs.select("[name='x-amz-credential']").`val`               shouldBe initiateResponse.uploadRequest.fields("x-amz-credential")
      Option(upscanInputs.select("[name='x-amz-date']"))                   shouldBe defined
      upscanInputs.select("[name='x-amz-date']").`val`                     shouldBe initiateResponse.uploadRequest.fields("x-amz-date")
      Option(upscanInputs.select("[name='x-amz-meta-callback-url']"))      shouldBe defined
      upscanInputs.select("[name='x-amz-meta-callback-url']").`val`        shouldBe initiateResponse.uploadRequest.fields("x-amz-meta-callback-url")
      Option(upscanInputs.select("[name='x-amz-meta-consuming-service']")) shouldBe defined
      upscanInputs.select("[name='x-amz-meta-consuming-service']").`val`   shouldBe initiateResponse.uploadRequest.fields("x-amz-meta-consuming-service")
      Option(upscanInputs.select("[name='x-amz-signature']"))              shouldBe defined
      upscanInputs.select("[name='x-amz-signature']").`val`                shouldBe initiateResponse.uploadRequest.fields("x-amz-signature")
      Option(upscanInputs.select("[name='acl']"))                          shouldBe defined
      upscanInputs.select("[name='acl']").`val`                            shouldBe initiateResponse.uploadRequest.fields("acl")
      Option(upscanInputs.select("[name='key']"))                          shouldBe defined
      upscanInputs.select("[name='key']").`val`                            shouldBe initiateResponse.uploadRequest.fields("key")
    }

    "do not contain Submit button when there is not initiate response" in {
      val doc          = asDocument(createView(false))
      val submitButton = Option(doc.getElementById("submit"))
      submitButton shouldBe None
    }
  }
