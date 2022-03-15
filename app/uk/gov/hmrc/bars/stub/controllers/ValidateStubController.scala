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

import play.api.libs.json.Json
import play.api.mvc.Results.{BadRequest, Ok}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import uk.gov.hmrc.bars.stub.helpers.EISCDHelper.{convertDDIVoucherToAssessmentType, flipYesNo, generateIBAN, supportsBACS}
import uk.gov.hmrc.bars.stub.helpers.ValidationHelper.validateAccount
import uk.gov.hmrc.bars.stub.helpers.{EISCDHelper, ValidationException}
import uk.gov.hmrc.bars.stub.models.AssessmentType.{No, Yes}
import uk.gov.hmrc.bars.stub.models.EISCDData
import uk.gov.hmrc.bars.stub.models.components.{Account, AccountDetails}
import uk.gov.hmrc.bars.stub.models.response.{BadRequestResponse, ValidateResponseV2, ValidateResponseV3}
import uk.gov.hmrc.bars.stub.models.stubbed.{BusinessData, PersonalData}

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ValidateStubController @Inject()(businessAccountData: Map[AccountDetails, BusinessData], personalAccountData: Map[AccountDetails, PersonalData], branchData: List[EISCDData], sortCodeDenyList: List[String], cc: ControllerComponents) {

  @Deprecated
  def validate: Action[AnyContent] = cc.actionBuilder.async {
    (request: Request[AnyContent]) =>
      val body: Account = Json.fromJson(request.body.asJson.get)(Account.reads).get
      val branch = EISCDHelper.getDataFor(body.account.sortCode, branchData)

      Try {
        validateAccount(body.account, sortCodeDenyList)
      } match {
        case Failure(e: ValidationException) =>
          Future.successful {
            BadRequest(Json.toJson(BadRequestResponse(
              code = e.ve.toString,
              desc = e.getMessage
            )))
          }
        case Success(_) =>
          val directDebitSupport = if (branch.sortCodeIsPresent == Yes)
            flipYesNo(branch.data.supportsDirectDebit) else None
          val wellFormatted = isAccountValid(body.account)

          Future.successful {
            Ok(Json.toJson(ValidateResponseV2(
              accountNumberWithSortCodeIsValid = wellFormatted,
              nonStandardAccountDetailsRequiredForBacs = branch.data.nonStandardBacsDataRequired,
              sortCodeIsPresentOnEISCD = branch.sortCodeIsPresent,
              supportsBACS = supportsBACS(branch.data.bacsStatus),
              ddiVoucherFlag = convertDDIVoucherToAssessmentType(branch.data.ddiVoucher),
              directDebitsDisallowed = directDebitSupport,
              directDebitInstructionsDisallowed = directDebitSupport,
              iban = if (wellFormatted == No) None else generateIBAN(branch.data.ibanPrefix, body.account),
              sortCodeBankName = branch.data.bankName
            )))
          }
      }
  }

  def validateV3: Action[AnyContent] = cc.actionBuilder.async {
    (request: Request[AnyContent]) =>
      val body: Account = Json.fromJson(request.body.asJson.get)(Account.reads).get
      val branch = EISCDHelper.getDataFor(body.account.sortCode, branchData)

      Try {
        validateAccount(body.account, sortCodeDenyList)
      } match {
        case Failure(e: ValidationException) =>
          Future.successful {
            BadRequest(Json.toJson(BadRequestResponse(
              code = e.ve.toString,
              desc = e.getMessage
            )))
          }
        case Success(_) =>
          val wellFormatted = isAccountValid(body.account)

          Future.successful {
            Ok(Json.toJson(ValidateResponseV3(
              accountNumberIsWellFormatted = wellFormatted,
              nonStandardAccountDetailsRequiredForBacs = branch.data.nonStandardBacsDataRequired,
              sortCodeIsPresentOnEISCD = branch.sortCodeIsPresent,
              sortCodeSupportsDirectDebit = if (branch.sortCodeIsPresent.equals(Yes)) {
                Some(branch.data.supportsDirectDebit)
              } else None,
              sortCodeSupportsDirectCredit = if (branch.sortCodeIsPresent.equals(Yes)) {
                Some(branch.data.supportsDirectCredit)
              } else None,
              iban = if (wellFormatted == No) None else generateIBAN(branch.data.ibanPrefix, body.account),
              sortCodeBankName = branch.data.bankName
            )))
          }
      }
  }

  def isAccountValid(account: AccountDetails): String = {
    val busAccountData = businessAccountData.get(account)
    busAccountData match {
      case None =>
        val persAccountData = personalAccountData.get(account)
        persAccountData match {
          case None => No
          case _ => persAccountData.get.accountNumberWithSortCodeIsValid
        }
      case _ => busAccountData.get.accountNumberWithSortCodeIsValid
    }
  }

}
