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

package uk.gov.hmrc.bars.stub.controllers

import akka.util.Timeout
import org.assertj.core.api.Assertions.assertThat
import org.scalatest.funsuite.AnyFunSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsJson
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bars.stub.AppConfig
import uk.gov.hmrc.bars.stub.models.AssessmentType._
import uk.gov.hmrc.bars.stub.models.components.{AccountDetails, Business}
import uk.gov.hmrc.bars.stub.models.request.BusinessRequest
import uk.gov.hmrc.bars.stub.models.response.BusinessAssessmentV3
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class BusinessAssessV3StubControllerTest extends AnyFunSuite {
  private final val DEFAULT_TEST_HEADER: (String, String) = "X-LOCALHOST-Origin" -> "test"
  private val env = Environment.simple()
  private val appConfig = new AppConfig(env, Configuration.load(env))
  private val businessAssessStubController = new BusinessAssessStubController(appConfig.loadStubbedBusinessBankAccountData, appConfig.loadStubbedEISCDData, appConfig.sortCodeDenyList, stubMessagesControllerComponents())

  test("Invalid account with sort code on EISCD") {

    val expectedResult = BusinessAssessmentV3(
      accountNumberIsWellFormatted = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
      nonStandardAccountDetailsRequiredForBacs = No,
      accountExists = Indeterminate,
      nameMatches = Inapplicable,
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      iban = Some("GB21BARC20710666487236")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/business")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(AccountDetails("207106", "66487236"))))

    val result: Future[Result] = businessAssessStubController.assessV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BusinessAssessmentV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Invalid account, sort code not on EISCD") {

    val expectedResult = BusinessAssessmentV3(
      accountNumberIsWellFormatted = No,
      sortCodeIsPresentOnEISCD = No,
      nonStandardAccountDetailsRequiredForBacs = No,
      accountExists = Indeterminate,
      nameMatches = Inapplicable,
      sortCodeSupportsDirectDebit = No,
      sortCodeSupportsDirectCredit = No
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/business")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(AccountDetails("123456", "66487236"))))

    val result: Future[Result] = businessAssessStubController.assessV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BusinessAssessmentV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Valid account with sort code on EISCD") {
    val expectedResult = BusinessAssessmentV3(
      accountNumberIsWellFormatted = Yes,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
      nonStandardAccountDetailsRequiredForBacs = No,
      accountExists = Yes,
      nameMatches = Yes,
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      iban = Some("GB21BARC20710686473611")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/business")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(
        AccountDetails("207106", "86473611"),
        Some(Business(companyName = "Security Engima"))
      )))

    val result: Future[Result] = businessAssessStubController.assessV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BusinessAssessmentV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Valid building society account with sort code on EISCD") {
    val expectedResult = BusinessAssessmentV3(
      accountNumberIsWellFormatted = Yes,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeBankName = Some("Nottingham Building Society"),
      nonStandardAccountDetailsRequiredForBacs = Yes,
      accountExists = Yes,
      nameMatches = Yes,
      sortCodeSupportsDirectDebit = No,
      sortCodeSupportsDirectCredit = Yes
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/business")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(
        AccountDetails("609593", "96863604"),
        Some(Business(companyName = "O'Connor Construction"))
      )))

    val result: Future[Result] = businessAssessStubController.assessV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BusinessAssessmentV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Indeterminate account with sort code not EISCD") {
    val expectedResult = BusinessAssessmentV3(
      accountNumberIsWellFormatted = Indeterminate,
      sortCodeIsPresentOnEISCD = No,
      nonStandardAccountDetailsRequiredForBacs = No,
      accountExists = Inapplicable,
      nameMatches = Inapplicable,
      sortCodeSupportsDirectDebit = No,
      sortCodeSupportsDirectCredit = No
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/business")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(AccountDetails("222222", "76523611"))))

    val result: Future[Result] = businessAssessStubController.assessV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BusinessAssessmentV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Account is known but company name does not match") {
    val expectedResult = BusinessAssessmentV3(
      accountNumberIsWellFormatted = Yes,
      sortCodeIsPresentOnEISCD = Yes,
      nonStandardAccountDetailsRequiredForBacs = No,
      accountExists = Yes,
      nameMatches = No,
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
      iban = Some("GB21BARC20710676523611")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/business")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(
        AccountDetails("207106", "76523611"),
        Some(Business(companyName = "Not match"))
        )))

    val result: Future[Result] = businessAssessStubController.assessV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BusinessAssessmentV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Account fails mod check") {
    val expectedResult = BusinessAssessmentV3(
      accountNumberIsWellFormatted = No,
      sortCodeIsPresentOnEISCD = Yes,
      nonStandardAccountDetailsRequiredForBacs = Inapplicable,
      accountExists = Inapplicable,
      nameMatches = Inapplicable,
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/business")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(
        AccountDetails("207106", "11111111"),
        Some(Business(companyName = "Does not matter"))
      )))

    val result: Future[Result] = businessAssessStubController.assessV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BusinessAssessmentV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Account is known and name is a partial match") {
    val expectedResult = BusinessAssessmentV3(
      accountNumberIsWellFormatted = Yes,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
      nonStandardAccountDetailsRequiredForBacs = No,
      accountExists = Yes,
      nameMatches = Partial,
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      iban = Some("GB21BARC20710686473611"),
      accountName = Some("Security Engima")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/business")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(
        AccountDetails("207106", "86473611"),
        Some(Business(companyName = "Security"))
      )))

    val result: Future[Result] = businessAssessStubController.assessV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BusinessAssessmentV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }
}
