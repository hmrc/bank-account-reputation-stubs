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
import uk.gov.hmrc.bars.stub.helpers.{INVALID_ACCOUNT_NUMBER, INVALID_SORTCODE}
import uk.gov.hmrc.bars.stub.models.components.{Account, AccountDetails}
import uk.gov.hmrc.bars.stub.models.response.BadRequestResponse
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class ValidateV3StubControllerValidateTest extends AnyFunSuite {
  private final val DEFAULT_TEST_HEADER: (String, String) = "X-LOCALHOST-Origin" -> "test"
  private val env = Environment.simple()
  private val appConfig = new AppConfig(env, Configuration.load(env))
  private val validateStubController = new ValidateStubController(appConfig.loadStubbedBusinessBankAccountData, appConfig.loadStubbedPersonalBankAccountData, appConfig.loadStubbedEISCDData, appConfig.sortCodeDenyList, stubMessagesControllerComponents())

  test("Invalid account number throws exception") {
    val requestData = Account(AccountDetails("207106", "87236"))
    val expectedResponse = BadRequestResponse(
      code = INVALID_ACCOUNT_NUMBER.toString,
      desc = INVALID_ACCOUNT_NUMBER.generateErrorMessage(Some(requestData.account.accountNumber))
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validate.apply(fakeRequest)
    val response = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(response).isEqualTo(expectedResponse)
  }

  test("Invalid sort code throws exception") {
    val requestData = Account(AccountDetails("7106", "87236342"))
    val expectedResponse = BadRequestResponse(
      code = INVALID_SORTCODE.toString,
      desc = INVALID_SORTCODE.generateErrorMessage(Some(requestData.account.sortCode))
    )

    val fakeRequest = FakeRequest(method = "POST", path = s"/validate/bank-details")
      .withHeaders(DEFAULT_TEST_HEADER)
      .withJsonBody(Json.toJson(requestData))

    val result: Future[Result] = validateStubController.validate.apply(fakeRequest)
    val response = contentAsJson(result)(Timeout.zero).as[BadRequestResponse]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.BAD_REQUEST)
    assertThat(response).isEqualTo(expectedResponse)
  }

}
