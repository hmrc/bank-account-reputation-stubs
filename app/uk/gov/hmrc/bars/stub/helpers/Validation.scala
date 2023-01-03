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

package uk.gov.hmrc.bars.stub.helpers

import java.util.regex.Pattern

object Validation {
  def requireValid(data: String, requirement: Pattern, ve: ValidationError): Unit = {
    if (!requirement.matcher(data).matches())
      throw new ValidationException(ve, ve.generateErrorMessage(Some(data)))
  }

  def requireValid(isValid: Boolean, ve: ValidationError): Unit = {
    if (!isValid)
      throw new ValidationException(ve, ve.generateErrorMessage())
  }

  def requireValidLength(text: Option[String], minLength: Int = 0, maxLength: Int, errorTooShort: ValidationError, errorTooLong: ValidationError): Unit = {
    if (text.isDefined) {
      if (text.get.length > maxLength) {
        throw new ValidationException(errorTooLong, errorTooLong.generateErrorMessage())
      }
      if (minLength >= text.get.length) {
        throw new ValidationException(errorTooShort, errorTooShort.generateErrorMessage())
      }
    } else {
      throw new ValidationException(errorTooShort, errorTooShort.generateErrorMessage())
    }
  }
}
