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

import com.edouardfouche.index.ExternalRankIndex
import uds.mac.MACFunction

/**
  * A Scala wrapper for MAC (Hoang Vu Nguyen et al. 2013), implemented by its authors
  * The uds package was found here: http://eda.mmci.uni-saarland.de/prj/uds/
  */

case class MAC(calibrate:Boolean = false, var parallelize:Int = 0) extends ExternalStats {
  val id = "MAC"
  type PreprocessedData = ExternalRankIndex

  def preprocess(input: Array[Array[Double]]): PreprocessedData = {
    new ExternalRankIndex(input)
  }

  def contrast(m: PreprocessedData, dimensions: Set[Int]): Double = {
    val unz = dimensions.toArray.sorted.map(x => m(x).unzip)
    val data = unz.map(x => x._2)
    val ranks = unz.map(x => x._1)
    val s = score(data.transpose.map(_.map(_.toDouble)), ranks.transpose) // This is not nice to have to map back to Double but I have no choice for now

    if (s.isNaN | s.isInfinite) {
      logger.info(s"$s value in ${this.getClass.getSimpleName} coerced to 0.0")
      0.0
    }
    else s
  }

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]]): Double = {
    MACFunction.computeScore(data, preRank) // it takes rows and not columns
  }
}
