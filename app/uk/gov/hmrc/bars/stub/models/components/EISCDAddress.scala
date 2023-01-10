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

package uk.gov.hmrc.bars.stub.models.components

import play.api.libs.json.{Json, OFormat, Reads}

case class EISCDAddress(lines: Seq[String],
                        town: Option[String] = None,
                        county: Option[String] = None,
                        country: Option[EISCDCountry] = None,
                        postCode: Option[String] = None)

object EISCDAddress {
  implicit val writes: OFormat[EISCDAddress] = Json.format[EISCDAddress]

  implicit val reads: Reads[EISCDAddress] = Json.reads[EISCDAddress]
}
