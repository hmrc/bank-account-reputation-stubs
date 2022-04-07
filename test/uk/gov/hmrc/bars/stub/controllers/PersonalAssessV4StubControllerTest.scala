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
import uk.gov.hmrc.bars.stub.models.AssessmentType._
import uk.gov.hmrc.bars.stub.models.components.{AccountDetails, Address, Subject}
import uk.gov.hmrc.bars.stub.models.request.PersonalRequest
import uk.gov.hmrc.bars.stub.models.response.PersonalAssessmentV4
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class PersonalAssessV4StubControllerTest extends AnyFunSuite {
  private final val DEFAULT_TEST_HEADER: (String, String) = "X-LOCALHOST-Origin" -> "test"
  private val env = Environment.simple()
  private val appConfig = new AppConfig(env, Configuration.load(env))
  private val personalAssessStubController = new PersonalAssessStubController(appConfig.loadStubbedPersonalBankAccountData, appConfig.loadStubbedEISCDData, appConfig.sortCodeDenyList, stubMessagesControllerComponents())

  test("Unknown account with sort code on EISCD") {

    val expectedResult = PersonalAssessmentV4(
      accountNumberIsWellFormatted = Indeterminate,
      accountExists = Inapplicable,
      nameMatches = Inapplicable,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      iban = Some("GB21BARC20710666487236")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/personal")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(PersonalRequest(
        AccountDetails("207106", "66487236"),
        Subject(
          name = Some("Mrs Jean Jones-Butler"),
          address = Some(Address(lines = List("1 Test Town"), town = Some("Testville"), postcode = Some("TS33 1ST")))
        ))))

    val result: Future[Result] = personalAssessStubController.assessV4.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[PersonalAssessmentV4]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Unknown account, sort code not on EISCD") {

    val expectedResult = PersonalAssessmentV4(
      accountNumberIsWellFormatted = Indeterminate,
      accountExists = Inapplicable,
      nameMatches = Inapplicable,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = No,
      sortCodeSupportsDirectDebit = No,
      sortCodeSupportsDirectCredit = No,
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/personal")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(PersonalRequest(
        AccountDetails("123456", "66487236"),
        Subject(
          name = Some("Mrs Jean Jones-Butler"),
          address = Some(Address(lines = List("1 Test Town"), town = Some("Testville"), postcode = Some("TS33 1ST")))
        ))))

    val result: Future[Result] = personalAssessStubController.assessV4.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[PersonalAssessmentV4]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Valid account with sort code on EISCD") {
    val expectedResult = PersonalAssessmentV4(
      accountNumberIsWellFormatted = Yes,
      accountExists = Yes,
      nameMatches = Yes,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      iban = Some("GB21BARC20710644344677")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/personal")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(PersonalRequest(
        AccountDetails("207106", "44344677"),
        Subject(
          name = Some("Felipa Doherty"),
          address = Some(Address(lines = List("1 Test Town"), town = Some("Testville"), postcode = Some("TS33 1ST")))
        ))))

    val result: Future[Result] = personalAssessStubController.assessV4.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[PersonalAssessmentV4]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Valid building society account with sort code on EISCD") {
    val expectedResult = PersonalAssessmentV4(
      accountNumberIsWellFormatted = Yes,
      accountExists = Yes,
      nameMatches = Yes,
      nonStandardAccountDetailsRequiredForBacs = Yes,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeBankName = Some("Nottingham Building Society"),
      sortCodeSupportsDirectDebit = No,
      sortCodeSupportsDirectCredit = Yes
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/personal")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(PersonalRequest(
        AccountDetails("609593", "95311500"),
        Subject(
          name = Some("Angelique Whitney"),
          address = Some(Address(lines = List("1 Test Town"), town = Some("Testville"), postcode = Some("TS33 1ST")))
        ))))

    val result: Future[Result] = personalAssessStubController.assessV4.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[PersonalAssessmentV4]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Indeterminate account with sort code not EISCD") {
    val expectedResult = PersonalAssessmentV4(
      accountNumberIsWellFormatted = Indeterminate,
      accountExists = Inapplicable,
      nameMatches = Inapplicable,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = No,
      sortCodeSupportsDirectDebit = No,
      sortCodeSupportsDirectCredit = No
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/personal")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(PersonalRequest(
        AccountDetails("222222", "54648979"),
        Subject(
          name = Some("Mrs Jean Jones-Butler"),
          address = Some(Address(lines = List("1 Test Town"), town = Some("Testville"), postcode = Some("TS33 1ST")))
        ))))

    val result: Future[Result] = personalAssessStubController.assessV4.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[PersonalAssessmentV4]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Account is known but name does not match") {
    val expectedResult = PersonalAssessmentV4(
      accountNumberIsWellFormatted = Yes,
      accountExists = Yes,
      nameMatches = No,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
      iban = Some("GB21BARC20710644311677")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/personal")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(PersonalRequest(
        AccountDetails("207106", "44311677"),
        Subject(
          name = Some("Wrong Name"),
          address = Some(Address(lines = List("1 Test Town"), town = Some("Testville"), postcode = Some("TS33 1ST")))
        ))))

    val result: Future[Result] = personalAssessStubController.assessV4.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[PersonalAssessmentV4]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Account is known and name is a partial match") {
    val expectedResult = PersonalAssessmentV4(
      accountNumberIsWellFormatted = Yes,
      accountExists = Yes,
      nameMatches = Partial,
      nonStandardAccountDetailsRequiredForBacs = No,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
      iban = Some("GB21BARC20710644344677"),
      accountName = Some("Felipa Doherty")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/personal")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(PersonalRequest(
        AccountDetails("207106", "44344677"),
        Subject(
          name = Some("Felipa")
        ))))

    val result: Future[Result] = personalAssessStubController.assessV4.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[PersonalAssessmentV4]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }

  test("Account fails mod check") {
    val expectedResult = PersonalAssessmentV4(
      accountNumberIsWellFormatted = No,
      accountExists = Inapplicable,
      nameMatches = Inapplicable,
      nonStandardAccountDetailsRequiredForBacs = Inapplicable,
      sortCodeIsPresentOnEISCD = Yes,
      sortCodeSupportsDirectDebit = Yes,
      sortCodeSupportsDirectCredit = Yes,
      sortCodeBankName = Some("BARCLAYS BANK UK PLC")
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/verify/personal")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(PersonalRequest(
        AccountDetails("207106", "11111111"),
        Subject(name = Some("Does not matter"))
      )))

    val result: Future[Result] = personalAssessStubController.assessV4.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[PersonalAssessmentV4]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(initResponse).isEqualTo(expectedResult)
  }
}
