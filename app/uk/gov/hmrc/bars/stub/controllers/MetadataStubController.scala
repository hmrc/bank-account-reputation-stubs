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
import play.api.mvc.Results.{NotFound, Ok}
import play.api.mvc._
import uk.gov.hmrc.bars.stub
import uk.gov.hmrc.bars.stub.helpers.EISCDHelper
import uk.gov.hmrc.bars.stub.models.AssessmentType.{No, Yes}
import uk.gov.hmrc.bars.stub.models.EISCDData
import uk.gov.hmrc.bars.stub.models.components.{EISCDAddress, EISCDCountry}

import javax.inject.Inject
import scala.collection.mutable.ListBuffer

class MetadataStubController @Inject()(branchData: List[EISCDData], cc: ControllerComponents) {

  def metadata(sortCode: String): Action[AnyContent] = cc.actionBuilder {
    val eiscdData = EISCDHelper.getDataFor(sortCode, branchData)
    eiscdData.sortCodeIsPresent match {
      case `Yes` =>
        buildMetaDataResponse(eiscdData.data)
      case `No` =>
        NotFound("")
    }
  }

  def buildMetaDataResponse(data: EISCDData): Result = {
    val response = Json.toJson(stub.models.response.MetaData(
      sortCode = data.sortCode,
      bankName = data.bankName.get,
      bankCode = data.bankCode,
      address = EISCDAddress(
        lines = List(data.addressLine.get),
        town = data.town,
        postCode = data.postcode,
        country = Some(EISCDCountry(data.country.get))
      ),
      telephone = data.telephone,
      bacsOfficeStatus = data.bacsStatus.get,
      chapsSterlingStatus = data.chapsStatus.get,
      branchName = data.bankName,
      ddiVoucherFlag = data.ddiVoucher,
      disallowedTransactions = buildDisallowedTransactionsList(data.supportsDirectDebit, data.supportsDirectCredit)
    ))
    Ok(response)
  }

  def buildDisallowedTransactionsList(supportsDirectDebit: String, supportsDirectCredit: String): Seq[String] = {
    val disallowedTransactions = new ListBuffer[String]()
    if (supportsDirectCredit == No) {
      disallowedTransactions += "DC"
    }
    if (supportsDirectDebit == No) {
      disallowedTransactions += "DR"
      disallowedTransactions += "AU"
    }
    disallowedTransactions.toList
  }
}
