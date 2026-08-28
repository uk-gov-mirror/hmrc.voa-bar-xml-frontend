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

package models

import uk.gov.hmrc.vo.unit.test.BaseSpec

class ReportStatusSpec extends BaseSpec:

  private val baCode                        = "ba1221"
  private val submissionId                  = "sId999"
  private val reportStatusError: Seq[Error] = Seq(Error("BAD-CHAR"))

  "ReportStatus model" should {
    "produce a ReportStatus model with no errors" in {
      val result = ReportStatus(submissionId, baCode = Some(baCode), status = Some("SUBMITTED"))
      result.baCode shouldBe Some(baCode)
      result.id     shouldBe submissionId
      result.status shouldBe Some("SUBMITTED")
    }

    "produce a ReportStatus model with errors" in {
      val result = ReportStatus(submissionId, baCode = Some(baCode), status = Some("INVALIDATED"), errors = reportStatusError)
      result.baCode shouldBe Some(baCode)
      result.id     shouldBe submissionId
      result.status shouldBe Some("INVALIDATED")
      result.errors shouldBe reportStatusError
    }
  }
