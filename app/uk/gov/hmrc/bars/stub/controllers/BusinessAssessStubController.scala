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

import play.api.http.Status
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.bars.stub.helpers.Data.checkBusinessName
import uk.gov.hmrc.bars.stub.helpers.EISCDHelper.generateIBAN
import uk.gov.hmrc.bars.stub.helpers.ValidationHelper.{validateAccount, validateAddress}
import uk.gov.hmrc.bars.stub.helpers._
import uk.gov.hmrc.bars.stub.models.AssessmentType.{Inapplicable, Indeterminate, No}
import uk.gov.hmrc.bars.stub.models.{EISCDData, _}
import uk.gov.hmrc.bars.stub.models.components.{AccountDetails, Business}
import uk.gov.hmrc.bars.stub.models.request.BusinessRequest
import uk.gov.hmrc.bars.stub.models.response.{BadRequestResponse, BusinessAssessmentV2, BusinessAssessmentV3}
import uk.gov.hmrc.bars.stub.models.stubbed.BusinessData

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


class BusinessAssessStubController @Inject()(businessAccountData: Map[AccountDetails, BusinessData], branchData: List[EISCDData], sortCodeDenyList: List[String], cc: ControllerComponents) {

  case class ResponseData(statusCode: Int, payload: JsValue)

  @Deprecated
  def assess: Action[AnyContent] = cc.actionBuilder.async {
    (request: Request[AnyContent]) =>
      val responseData = parseRequestAndReturnResponseData(Json.fromJson(request.body.asJson.get)(BusinessRequest.reads).get)
      Future.successful {
        Results.Status(responseData.statusCode)(responseData.payload)
      }
  }

  def assessV3: Action[AnyContent] = cc.actionBuilder.async {
    (request: Request[AnyContent]) =>
      val responseData = parseRequestAndReturnResponseData(Json.fromJson(request.body.asJson.get)(BusinessRequest.reads).get, Some("V3"))
      Future.successful {
        Results.Status(responseData.statusCode)(responseData.payload)
      }
  }

  def parseRequestAndReturnResponseData(body: BusinessRequest, version: Option[String] = Some("Default")): ResponseData = {
    val accountDetails = AccountDetails(body.account.sortCode, body.account.accountNumber)
    Try {
      validateAccount(accountDetails, sortCodeDenyList)
      if (body.business.isDefined) {
        validateAddress(body.business.get.address)
      }
    } match {
      case Failure(e: ValidationException) =>
        ResponseData(
          Status.BAD_REQUEST,
          Json.toJson(BadRequestResponse(code = e.ve.toString, desc = e.getMessage))
        )
      case Success(_) =>
        version.get match {
          case "V3" =>
            ResponseData(
              Status.OK,
              Json.toJson(searchStubbedDataForBusinessAccountV3(accountDetails, body.business))
            )
          case _ =>
            ResponseData(
              Status.OK,
              Json.toJson(searchStubbedDataForBusinessAccount(accountDetails, body.business))
            )
        }
    }
  }

  def searchStubbedDataForBusinessAccount(account: AccountDetails, business: Option[Business]): BusinessAssessmentV2 = {
    val accountData: Option[BusinessData] = businessAccountData.get(account)
    accountData match {
      case None =>
        val branch = EISCDHelper.getDataFor(account.sortCode, branchData)
        BusinessAssessmentV2(
          accountNumberWithSortCodeIsValid = No,
          sortCodeIsPresentOnEISCD = branch.sortCodeIsPresent,
          sortCodeBankName = branch.data.bankName,
          nonStandardAccountDetailsRequiredForBacs = branch.data.nonStandardBacsDataRequired,
          accountExists = Indeterminate,
          companyNameMatches = Inapplicable,
          companyPostCodeMatches = Inapplicable,
          companyRegistrationNumberMatches = Inapplicable,
          sortCodeSupportsDirectDebit = branch.data.supportsDirectDebit,
          sortCodeSupportsDirectCredit = branch.data.supportsDirectCredit
        )
      case _ =>
        BusinessAssessmentV2(
          accountNumberWithSortCodeIsValid = accountData.get.accountNumberWithSortCodeIsValid,
          sortCodeIsPresentOnEISCD = accountData.get.sortCodeIsPresentOnEISCD,
          sortCodeBankName = accountData.get.sortCodeBankName,
          nonStandardAccountDetailsRequiredForBacs = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.nonStandardAccountDetailsRequiredForBacs,
          accountExists = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.accountExists,
          companyNameMatches = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else checkBusinessName(accountData.get.companyName, business),
          companyPostCodeMatches = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.companyPostCodeMatches,
          companyRegistrationNumberMatches = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.companyRegistrationNumberMatches,
          sortCodeSupportsDirectDebit = accountData.get.sortCodeSupportsDirectDebit,
          sortCodeSupportsDirectCredit = accountData.get.sortCodeSupportsDirectCredit
        )
    }
  }

  def searchStubbedDataForBusinessAccountV3(account: AccountDetails, business: Option[Business]): BusinessAssessmentV3 = {
    val accountData: Option[BusinessData] = businessAccountData.get(account)
    val branch = EISCDHelper.getDataFor(account.sortCode, branchData)
    accountData match {
      case None =>
        BusinessAssessmentV3(
          accountNumberIsWellFormatted = No,
          sortCodeIsPresentOnEISCD = branch.sortCodeIsPresent,
          sortCodeBankName = branch.data.bankName,
          nonStandardAccountDetailsRequiredForBacs = branch.data.nonStandardBacsDataRequired,
          accountExists = Indeterminate,
          nameMatches = Inapplicable,
          sortCodeSupportsDirectDebit = branch.data.supportsDirectDebit,
          sortCodeSupportsDirectCredit = branch.data.supportsDirectCredit,
          iban = generateIBAN(branch.data.ibanPrefix, account)
        )
      case _ =>
        BusinessAssessmentV3(
          accountNumberIsWellFormatted = accountData.get.accountNumberWithSortCodeIsValid,
          sortCodeIsPresentOnEISCD = accountData.get.sortCodeIsPresentOnEISCD,
          sortCodeBankName = accountData.get.sortCodeBankName,
          nonStandardAccountDetailsRequiredForBacs = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.nonStandardAccountDetailsRequiredForBacs,
          accountExists = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.accountExists,
          nameMatches = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else checkBusinessName(accountData.get.companyName, business),
          sortCodeSupportsDirectDebit = accountData.get.sortCodeSupportsDirectDebit,
          sortCodeSupportsDirectCredit = accountData.get.sortCodeSupportsDirectCredit,
          iban = if (accountData.get.accountNumberWithSortCodeIsValid == No) None else generateIBAN(branch.data.ibanPrefix, account)
        )
    }
  }
}
