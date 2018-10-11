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
package com.edouardfouche.stats.external

import com.edouardfouche.index.NonIndex
import kit.edu.DependencyEstimation.MultivariateSpearman

/**
  * A Scala wrapper for Interaction Information (II), implemented by Hendrik Braun
  */

case class MS(calibrate:Boolean = false, var parallelize:Int = 0) extends ExternalStats {
  val id = "MS"

  type PreprocessedData = NonIndex

  def preprocess(input: Array[Array[Double]]): PreprocessedData = {
    new NonIndex(input)
  }
  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    new MultivariateSpearman(data).estimate()
  }

  /**
    * Compute the score
    * @param m          Data set, column-oriented
    * @param dimensions Set of dimensions to project
    * @return A contrast value
    */
  override def contrast(m: PreprocessedData, dimensions: Set[Int]): Double = {
    val s = score(dimensions.toArray.sorted.map(x => m(x)).transpose, null)

    if (s.isNaN | s.isInfinite) {
      logger.info(s"$s value in ${this.getClass.getSimpleName} coerced to 0.0")
      0.0
    }
    else s
  }
}
