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
import uk.gov.hmrc.bars.stub.models.AssessmentType.{Indeterminate, No, Yes}
import uk.gov.hmrc.bars.stub.models.components.{Account, AccountDetails}
import uk.gov.hmrc.bars.stub.models.response.ValidateResponseV3
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class ValidateV3StubControllerTest extends AnyFunSuite {
  private final val DEFAULT_TEST_HEADER: (String, String) = "X-LOCALHOST-Origin" -> "test"
  private val env = Environment.simple()
  private val appConfig = new AppConfig(env, Configuration.load(env))
  private val validateStubController = new ValidateStubController(appConfig.loadStubbedBusinessBankAccountData, appConfig.loadStubbedPersonalBankAccountData, appConfig.loadStubbedEISCDData, appConfig.sortCodeDenyList, stubMessagesControllerComponents())

  test("valid personal account check") {
    val requestData = Account(AccountDetails("207106", "44377677"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = Yes,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Some(Yes),
      sortCodeSupportsDirectCredit = Some(Yes),
      iban = Some(s"GB21BARC${requestData.account.sortCode}${requestData.account.accountNumber}"),
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("indeterminate personal account check") {
    val requestData = Account(AccountDetails("222222", "54648979"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = Indeterminate,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = No
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("invalid account check sort code not on EISCD") {
    val requestData = Account(AccountDetails("123456", "66487236"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = No,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = No
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("valid business account check") {
    val requestData = Account(AccountDetails("207106", "86473611"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = Yes,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Some(Yes),
      sortCodeSupportsDirectCredit = Some(Yes),
      iban = Some(s"GB21BARC${requestData.account.sortCode}${requestData.account.accountNumber}"),
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("indeterminate business account check") {
    val requestData = Account(AccountDetails("222222", "76523611"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = Indeterminate,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = No
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("valid personal account check - DD not supported") {
    val requestData = Account(AccountDetails("609593", "95311500"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = Yes,
      nonStandardAccountDetailsRequiredForBacs = Yes,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Some(No),
      sortCodeSupportsDirectCredit = Some(Yes),
      sortCodeBankName = Some("Nottingham Building Society"),
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("valid personal account check - DC not supported") {
    val requestData = Account(AccountDetails("207102", "44355655"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = Yes,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Some(Yes),
      sortCodeSupportsDirectCredit = Some(No),
      iban = Some(s"GB21BARC${requestData.account.sortCode}${requestData.account.accountNumber}"),
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("valid personal account check - DD and DC not supported") {
    val requestData = Account(AccountDetails("406252", "54344611"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = Indeterminate,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Some(No),
      sortCodeSupportsDirectCredit = Some(No),
      iban = Some(s"FTSBGB2L${requestData.account.sortCode}${requestData.account.accountNumber}"),
      sortCodeBankName = Some("ABN AMRO BANK N.V"),
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("account fails modcheck") {
    val requestData = Account(AccountDetails("207106", "11111111"))
    val expectedResult = ValidateResponseV3(
      accountNumberIsWellFormatted = No,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Some(Yes),
      sortCodeSupportsDirectCredit = Some(Yes),
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validateV3.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[ValidateResponseV3]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

}
