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

import org.assertj.core.api.Assertions.assertThat
import org.scalatest.funsuite.AnyFunSuite
import uk.gov.hmrc.bars.stub.helpers.Validation.{requireValid, requireValidLength}

import java.util.regex.Pattern

class ValidationTest extends AnyFunSuite {

  test("Exception not thrown if text does match pattern") {
    val sortCodePattern: Pattern = "[0-9]{6}".r.pattern
    requireValid("123456", sortCodePattern, INVALID_SORTCODE)
  }

  test("Exception thrown if text does not match pattern") {
    val sortCodePattern: Pattern = "[0-9]{6}".r.pattern
    val thrown = intercept[Exception] {
      requireValid("1234", sortCodePattern, INVALID_SORTCODE)
    }

    assertThat(thrown.getMessage).isEqualTo("1234: invalid sortcode")
  }

  test("Exception thrown if isValid enumerates to false") {
    val thrown = intercept[Exception] {
      requireValid(isValid = false, BLANK_TOWN)
    }

    assertThat(thrown.getMessage).isEqualTo("town length is blank")
  }

  test("Exception not thrown if isValid enumerates to true") {
    requireValid(isValid = true, INVALID_SORTCODE)
  }

  test("Exception not thrown if length is valid") {
    requireValidLength(
      text = Some("text"),
      minLength = 3,
      maxLength = 5,
      errorTooShort = BLANK_TOWN,
      errorTooLong = EXCESS_TOWN_LENGTH
    )
  }

  test("Exception thrown if length is too long") {
    val thrown = intercept[Exception] {
      requireValidLength(
        text = Some("long text"),
        minLength = 3,
        maxLength = 5,
        errorTooShort = BLANK_TOWN,
        errorTooLong = EXCESS_TOWN_LENGTH
      )
    }

    assertThat(thrown.getMessage).isEqualTo("town is too long")
  }

  test("Exception thrown if length is too short") {
    val thrown = intercept[Exception] {
      requireValidLength(
        text = Some("oh"),
        minLength = 3,
        maxLength = 5,
        errorTooShort = BLANK_TOWN,
        errorTooLong = EXCESS_TOWN_LENGTH
      )
    }

    assertThat(thrown.getMessage).isEqualTo("town length is blank")
  }

  test("Exception thrown if test not provided") {
    val thrown = intercept[Exception] {
      requireValidLength(
        text = None,
        minLength = 3,
        maxLength = 5,
        errorTooShort = BLANK_TOWN,
        errorTooLong = EXCESS_TOWN_LENGTH
      )
    }

    assertThat(thrown.getMessage).isEqualTo("town length is blank")
  }
}
