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

sealed abstract class ValidationError(errorMessage: String) {

  def generateErrorMessage(someText: Option[String] = None): String = {
    s"${someText.getOrElse("")}$errorMessage"
  }
}

case object INVALID_SORTCODE extends ValidationError(": invalid sortcode")

case object INVALID_ACCOUNT_NUMBER extends ValidationError(": invalid account number")

case object NO_ADDRESS_LINES extends ValidationError("no address lines")

case object EXCESS_ADDRESS_LINES extends ValidationError("too many address lines")

case object BLANK_ADDRESS extends ValidationError("empty address text")

case object EXCESS_ADDRESS_LENGTH extends ValidationError("address text is too long")

case object BLANK_TOWN extends ValidationError("town length is blank")

case object EXCESS_TOWN_LENGTH extends ValidationError("town is too long")

case object BAD_NAME extends ValidationError("g")

case object BLANK_TITLE extends ValidationError("h")

case object EXCESS_TITLE extends ValidationError("i")

case object BLANK_NAME extends ValidationError("j")

case object EXCESS_NAME extends ValidationError("k")

case object BLANK_FIRST_NAME extends ValidationError("l")

case object EXCESS_FIRST_NAME extends ValidationError("m")

case object BLANK_LAST_NAME extends ValidationError("n")

case object EXCESS_LAST_NAME extends ValidationError("o")

case object BAD_DOB extends ValidationError("p")

case object SORT_CODE_ON_DENY_LIST extends ValidationError("q")
