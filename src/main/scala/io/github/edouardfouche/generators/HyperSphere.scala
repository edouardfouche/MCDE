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

import breeze.stats.distributions.Gaussian

case class HyperSphere(nDim: Int, noise: Double) extends DataGenerator {
  val id = s"HyperSphere-${nDim}-${noise}"

  def generate(n: Int): Array[Array[Double]] = {
    (1 to n).toArray.map { _ =>
      val init = (1 to nDim).toArray.map(y => Gaussian(0, 1).draw())
      val r = math.sqrt(init.map(math.pow(_, 2)).sum)
      postprocess(init.map((1.0 / (r * 2)) * _ + 0.5), noise)
    }
  }
}
