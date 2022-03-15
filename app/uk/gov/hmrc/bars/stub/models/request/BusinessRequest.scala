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

package uk.gov.hmrc.bars.stub.models.request

import play.api.libs.json.{Json, OFormat, Reads}
import uk.gov.hmrc.bars.stub.models.components.{AccountDetails, Business}

case class BusinessRequest(account: AccountDetails, business: Option[Business] = None)

object BusinessRequest {
  implicit val writes: OFormat[BusinessRequest] = Json.format[BusinessRequest]

  implicit val reads: Reads[BusinessRequest] = Json.reads[BusinessRequest]
}
