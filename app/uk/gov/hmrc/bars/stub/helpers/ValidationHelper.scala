/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.bars.stub.helpers

import uk.gov.hmrc.bars.stub.helpers.Validation.{requireValid, requireValidLength}
import uk.gov.hmrc.bars.stub.models.components.{AccountDetails, Address}

import java.util.regex.Pattern

object ValidationHelper {

  val sortCodePattern: Pattern = "[0-9]{6}".r.pattern
  val accountCodePattern: Pattern = "[0-9]{8}".r.pattern

  def validateAccount(accountDetails: AccountDetails, sortCodeDenyList: List[String]) {
    requireValid(!sortCodeDenyList.contains(accountDetails.sortCode), SORT_CODE_ON_DENY_LIST)
    requireValid(accountDetails.sortCode, sortCodePattern, INVALID_SORTCODE)
    requireValid(accountDetails.accountNumber, accountCodePattern, INVALID_ACCOUNT_NUMBER)
  }

  def validateAddress(address: Option[Address]): Unit = {
    if (address.isDefined) {
      requireValid(address.get.lines.nonEmpty, NO_ADDRESS_LINES)
      requireValid(address.get.lines.size <= 4, EXCESS_ADDRESS_LINES)
      requireValid(0 < address.get.lines.map(_.length).sum, BLANK_ADDRESS)
      requireValid(address.get.lines.map(_.length).sum <= 140, EXCESS_ADDRESS_LENGTH)
      requireValidLength(
        text = address.get.town,
        maxLength = 35,
        errorTooShort = BLANK_TOWN,
        errorTooLong = EXCESS_TOWN_LENGTH
      )
    }
  }

}
