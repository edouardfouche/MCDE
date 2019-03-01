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
package io.github.edouardfouche.generators

import breeze.stats.distributions.Uniform

case class DoubleLinear(coef: Double, nDim: Int, noise: Double) extends DataGenerator {
  val id = s"DoubleLinear_${coef}-${nDim}-${noise}"

  def generate(n: Int): Array[Array[Double]] = {
    (1 to n).toArray.map { _ =>
      var data = Array(Uniform(0, 1).draw())
      (2 to nDim).toArray.foreach(_ => {
        if (scala.util.Random.nextInt(2) == 0) data = data ++ Array(data.head * coef)
        else data = data ++ Array(data.head)
      })
      postprocess(data, noise)
    }
  }
}
