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
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsJson
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bars.stub.AppConfig
import uk.gov.hmrc.bars.stub.models.components.{EISCDAddress, EISCDCountry}
import uk.gov.hmrc.bars.stub.models.response.MetaData
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class MetadataStubControllerTest extends AnyFunSuite {
  private final val DEFAULT_TEST_HEADER: (String, String) = "X-LOCALHOST-Origin" -> "test"
  private val env = Environment.simple()
  private val appConfig = new AppConfig(env, Configuration.load(env))
  private val metadataStubController = new MetadataStubController(appConfig.loadStubbedEISCDData, stubMessagesControllerComponents())

  test("Able to retrieve metadata") {

    val expectedResponse = MetaData(
      sortCode = "206705",
      bankName = "BARCLAYS BANK UK PLC",
      bankCode = Some("0934"),
      address = EISCDAddress(
        lines = List("PO Box 424"),
        town = Some("Coventry"),
        postCode = Some("CV2 2BR"),
        country = Some(EISCDCountry("UNITED KINGDOM"))
      ),
      telephone = None,
      bacsOfficeStatus = "M",
      chapsSterlingStatus = "I",
      branchName = Some("BARCLAYS BANK UK PLC"),
      ddiVoucherFlag = Some("N"),
      disallowedTransactions = List("DC", "DR", "AU")
    )

    val fakeRequest = FakeRequest(method = "GET", path = s"/metadata/${expectedResponse.sortCode}")
      .withHeaders(DEFAULT_TEST_HEADER)

    val result: Future[Result] = metadataStubController.metadata(expectedResponse.sortCode).apply(fakeRequest)
    val response = contentAsJson(result)(Timeout.zero).as[MetaData]

    assertThat(result.value.get.get.header.status).isEqualTo(Status.OK)
    assertThat(response).isEqualTo(expectedResponse)
  }

  test("Unknown sort code returns 404") {
    val unknownSortCode = "123456"
    val fakeRequest = FakeRequest(method = "GET", path = s"/metadata/$unknownSortCode")
      .withHeaders(DEFAULT_TEST_HEADER)

    val result: Future[Result] = metadataStubController.metadata(unknownSortCode).apply(fakeRequest)

    assertThat(result.value.get.get.header.status).isEqualTo(Status.NOT_FOUND)
  }
}
