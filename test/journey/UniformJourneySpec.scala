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

package journey

import cats.data.Validated.{Invalid, Valid}
import ltbs.uniform.ErrorTree
import uk.gov.hmrc.vo.unit.test.BaseSpec

class UniformJourneySpec extends BaseSpec:

  import UniformJourney.*

  private val string226Char: String = (1 to 22).map(_ => "1234567890").mkString("") + "123456"
  private val string227Char: String = string226Char + "7"

  "UniformJourney" should {
    "validate BAReport" in {
      baReportValidation("1234")         shouldBe Valid("1234")
      baReportValidation("1")            shouldBe Valid("1")
      baReportValidation("123456789012") shouldBe Valid("123456789012")
      baReportValidation("")             shouldBe a[Invalid[?]]
      baReportValidation("asdasd")       shouldBe a[Valid[?]]
      baReportValidation("|")            shouldBe a[Invalid[?]]
    }

    "validate BA-ref" in {
      baReferenceValidation("1234")          shouldBe Valid("1234")
      baReferenceValidation("adasd#$^&*()")  shouldBe Valid("adasd#$^&*()")
      baReferenceValidation("adasd#$^&*(%)") shouldBe a[Valid[?]]
      baReferenceValidation("|")             shouldBe a[Invalid[?]]
    }

    "validate UPRN" in {
      uprnValidation(None)                  shouldBe Valid(None)
      uprnValidation(Some("1123"))          shouldBe Valid(Some("1123"))
      uprnValidation(Some("123456789011"))  shouldBe Valid(Some("123456789011"))
      uprnValidation(Some("1234567890013")) shouldBe a[Invalid[?]]
      uprnValidation(Some(""))              shouldBe a[Invalid[?]]
    }

    "validate Address" in {
      val address = Address("99  Fosse Way %+", "ARDNAGOINE", None, Some("Fiction house"), "IV26 4YY")
      UniformJourney.longAddressValidation("some-address")(address).toEither.value shouldBe address
    }

    "reject invalid address" in {
      val address = Address("", "ARDNAGOINE", None, None, "HHGGD")
      UniformJourney.longAddressValidation("some-address")(address).toEither.left.value shouldBe a[ErrorTree]
    }

    "validate short address with 35 characters" in {
      val address = Address("12345678901234567890123456789012345", "ARDNAGOINE", None, Some("Fiction house"), "IV26 4YY")
      UniformJourney.shortAddressValidation("some-address")(address).toEither.value shouldBe address
    }

    "reject short address with more that 35 characters" in {
      val address = Address("123456789012345678901234567890123456", "ARDNAGOINE", None, Some("Fiction house"), "IV26 4YY")
      UniformJourney.shortAddressValidation("some-address")(address).toEither.left.value shouldBe a[ErrorTree]
    }

    "Validate correct contact details" in {
      val contactDetails = ContactDetails("First name", "lastName", None, None)
      UniformJourney.propertyContactDetailValidator(contactDetails).toEither.value shouldBe contactDetails
    }

    "reject invalid contact details" in {
      val contactDetails = ContactDetails("", "", Some("*&*&"), Some("*&(*&"))
      UniformJourney.propertyContactDetailValidator(contactDetails).toEither.left.value shouldBe a[ErrorTree]
      UniformJourney.propertyContactDetailValidator(contactDetails).toEither.left.value   should have size 4
    }

    "validate planning reference" in {
      UniformJourney.planningRefValidator("1234asdf½").toEither.left.value                  shouldBe a[ErrorTree]
      UniformJourney.planningRefValidator("").toEither.left.value                           shouldBe a[ErrorTree]
      UniformJourney.planningRefValidator("12345678901234567890123456").toEither.left.value shouldBe a[ErrorTree]
      UniformJourney.planningRefValidator("€ \tŠ \tš \tŽ \tž \tŒ \tœ \tŸ").toEither.value   shouldBe "€ \tŠ \tš \tŽ \tž \tŒ \tœ \tŸ"
    }

    "validate comments" in {
      UniformJourney.commentsValidation(Option(string226Char)).toEither.value                   shouldBe Option(string226Char)
      UniformJourney.commentsValidation(Option(string227Char)).toEither.left.value              shouldBe a[ErrorTree]
      UniformJourney.commentsValidation(Option("€ \tŠ \tš \tŽ \tž \tŒ \tœ \tŸ")).toEither.value shouldBe Some("€ \tŠ \tš \tŽ \tž \tŒ \tœ \tŸ")
    }
  }
