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

class DataTest extends AnyFunSuite {

  test("Parser returns valid string") {
    val result = Data.parser("something")

    assertThat(result).isEqualTo(Some("something"))
  }

  test("Parser returns none for empty string") {
    val result = Data.parser("")

    assertThat(result).isEqualTo(None)
  }

  test("Parser returns none for blank string") {
    val result = Data.parser("   ")

    assertThat(result).isEqualTo(None)
  }

  test("Parser returns none for null") {
    val result = Data.parser(null)

    assertThat(result).isEqualTo(None)
  }
}
