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
import uk.gov.hmrc.bars.stub.helpers._
import uk.gov.hmrc.bars.stub.models.components.{AccountDetails, Address, Business}
import uk.gov.hmrc.bars.stub.models.request.BusinessRequest
import uk.gov.hmrc.bars.stub.models.response.BadRequestResponse
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class BusinessAssessV2ValidationTest extends AnyFunSuite {

  private final val DEFAULT_TEST_HEADER: (String, String) = "X-LOCALHOST-Origin" -> "test"
  private val env = Environment.simple()
  private val appConfig = new AppConfig(env, Configuration.load(env))
  private val businessAssessStubController = new BusinessAssessStubController(appConfig.loadStubbedBusinessBankAccountData, appConfig.loadStubbedEISCDData, appConfig.sortCodeDenyList, stubMessagesControllerComponents())

  test("Invalid account number throws exception") {
    val accountDetails = AccountDetails("207106", "87236")
    val expectedResponse = BadRequestResponse(
      code = INVALID_ACCOUNT_NUMBER.toString,
      desc = INVALID_ACCOUNT_NUMBER.generateErrorMessage(Some(accountDetails.accountNumber))
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails)))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }

  test("Invalid sort code throws exception") {
    val accountDetails = AccountDetails("7106", "87236342")
    val expectedResponse = BadRequestResponse(
      code = INVALID_SORTCODE.toString,
      desc = INVALID_SORTCODE.generateErrorMessage(Some(accountDetails.sortCode))
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails)))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }

  test("business address with no lines throws exception") {
    val accountDetails = AccountDetails("207106", "87236342")
    val business = Business(
      companyName = "name",
      address = Some(Address(
        lines = List.empty,
        town = None,
        postcode = None
      )),
      Some("123456789"))
    val expectedResponse = BadRequestResponse(
      code = NO_ADDRESS_LINES.toString,
      desc = NO_ADDRESS_LINES.generateErrorMessage()
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails, Some(business))))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }

  test("business address with too many lines throws exception") {
    val accountDetails = AccountDetails("207106", "87236342")
    val business = Business(
      companyName = "name",
      address = Some(Address(
        lines = List("one", "two", "three", "four", "five"),
        town = None,
        postcode = None
      )),
      Some("123456789"))
    val expectedResponse = BadRequestResponse(
      code = EXCESS_ADDRESS_LINES.toString,
      desc = EXCESS_ADDRESS_LINES.generateErrorMessage()
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails, Some(business))))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }

  test("business address with empty lines throws exception") {
    val accountDetails = AccountDetails("207106", "87236342")
    val business = Business(
      companyName = "name",
      address = Some(Address(
        lines = List("", ""),
        town = None,
        postcode = None
      )),
      Some("123456789"))
    val expectedResponse = BadRequestResponse(
      code = BLANK_ADDRESS.toString,
      desc = BLANK_ADDRESS.generateErrorMessage()
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails, Some(business))))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }

  test("business address with lines that total more than 140 chars throws exception") {
    val accountDetails = AccountDetails("207106", "87236342")
    val business = Business(
      companyName = "name",
      address = Some(Address(
        lines = List(
          "123456789 123456789 123456789 123456789 123456789 ",
          "123456789 123456789 123456789 123456789 123456789 ",
          "123456789 123456789 123456789 123456789 1"
        ),
        town = None,
        postcode = None
      )),
      Some("123456789"))
    val expectedResponse = BadRequestResponse(
      code = EXCESS_ADDRESS_LENGTH.toString,
      desc = EXCESS_ADDRESS_LENGTH.generateErrorMessage()
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails, Some(business))))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }

  test("business address with blank town throws exception") {
    val accountDetails = AccountDetails("207106", "87236342")
    val business = Business(
      companyName = "name",
      address = Some(Address(
        lines = List("1 Test Street"),
        town = Some(""),
        postcode = None
      )),
      Some("123456789"))
    val expectedResponse = BadRequestResponse(
      code = BLANK_TOWN.toString,
      desc = BLANK_TOWN.generateErrorMessage()
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails, Some(business))))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }

  test("business address with no town throws exception") {
    val accountDetails = AccountDetails("207106", "87236342")
    val business = Business(
      companyName = "name",
      address = Some(Address(
        lines = List("1 Test Street"),
        town = None,
        postcode = None
      )),
      Some("123456789"))
    val expectedResponse = BadRequestResponse(
      code = BLANK_TOWN.toString,
      desc = BLANK_TOWN.generateErrorMessage()
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails, Some(business))))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }

  test("business address with town that is too long throws exception") {
    val accountDetails = AccountDetails("207106", "87236342")
    val business = Business(
      companyName = "name",
      address = Some(Address(
        lines = List("1 Test Street"),
        town = Some("123456789 123456789 123456789 123456"),
        postcode = None
      )),
      Some("123456789"))
    val expectedResponse = BadRequestResponse(
      code = EXCESS_TOWN_LENGTH.toString,
      desc = EXCESS_TOWN_LENGTH.generateErrorMessage()
    )
    val fakeRequest = FakeRequest(method = "POST", path = s"/business/v2/assess")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(BusinessRequest(accountDetails, Some(business))))

    val result: Future[Result] = businessAssessStubController.assess.apply(fakeRequest)
    val initResponse = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(initResponse).isEqualTo(expectedResponse)
  }
}
