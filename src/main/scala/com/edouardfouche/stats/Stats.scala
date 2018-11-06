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
package com.edouardfouche.stats

import com.edouardfouche.index.Index
/**
  * Created by fouchee on 07.07.17.
  */
trait Stats {
  type PreprocessedData <: Index // PreprocessedData are subtypes of Index, which are column oriented structures
  val id: String
  val alpha: Double
  val M: Int

  /**
    * @param input A data set (row oriented)
   */
  def preprocess(input: Array[Array[Double]]): PreprocessedData

  /**
    * @param m A data set (row oriented)
    */
  def contrast(m: Array[Array[Double]], dimensions: Set[Int]): Double = {
    this.contrast(this.preprocess(m), dimensions)
  }

  def contrast(m: PreprocessedData, dimensions: Set[Int]): Double

  /**
    * @param m A data set (row oriented)
    */
  def contrastMatrix(m: Array[Array[Double]]): Array[Array[Double]] = {
    this.contrastMatrix(this.preprocess(m))
  }

  def contrastMatrix(m: PreprocessedData): Array[Array[Double]]
}
