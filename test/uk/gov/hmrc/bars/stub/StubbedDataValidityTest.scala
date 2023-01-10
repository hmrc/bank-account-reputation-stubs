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

package uk.gov.hmrc.bars.stub

import com.github.tototoshi.csv.CSVReader
import org.assertj.core.api.Assertions._
import org.scalatest.funsuite.AnyFunSuite
import play.api.{Configuration, Environment}

import java.io.File

class StubbedDataValidityTest extends AnyFunSuite {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val appConfig = new AppConfig(env, configuration)
  private lazy val logger = appConfig.provideLogger
  private lazy val eiscdData = appConfig.loadStubbedEISCDData

  private def loadDataFile(stubbedDataFile: String): File = {
    env.getExistingFile(stubbedDataFile).getOrElse {
      throw new Exception("Unable to find " + stubbedDataFile)
    }
  }

  test("eiscd-data flag is correct for all personal data entries") {
    val mockedDataStream = CSVReader.open(loadDataFile(appConfig.stubbedPersonalDataFile)).toStreamWithHeaders
    val it = mockedDataStream.iterator

    while (it.hasNext) {
      val data = it.next()
      val sortCodesFound = eiscdData.filter(bankData => bankData.sortCode == data("sort-code"))
      if (data("eiscd-data").toBoolean) {
        logger.info(s"\n\nExpecting sort code to be found in EISCD for the following account:\n\nSort code: ${data("sort-code")}\nAccount Number: ${data("account-number")}\n")
        assertThat(sortCodesFound.size)
          .isEqualTo(1)

      } else {
        logger.info(s"\n\nExpecting sort code to not be in EISCD for the following account:\n\nSort code: ${data("sort-code")}\nAccount Number: ${data("account-number")}\n")
        assertThat(sortCodesFound.size)
          .isEqualTo(0)
      }
    }
  }

  test("eiscd-data flag is correct for all business data entries") {
    val mockedDataStream = CSVReader.open(loadDataFile(appConfig.stubbedBusinessDataFile)).toStreamWithHeaders
    val it = mockedDataStream.iterator
    while (it.hasNext) {
      val data = it.next()
      val sortCodesFound = eiscdData.filter(bankData => bankData.sortCode == data("sort-code"))
      if (data("eiscd-data").toBoolean) {
        logger.info(s"\n\nExpecting sort code to be found in EISCD for the following account:\n\nSort code: ${data("sort-code")}\nAccount Number: ${data("account-number")}\n")
        assertThat(sortCodesFound.size)
          .isEqualTo(1)

      } else {
        logger.info(s"\n\nExpecting sort code to not be in EISCD for the following account:\n\nSort code: ${data("sort-code")}\nAccount Number: ${data("account-number")}\n")
        assertThat(sortCodesFound.size)
          .isEqualTo(0)
      }
    }
  }
}
