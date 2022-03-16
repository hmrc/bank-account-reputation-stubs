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

package uk.gov.hmrc.bars.stub

import com.github.tototoshi.csv.CSVReader
import com.google.inject.{AbstractModule, Provides}
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.bars.stub.helpers.{Data, EISCDHelper}
import uk.gov.hmrc.bars.stub.models.AssessmentType._
import uk.gov.hmrc.bars.stub.models.EISCDData
import uk.gov.hmrc.bars.stub.models.components.AccountDetails
import uk.gov.hmrc.bars.stub.models.stubbed.{BusinessData, PersonalData}

import java.io.File
import javax.inject.Singleton
import scala.collection.mutable.ListBuffer

class AppConfig(environment: Environment, configuration: Configuration) extends AbstractModule {

  private lazy val stubbedEISCDDataFile: String = configuration.get[String]("stubbed.data.eiscd")
  lazy val stubbedPersonalDataFile: String = configuration.get[String]("stubbed.data.personal")
  lazy val stubbedBusinessDataFile: String = configuration.get[String]("stubbed.data.business")
  lazy val denyListSortCode: String = configuration.get[String]("stubbed.data.denyListSortCode")

  private var stubbedPersonalData: Map[AccountDetails, PersonalData] = Map.empty
  private var stubbedBusinessData: Map[AccountDetails, BusinessData] = Map.empty
  private var stubbedEISCDData: List[EISCDData] = List.empty

  private def loadDataFile(stubbedDataFile: String): File = {
    environment.getExistingFile(stubbedDataFile).getOrElse {
      throw new Exception("Unable to find " + stubbedDataFile)
    }
  }

  @Provides
  @Singleton
  def sortCodeDenyList: List[String] = {
    List(denyListSortCode)
  }

  @Provides
  @Singleton
  def loadStubbedEISCDData: List[EISCDData] = {
    if (stubbedEISCDData.isEmpty) {
      val mockedDataStream = CSVReader.open(loadDataFile(stubbedEISCDDataFile)).toStreamWithHeaders
      val stubbedDataBuilder = new ListBuffer[EISCDData]()
      val it = mockedDataStream.iterator
      while (it.hasNext) {
        val data = it.next()
        stubbedDataBuilder += EISCDData(
          sortCode = data("sort-code"),
          bankCode = Data.parser(data("bank-code")),
          bankName = Data.parser(data("bank-name")),
          nonStandardBacsDataRequired = fromString(data("non-standard-bacs-data-required")),
          addressLine = Data.parser(data("address-line")),
          town = Data.parser(data("town")),
          country = Data.parser(data("country")),
          postcode = Data.parser(data("postcode")),
          telephone = Data.parser(data("telephone")),
          bacsStatus = Data.parser(data("bacs-status")),
          chapsStatus = Data.parser(data("chaps-status")),
          ddiVoucher = Data.parser(data("ddi-voucher")),
          supportsDirectDebit = fromString(data("direct-debit")),
          supportsDirectCredit = fromString(data("direct-credit")),
          ibanPrefix = Data.parser(data("iban-prefix"))
        )
      }
      stubbedEISCDData = stubbedDataBuilder.toList
    }
    stubbedEISCDData
  }

  @Provides
  @Singleton
  def provideLogger: Logger = Logger(this.getClass)

  @Provides
  @Singleton
  def loadStubbedPersonalBankAccountData: Map[AccountDetails, PersonalData] = {
    if (stubbedPersonalData.isEmpty) {
      val mockedDataStream = CSVReader.open(loadDataFile(stubbedPersonalDataFile)).toStreamWithHeaders
      val it = mockedDataStream.iterator
      while (it.hasNext) {
        val data = it.next()
        val sortCode = data("sort-code")
        val branch = EISCDHelper.getDataFor(sortCode, loadStubbedEISCDData)
        val responseData: PersonalData = PersonalData(
          accountNumberWithSortCodeIsValid = fromString(data("valid")),
          accountExists = fromString(data("account-exists")),
          name = data("account-name"),
          addressMatches = fromString(data("address-match")),
          nonConsented = fromString(data("non-consented")),
          subjectHasDeceased = fromString(data("deceased")),
          nonStandardAccountDetailsRequiredForBacs = branch.data.nonStandardBacsDataRequired,
          sortCodeIsPresentOnEISCD = branch.sortCodeIsPresent,
          sortCodeBankName = branch.data.bankName,
          sortCodeSupportsDirectDebit = branch.data.supportsDirectDebit,
          sortCodeSupportsDirectCredit = branch.data.supportsDirectCredit
        )
        stubbedPersonalData += (AccountDetails(sortCode, data("account-number")) -> responseData)
      }
    }
    stubbedPersonalData
  }

  @Provides
  @Singleton
  def loadStubbedBusinessBankAccountData: Map[AccountDetails, BusinessData] = {
    if (stubbedBusinessData.isEmpty) {
      val mockedDataStream = CSVReader.open(loadDataFile(stubbedBusinessDataFile)).toStreamWithHeaders
      val it = mockedDataStream.iterator
      while (it.hasNext) {
        val data = it.next()
        val sortCode = data("sort-code")
        val branch = EISCDHelper.getDataFor(sortCode, loadStubbedEISCDData)
        val responseData = BusinessData(
          accountNumberWithSortCodeIsValid = fromString(data("valid")),
          sortCodeIsPresentOnEISCD = branch.sortCodeIsPresent,
          sortCodeBankName = branch.data.bankName,
          nonStandardAccountDetailsRequiredForBacs = branch.data.nonStandardBacsDataRequired,
          accountExists = fromString(data("account-exists")),
          companyName = data("account-name"),
          companyPostCodeMatches = fromString(data("postcode-match")),
          companyRegistrationNumberMatches = fromString(data("registration-number-match")),
          sortCodeSupportsDirectDebit = branch.data.supportsDirectDebit,
          sortCodeSupportsDirectCredit = branch.data.supportsDirectCredit
        )
        stubbedBusinessData += (AccountDetails(sortCode, data("account-number")) -> responseData)
      }
    }
    stubbedBusinessData
  }

}
