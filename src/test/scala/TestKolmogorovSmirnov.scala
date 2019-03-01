/*
 * Copyright (C) 2018 Edouard Fouché
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
  * Created by fouchee on 26.06.17.
  */

import io.github.edouardfouche.preprocess.Preprocess
import io.github.edouardfouche.mcde.{KS, McdeStats}
import org.scalatest.FunSuite

class TestKolmogorovSmirnov extends FunSuite with TestData {
  val test: McdeStats = KS(1000, 0.1)

  test("Computing Preprocessing index structure") {
    val data = Preprocess.openCSV(getClass.getResource("/data/Independent-2-0.0.csv").getPath)
      val indexData = test.preprocess(data)
    assert(!indexData.isEmpty)
  }
  test("Contrast can be detected in a 2-D subspace") {
    val res1 = test.contrast(highcontrast_2D, Set(0, 1))
    val res2 = test.contrast(lowcontrast_2D, Set(0, 1))
    assert(res1 > 0.35)
    assert(res2 < 0.10)
  }
  test("Contrast can be detected in a 5-D subspace") {
    val res1 = test.contrast(highcontrast_5D, Set(0, 1, 2, 3, 4))
    val res2 = test.contrast(lowcontrast_5D, Set(0, 1, 2, 3, 4))
    assert(res1 > 0.15)
    assert(res2 < 0.10)
  }

  test("Computing small 5-D contrast matrix") {
    val res = test.contrastMatrix(highcontrast_5D)
    assert(!res.isEmpty)
  }

  //test("Computing larger 100-D contrast matrix") {
  //  val res = test.contrastMatrix(lowcontrast_100D)
  //  assert(!res.isEmpty)
  //}

  test("Deviation can be detected in a 2-D subspace") {
    val res1 = test.deviation(highcontrast_2D, Set(0, 1), 0)
    val res2 = test.deviation(lowcontrast_2D, Set(0, 1), 0)
    assert(res1 > 0.35)
    assert(res2 < 0.30)
  }
  test("Deviation can be detected in a 5-D subspace") {
    val res1 = test.deviation(highcontrast_5D, Set(0, 1, 2, 3, 4), 0)
    val res2 = test.deviation(lowcontrast_5D, Set(0, 1, 2, 3, 4), 0)
    assert(res1 > 0.15)
    assert(res2 < 0.10)
  }

  test("Computing small 5-D deviation matrix") {
    val res = test.deviationMatrix(highcontrast_5D)
    assert(!res.isEmpty)
  }

  test("Computing big 100-D deviation matrix") {
    val res = test.deviationMatrix(lowcontrast_100D)
    assert(!res.isEmpty)
  }

  test("Check that KS values are always between 0 and 1, alpha=0.1") {
    val preprocessed = KS(1, 0.1).preprocess(lowcontrast_2D)
    val res = for {x <- 0 until 10000} yield KS(1, 0.1).contrast(preprocessed, dimensions = Set(0, 1))
    assert(!res.exists(x => x < 0 | (x-1 > 0.001)))
  }
  test("Check that KS values are always between 0 and 1, alpha=0.5") {
    val preprocessed = KS(1, 0.1).preprocess(lowcontrast_2D)
    val res = for {x <- 0 until 10000} yield KS(1, 0.5).contrast(preprocessed, dimensions = Set(0, 1))
    assert(!res.exists(x => x < 0 | (x-1 > 0.001)))
  }
  test("Check that KS values are always between 0 and 1, alpha=0.9") {
    val preprocessed = KS(1, 0.1).preprocess(lowcontrast_2D)
    val res = for {x <- 0 until 10000} yield KS(1, 0.9).contrast(preprocessed, dimensions = Set(0, 1))
    assert(!res.exists(x => x < 0 | (x-1 > 0.001)))
  }
}
