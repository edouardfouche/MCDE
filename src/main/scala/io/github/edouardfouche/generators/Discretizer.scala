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

object Discretizer {
  // expect rows
  def discretize(data: Array[Array[Double]], nLevels: Int): Array[Array[Double]] = {
    data.transpose.map(y => {
      val minVal = y.min
      val maxVal = y.max
      val amplitude = maxVal - minVal

      y.map(x => {
        if (nLevels == 1) amplitude / 2.0
        else if (math.abs(x % (amplitude / (nLevels - 1))) > math.abs(x % (-amplitude / (nLevels - 1)))) x - (x % (-amplitude / (nLevels - 1)))
        else x - (x % (amplitude / (nLevels - 1)))
      })
    }).transpose
  }
}