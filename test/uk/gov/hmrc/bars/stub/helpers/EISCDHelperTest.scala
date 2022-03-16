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
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bars.stub.AppConfig
import uk.gov.hmrc.bars.stub.helpers.EISCDHelper.{convertDDIVoucherToAssessmentType, flipYesNo, generateIBAN, supportsBACS}
import uk.gov.hmrc.bars.stub.models.AssessmentType.{No, Yes}
import uk.gov.hmrc.bars.stub.models.components.AccountDetails
import uk.gov.hmrc.bars.stub.models.{EISCD, EISCDData}

class EISCDHelperTest extends AnyFunSuite {

  private val env = Environment.simple()
  private val appConfig = new AppConfig(env, Configuration.load(env))
  private val branchData = appConfig.loadStubbedEISCDData

  test("Returns data if sort code is found") {
    val sortCode = "206705"
    val expectedResult = EISCD(sortCodeIsPresent = Yes, EISCDData(
      sortCode = sortCode,
      bankCode = Some("0934"),
      bankName = Some("BARCLAYS BANK UK PLC"),
      nonStandardBacsDataRequired = No,
      addressLine = Some("PO Box 424"),
      town = Some("Coventry"),
      country = Some("UNITED KINGDOM"),
      postcode = Some("CV2 2BR"),
      telephone = None,
      bacsStatus = Some("M"),
      chapsStatus = Some("I"),
      ddiVoucher = Some("N"),
      supportsDirectDebit = No,
      supportsDirectCredit = No,
      ibanPrefix = Some("GB21BARC")
    ))
    val result = EISCDHelper.getDataFor(sortCode, branchData)

    assertThat(result).isEqualTo(expectedResult)
  }

  test("Returns default if sort code is not found") {
    val sortCode = "123456"
    val expectedResult = EISCD(sortCodeIsPresent = No, EISCDData(sortCode))
    val result = EISCDHelper.getDataFor(sortCode, List.empty)

    assertThat(result).isEqualTo(expectedResult)
  }

  test("generate IBAN produces valid IBAN") {
    val expected = Some("GB96HSBC54789654966784")
    val actual = generateIBAN(Some("GB96HSBC"), AccountDetails(sortCode = "547896", accountNumber = "54966784"))
    assertThat(actual).isEqualTo(expected)
  }

  test("generate IBAN returns None if no prefix is supplied") {
    val actual = generateIBAN(None, AccountDetails(sortCode = "547896", accountNumber = "54966784"))
    assertThat(actual).isEqualTo(None)
  }

  test("flipYesNo flips yes") {
    assertThat(flipYesNo("yEs")).isEqualTo(Some(No))
  }

  test("flipYesNo flips no") {
    assertThat(flipYesNo("nO")).isEqualTo(Some(Yes))
  }

  test("flipYesNo returns none if the input is not yes or no") {
    assertThat(flipYesNo("something")).isEqualTo(None)
  }

  test("supportsBACS returns Yes for A") {
    assertThat(supportsBACS(Some("A"))).isEqualTo(Some(Yes))
  }

  test("supportsBACS returns Yes for M") {
    assertThat(supportsBACS(Some("M"))).isEqualTo(Some(Yes))
  }

  test("supportsBACS returns No for unexpected code") {
    assertThat(supportsBACS(Some("Z"))).isEqualTo(Some(No))
  }

  test("supportsBACS returns None if no status is supplied") {
    assertThat(supportsBACS(None)).isEqualTo(None)
  }

  test("convertDDIVoucherToAssessmentType returns Yes for Y") {
    assertThat(convertDDIVoucherToAssessmentType(Some("Y"))).isEqualTo(Some(Yes))
  }

  test("convertDDIVoucherToAssessmentType returns No for N") {
    assertThat(convertDDIVoucherToAssessmentType(Some("N"))).isEqualTo(Some(No))
  }

  test("convertDDIVoucherToAssessmentType returns None for unknown code") {
    assertThat(convertDDIVoucherToAssessmentType(Some("X"))).isEqualTo(None)
  }

  test("convertDDIVoucherToAssessmentType returns None for None") {
    assertThat(convertDDIVoucherToAssessmentType(None)).isEqualTo(None)
  }
}
