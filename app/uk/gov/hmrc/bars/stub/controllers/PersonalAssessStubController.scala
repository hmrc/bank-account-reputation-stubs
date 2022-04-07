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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.bars.stub.helpers.Data.{checkExactNameMatch, checkName, constructPersonalName}
import uk.gov.hmrc.bars.stub.helpers.EISCDHelper.generateIBAN
import uk.gov.hmrc.bars.stub.helpers.ValidationHelper.{validateAccount, validateAddress}
import uk.gov.hmrc.bars.stub.helpers.{EISCDHelper, ValidationException}
import uk.gov.hmrc.bars.stub.models.AssessmentType._
import uk.gov.hmrc.bars.stub.models.EISCDData
import uk.gov.hmrc.bars.stub.models.components.AccountDetails
import uk.gov.hmrc.bars.stub.models.request.PersonalRequest
import uk.gov.hmrc.bars.stub.models.response.{BadRequestResponse, PersonalAssessmentV3, PersonalAssessmentV4}
import uk.gov.hmrc.bars.stub.models.stubbed.PersonalData

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class PersonalAssessStubController @Inject()(personalAccountData: Map[AccountDetails, PersonalData], branchData: List[EISCDData], sortCodeDenyList: List[String], cc: ControllerComponents) {

  case class ResponseData(statusCode: Int, payload: JsValue)

  @Deprecated
  def assess: Action[AnyContent] = cc.actionBuilder.async {
    (request: Request[AnyContent]) =>
      val responseData = parseRequestAndReturnResponseData(Json.fromJson(request.body.asJson.get)(PersonalRequest.reads).get)
      Future.successful {
        Results.Status(responseData.statusCode)(responseData.payload)
      }
  }

  def assessV4: Action[AnyContent] = cc.actionBuilder.async {
    (request: Request[AnyContent]) =>
      val responseData = parseRequestAndReturnResponseData(Json.fromJson(request.body.asJson.get)(PersonalRequest.reads).get, Some("V4"))
      Future.successful {
        Results.Status(responseData.statusCode)(responseData.payload)
      }
  }

  def parseRequestAndReturnResponseData(body: PersonalRequest, version: Option[String] = Some("Default")): ResponseData = {
    val accountDetails = AccountDetails(body.account.sortCode, body.account.accountNumber)
    Try {
      validateAccount(accountDetails, sortCodeDenyList)
      if (body.subject.address.isDefined) {
        validateAddress(body.subject.address)
      }
    } match {
      case Failure(e: ValidationException) =>
        ResponseData(
          Status.BAD_REQUEST,
          Json.toJson(BadRequestResponse(code = e.ve.toString, desc = e.getMessage))
        )
      case Success(_) =>
        version.get match {
          case "V4" =>
            ResponseData(
              Status.OK,
              Json.toJson(searchStubbedDataForPersonalAccountV4(accountDetails, constructPersonalName(Some(body.subject))))
            )
          case _ =>
            ResponseData(
              Status.OK,
              Json.toJson(searchStubbedDataForPersonalAccount(accountDetails, constructPersonalName(Some(body.subject))))
            )
        }
    }
  }

  def searchStubbedDataForPersonalAccount(account: AccountDetails, givenName: String): PersonalAssessmentV3 = {
    val accountData: Option[PersonalData] = personalAccountData.get(account)
    accountData match {
      case None =>
        val branch = EISCDHelper.getDataFor(account.sortCode, branchData)
        PersonalAssessmentV3(
          accountNumberWithSortCodeIsValid = Indeterminate,
          accountExists = Inapplicable,
          nameMatches = Inapplicable,
          addressMatches = Inapplicable,
          nonConsented = Inapplicable,
          subjectHasDeceased = Inapplicable,
          nonStandardAccountDetailsRequiredForBacs = branch.data.nonStandardBacsDataRequired,
          sortCodeIsPresentOnEISCD = branch.sortCodeIsPresent,
          sortCodeBankName = branch.data.bankName,
          sortCodeSupportsDirectDebit = branch.data.supportsDirectDebit,
          sortCodeSupportsDirectCredit = branch.data.supportsDirectCredit
        )
      case _ =>
        PersonalAssessmentV3(
          accountNumberWithSortCodeIsValid = accountData.get.accountNumberWithSortCodeIsValid,
          accountExists = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.accountExists,
          nameMatches = if(accountData.get.accountExists == Inapplicable || accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else checkExactNameMatch(givenName, accountData.get.name),
          addressMatches = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.addressMatches,
          nonConsented = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.nonConsented,
          subjectHasDeceased = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.subjectHasDeceased,
          nonStandardAccountDetailsRequiredForBacs = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.nonStandardAccountDetailsRequiredForBacs,
          sortCodeIsPresentOnEISCD = accountData.get.sortCodeIsPresentOnEISCD,
          sortCodeBankName = accountData.get.sortCodeBankName,
          sortCodeSupportsDirectDebit = accountData.get.sortCodeSupportsDirectDebit,
          sortCodeSupportsDirectCredit = accountData.get.sortCodeSupportsDirectCredit
        )
    }
  }

  def searchStubbedDataForPersonalAccountV4(account: AccountDetails, givenName: String): PersonalAssessmentV4 = {
    val accountData: Option[PersonalData] = personalAccountData.get(account)
    val branch = EISCDHelper.getDataFor(account.sortCode, branchData)
    accountData match {
      case None =>
        PersonalAssessmentV4(
          accountNumberIsWellFormatted = Indeterminate,
          accountExists = Inapplicable,
          nameMatches = Inapplicable,
          nonStandardAccountDetailsRequiredForBacs = branch.data.nonStandardBacsDataRequired,
          sortCodeIsPresentOnEISCD = branch.sortCodeIsPresent,
          sortCodeBankName = branch.data.bankName,
          sortCodeSupportsDirectDebit = branch.data.supportsDirectDebit,
          sortCodeSupportsDirectCredit = branch.data.supportsDirectCredit,
          iban = generateIBAN(branch.data.ibanPrefix, account)
        )
      case _ =>
        PersonalAssessmentV4(
          accountNumberIsWellFormatted = accountData.get.accountNumberWithSortCodeIsValid,
          accountExists = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.accountExists,
          nameMatches = if(accountData.get.accountExists == Inapplicable || accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else checkName(givenName, accountData.get.name),
          nonStandardAccountDetailsRequiredForBacs = if (accountData.get.accountNumberWithSortCodeIsValid == No) Inapplicable else accountData.get.nonStandardAccountDetailsRequiredForBacs,
          sortCodeIsPresentOnEISCD = accountData.get.sortCodeIsPresentOnEISCD,
          sortCodeBankName = accountData.get.sortCodeBankName,
          sortCodeSupportsDirectDebit = accountData.get.sortCodeSupportsDirectDebit,
          sortCodeSupportsDirectCredit = accountData.get.sortCodeSupportsDirectCredit,
          iban = if (accountData.get.accountNumberWithSortCodeIsValid == No) None else generateIBAN(branch.data.ibanPrefix, account),
          accountName = if (checkName(givenName, accountData.get.name) == Partial) Some(accountData.get.name) else None
        )
    }
  }
}
