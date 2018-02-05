/*
 * Copyright 2018 HM Revenue & Customs
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

import base.SpecBase
import play.api.Configuration
import uk.gov.hmrc.crypto.{ApplicationCryptoDI, Crypted}


class EncryptedLoginSpec extends SpecBase {

  val configuration = injector.instanceOf[Configuration]
  val userName = "user"
  val password = "password"

  "Given a username and plaintext password produce an encrypted login" in {

    val crypto = new ApplicationCryptoDI(configuration).JsonCrypto

    val result = EncryptedLogin(Login(userName, password))

    result.userName mustBe userName
    result.encryptedPassword mustBe crypto.decrypt(Crypted(password))
  }
}
