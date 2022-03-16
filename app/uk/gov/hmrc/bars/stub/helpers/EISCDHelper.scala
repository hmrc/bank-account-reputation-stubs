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

package uk.gov.hmrc.bars.stub.helpers

import uk.gov.hmrc.bars.stub.models.AssessmentType.{No, Yes}
import uk.gov.hmrc.bars.stub.models.components.AccountDetails
import uk.gov.hmrc.bars.stub.models.{EISCD, EISCDData}

object EISCDHelper {

  def getDataFor(sortCode: String, knownSortCodes: List[EISCDData]): EISCD = {
    val sortCodeData = knownSortCodes.filter(bankData => bankData.sortCode == sortCode)
    if (sortCodeData.isEmpty) {
      EISCD(sortCodeIsPresent = No, EISCDData(sortCode))
    } else {
      EISCD(sortCodeIsPresent = Yes, EISCDData(
        sortCode = sortCodeData.head.sortCode,
        bankCode = sortCodeData.head.bankCode,
        bankName = sortCodeData.head.bankName,
        nonStandardBacsDataRequired = sortCodeData.head.nonStandardBacsDataRequired,
        addressLine = sortCodeData.head.addressLine,
        town = sortCodeData.head.town,
        country = sortCodeData.head.country,
        postcode = sortCodeData.head.postcode,
        telephone = sortCodeData.head.telephone,
        bacsStatus = sortCodeData.head.bacsStatus,
        chapsStatus = sortCodeData.head.chapsStatus,
        ddiVoucher = sortCodeData.head.ddiVoucher,
        supportsDirectDebit = sortCodeData.head.supportsDirectDebit,
        supportsDirectCredit = sortCodeData.head.supportsDirectCredit,
        ibanPrefix = sortCodeData.head.ibanPrefix
      ))
    }
  }

  def generateIBAN(prefix: Option[String], account: AccountDetails): Option[String] = {
    if (prefix.isEmpty) {
      None
    } else {
      Some(s"${prefix.get}${account.sortCode}${account.accountNumber}")
    }
  }

  def flipYesNo(value: String): Option[String] = {
    value.toUpperCase() match {
      case "YES" => Some(No)
      case "NO" => Some(Yes)
      case _ => None
    }
  }

  def supportsBACS(bacsStatus: Option[String]): Option[String] = {
    if (bacsStatus.isEmpty) {
      None
    } else {
      bacsStatus.get.toUpperCase() match {
        case "A" => Some(Yes)
        case "M" => Some(Yes)
        case _ => Some(No)
      }
    }
  }

  def convertDDIVoucherToAssessmentType(ddiVoucher: Option[String]): Option[String] = {
    if (ddiVoucher.isEmpty) {
      None
    } else {
      ddiVoucher.get.toUpperCase() match {
        case "Y" => Some(Yes)
        case "N" => Some(No)
        case _ => None
      }
    }
  }
}
