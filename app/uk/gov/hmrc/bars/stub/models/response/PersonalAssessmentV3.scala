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

package uk.gov.hmrc.bars.stub.models.response

import play.api.libs.json.{Json, OFormat, Reads}

case class PersonalAssessmentV3(accountNumberWithSortCodeIsValid: String,
                                accountExists: String,
                                nameMatches: String,
                                addressMatches: String,
                                nonConsented: String,
                                subjectHasDeceased: String,
                                nonStandardAccountDetailsRequiredForBacs: String,
                                sortCodeIsPresentOnEISCD: String,
                                sortCodeBankName: Option[String] = None,
                                sortCodeSupportsDirectDebit: String,
                                sortCodeSupportsDirectCredit: String)

object PersonalAssessmentV3 {
  implicit val writes: OFormat[PersonalAssessmentV3] = Json.format[PersonalAssessmentV3]

  implicit val reads: Reads[PersonalAssessmentV3] = Json.reads[PersonalAssessmentV3]
}
