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

package uk.gov.hmrc.bars.stub.models

import AssessmentType.No

case class EISCDData(
                      sortCode: String,
                      bankCode: Option[String] = None,
                      bankName: Option[String] = None,
                      nonStandardBacsDataRequired: String = No,
                      addressLine: Option[String] = None,
                      town: Option[String] = None,
                      country: Option[String] = None,
                      postcode: Option[String] = None,
                      telephone: Option[String] = None,
                      bacsStatus: Option[String] = None,
                      chapsStatus: Option[String] = None,
                      ddiVoucher: Option[String] = None,
                      supportsDirectDebit: String = No,
                      supportsDirectCredit: String = No,
                      ibanPrefix: Option[String] = None
                    )
