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

import com.edouardfouche.generators.Independent
import com.edouardfouche.stats.mcde.MWP
import org.scalatest.FunSuite

/**
  * Created by fouchee on 10.07.17.
  */
class TestParallelMatrix extends FunSuite with TestData {

  val data = MWP().preprocess(Independent(3, 0.0).generate(1000).transpose)

  test(s"CMatrix+MWP with parallelism level = 0") {
    val test = MWP(50, parallelize = 0)
    val result = test.contrastMatrix(data)
    assert(result.nonEmpty)
  }
  test(s"CMatrix+MWP with parallelism level = 1 (auto)") {
    val test = MWP(50, parallelize = 1)
    val result = test.contrastMatrix(data)
    assert(result.nonEmpty)
  }
  /*
  test(s"CMatrix+MWP with parallelism level = 2") {
    val test = MWP(50, parallelize = 2)
    val result = test.contrastMatrix(data)
    assert(result.nonEmpty)
  }
  test(s"CMatrix+MWP with parallelism level = 4") {
    val test = MWP(50, parallelize = 4)
    val result = test.contrastMatrix(data)
    assert(result.nonEmpty)
  }
  test(s"CMatrix+MWP with parallelism level = 8") {
    val test = MWP(50, parallelize = 8)
    val result = test.contrastMatrix(data)
    assert(result.nonEmpty)
  }
  */
}