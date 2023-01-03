/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.bars.stub.models.response

import play.api.libs.json.{Json, OFormat, Reads}

case class ValidateResponseV2(accountNumberWithSortCodeIsValid: String,
                              nonStandardAccountDetailsRequiredForBacs: String,
                              sortCodeIsPresentOnEISCD: String,
                              supportsBACS: Option[String] = None,
                              ddiVoucherFlag: Option[String] = None,
                              directDebitsDisallowed: Option[String] = None,
                              directDebitInstructionsDisallowed: Option[String] = None,
                              iban: Option[String] = None,
                              sortCodeBankName: Option[String] = None
                             )

object ValidateResponseV2 {
  implicit val writes: OFormat[ValidateResponseV2] = Json.format[ValidateResponseV2]

  implicit val reads: Reads[ValidateResponseV2] = Json.reads[ValidateResponseV2]
}
