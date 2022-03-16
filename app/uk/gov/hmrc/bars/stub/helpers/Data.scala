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

import uk.gov.hmrc.bars.stub.models.AssessmentType._
import uk.gov.hmrc.bars.stub.models.components.{Business, Subject}

object Data {
  def parser(value: String): Option[String] = {
    value match {
      case null => None
      case str if str.trim == "" => None
      case str => Some(str.trim)
    }
  }

  def checkBusinessName(givenName: String, business: Option[Business]): String = {
    if (business.isDefined) {
      checkName(givenName, business.get.companyName)
    } else {
      Inapplicable
    }
  }

  def checkName(givenName: String, expectedName: String): String = {
    givenName match {
      case `expectedName` => Yes
      case _ => No
    }
  }

  def constructPersonalName(subject: Option[Subject]): String = {
    if (subject.isDefined) {
      val subjectData = subject.get
      if (subjectData.name.isDefined) {
        subjectData.name.get
      } else {
        s"${subjectData.title} ${subjectData.firstName} ${subjectData.lastName}".trim
      }
    } else {
      " "
    }
  }
}
