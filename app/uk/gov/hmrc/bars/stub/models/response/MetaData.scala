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
import uk.gov.hmrc.bars.stub.models.components.EISCDAddress

case class MetaData(sortCode: String,
                    bankName: String,
                    bankCode: Option[String] = None,
                    address: EISCDAddress,
                    telephone: Option[String] = None,
                    bacsOfficeStatus: String,
                    chapsSterlingStatus: String,
                    branchName: Option[String] = None,
                    ddiVoucherFlag: Option[String] = None,
                    disallowedTransactions: Seq[String] = Seq.empty)

object MetaData {
  implicit val writes: OFormat[MetaData] = Json.format[MetaData]

  implicit val reads: Reads[MetaData] = Json.reads[MetaData]
}
