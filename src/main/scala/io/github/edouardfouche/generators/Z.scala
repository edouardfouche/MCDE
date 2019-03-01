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

case class Z(nDim: Int, noise: Double) extends DataGenerator {
  val id = s"Z-${nDim}-${noise}"

  def generate(n: Int): Array[Array[Double]] = {
    (1 to n).toArray.map { _ =>
      val x = Uniform(0, 1).draw()
      val data = (1 to nDim).toArray.map(y => {
        val v = if (y == 1) x
        else {
          val flag = scala.util.Random.nextInt(3)
          if (flag == 0) x
          else if (flag == 1) 0
          else 1
        }
        addUniformNoise(v, noise)
      })
      postprocess(data, noise)
    }
  }
}
