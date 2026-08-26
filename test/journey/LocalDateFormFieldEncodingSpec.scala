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

import journey.LocalDateFormFieldEncoding.{day, month, year}
import ltbs.uniform.ErrorTree
import uk.gov.hmrc.vo.unit.test.BaseSpec

import java.time.LocalDate

class LocalDateFormFieldEncodingSpec extends BaseSpec:

  private val encoding = LocalDateFormFieldEncoding()
  private val tomorrow = LocalDate.now().plusDays(1)
  private val today    = LocalDate.now()

  private val invalidInputDate = Table(
    ("day", "month", "year"),
    ("", "", ""),
    ("1", "", ""),
    ("", "2", ""),
    ("", "", "2020"),
    ("", "2", "2020"),
    ("1", "", "2020"),
    ("1", "1", ""),
    ("1", "1", LocalDate.now().plusYears(1).getYear.toString),
    (tomorrow.getDayOfMonth.toString, tomorrow.getMonthValue.toString, tomorrow.getYear.toString),
    ("31", "2", "2020"),
    ("a", "2", "2020"),
    ("31", "a", "2020"),
    ("31", "2", "a"),
    ("32", "1", "2020"),
    ("1", "13", "2020")
  )

  private val validInputDate = Table(
    ("day", "month", "year"),
    ("23", "12", "1993"),
    ("1", "4", "1993"),
    (today.getDayOfMonth.toString, today.getMonthValue.toString, today.getYear.toString)
  )

  "LocalDateFormFieldEncoding" should {
    "decode and validate correct date" in {
      val input = Map(
        year  -> List("2020"),
        month -> List("3"),
        day   -> List("30")
      )
      encoding.decode(input).value shouldBe LocalDate.of(2020, 3, 30)
    }

    "Fail validation for all invalidInput" in
      forAll(invalidInputDate) { (_day: String, _month: String, _year: String) =>
        val input = Map(
          year  -> List(_year),
          month -> List(_month),
          day   -> List(_day)
        )
        encoding.decode(input).left.value shouldBe a[ErrorTree]
      }

    "Validate input for all valid input dates" in
      forAll(validInputDate) { (_day: String, _month: String, _year: String) =>
        val input = Map(
          year  -> List(_year),
          month -> List(_month),
          day   -> List(_day)
        )
        encoding.decode(input).value shouldBe a[LocalDate]
      }

    "convert LocalDate to Input" in {
      encoding.encode(LocalDate.of(1900, 1, 1)) shouldBe
        Map(
          year  -> List("1900"),
          month -> List("1"),
          day   -> List("1")
        )
    }
  }
