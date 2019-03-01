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

import io.github.edouardfouche.mcde.StatsFactory
import org.scalatest.FunSuite

/**
  * Created by fouchee on 19.07.17.
  */
class TestMiniBenchmark extends FunSuite with TestData {
  input.foreach { i =>
    test(s"${i.id} - Computing big 2-D deviation matrix alpha:0.1") {
      val res = StatsFactory.getTest(i.id, 100, 0.1, 0.1, calibrate = false, parallelize = i.parallelize).contrastMatrix(lowcontrast_100D)
      assert(!res.isEmpty)
    }
    test(s"${i.id} - Computing big 2-D deviation matrix alpha:0.3") {
      val res = StatsFactory.getTest(i.id, 100, 0.3, 0.3, calibrate = false, parallelize = i.parallelize).contrastMatrix(lowcontrast_100D)
      assert(!res.isEmpty)
    }
    test(s"${i.id} - Computing big 2-D deviation matrix alpha:0.5") {
      val res = StatsFactory.getTest(i.id, 100, 0.5, 0.5, calibrate = false, parallelize = i.parallelize).contrastMatrix(lowcontrast_100D)
      assert(!res.isEmpty)
    }
    test(s"${i.id} - Computing big 2-D deviation matrix alpha:0.7") {
      val res = StatsFactory.getTest(i.id, 100, 0.7, 0.7,  calibrate = false, parallelize = i.parallelize).contrastMatrix(lowcontrast_100D)
      assert(!res.isEmpty)
    }
    test(s"${i.id} - Computing big 2-D deviation matrix alpha:0.9") {
      val res = StatsFactory.getTest(i.id, 100, 0.9, 0.9, calibrate = false, parallelize = i.parallelize).contrastMatrix(lowcontrast_100D)
      assert(!res.isEmpty)
    }
  }
}