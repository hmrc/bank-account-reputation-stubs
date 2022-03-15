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

package uk.gov.hmrc.bars.stub.models.components

import play.api.libs.json.{Json, OFormat, Reads}

case class Subject(title: Option[String] = None,
                   name: Option[String] = None,
                   firstName: Option[String] = None,
                   lastName: Option[String] = None,
                   dob: Option[String] = None,
                   address: Option[Address] = None)

object Subject {
  implicit val writes: OFormat[Subject] = Json.format[Subject]

  implicit val reads: Reads[Subject] = Json.reads[Subject]
}
