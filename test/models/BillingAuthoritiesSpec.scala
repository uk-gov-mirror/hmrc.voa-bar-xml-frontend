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

class BillingAuthoritiesSpec extends BaseSpec:

  private val existingBaCode    = "BA0230"
  private val nonExistingBaCode = "ba9999"
  private val ForestHeath       = "ba3510"
  private val StEdmundsbury     = "ba3525"
  private val SuffolkCoastal    = "ba3530"
  private val Waveney           = "ba3535"
  private val BAMapSize         = 350

  "BillingAuthorities" should {
    "return the name of the Billing Authority for an existing Billing Authority Code" in {
      BillingAuthorities.find(existingBaCode) shouldBe Some("Luton")
    }

    "return None if no baCode is found related to the given code even if the user is logged in" in {
      BillingAuthorities.find(nonExistingBaCode) shouldBe None
    }

    "have 350 entries" in {
      BillingAuthorities.billingAuthorities.size shouldBe BAMapSize
    }

    "return None if Forest Heath Code is found related to the given code even if the user is logged in" in {
      BillingAuthorities.find(ForestHeath) shouldBe None
    }

    "return None if St Edmundsbury Code is found related to the given code even if the user is logged in" in {
      BillingAuthorities.find(StEdmundsbury) shouldBe None
    }

    "return None if Suffolk Coastal Code is found related to the given code even if the user is logged in" in {
      BillingAuthorities.find(SuffolkCoastal) shouldBe None
    }

    "return None if Waveney Code is found related to the given code even if the user is logged in" in {
      BillingAuthorities.find(Waveney) shouldBe None
    }
  }
