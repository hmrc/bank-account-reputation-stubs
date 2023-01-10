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

object AssessmentType {

  final val Yes = "yes"
  final val No = "no"
  final val Error = "error"
  final val Indeterminate = "indeterminate"
  final val Inapplicable = "inapplicable"
  final val Partial = "partial"


  def fromString(value: String): String = value.toLowerCase match {
    case "yes" => Yes
    case "no" => No
    case "error" => Error
    case "indeterminate" => Indeterminate
    case "inapplicable" => Inapplicable
    case "partial" => Partial
    case _ => throw new Exception(s"Unable to match `$value` to an AssessmentType")
  }
}
