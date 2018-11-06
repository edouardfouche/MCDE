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
package com.edouardfouche.index

import uds.score.UDSFunction

/**
  * Compute an index from a given data set
  * The index is "external", so to satisfy the needs of the external measures (i.e., not from us)
  *
  * @param values A row-oriented data set
  * @param parallelize Whether to parallelize or not the index computation (beta)
  */
class ExternalRankIndex(val values: Array[Array[Double]], val parallelize: Int = 0) extends Index  {
  type T = (Int, Float)

  def createIndex(input: Array[Array[Double]]): Array[Array[T]] = {
    val t = input.transpose
    val r = UDSFunction.generateRank(t).transpose
    r.zip(input).map(x => x._1.zip(x._2.map(_.toFloat)))
  }

  def randomSlice(dimensions: Set[Int], referenceDim: Int, sliceSize: Int): Array[Boolean] = {
    Slicing2.randomSlice(this.index, dimensions, referenceDim, sliceSize)
  }

  def allSlice(dimensions: Set[Int], sliceSize: Int): Array[Boolean] = {
    Slicing2.allSlice(this.index, dimensions, sliceSize)
  }

  def safeSlice(dimensions: Set[Int], referenceDim: Int, sliceSize: Int): Array[Boolean] = {
    Slicing2.safeSlice(this.index, dimensions, referenceDim, sliceSize)
  }

  // the slicing scheme used for conditional independence
  def simpleSlice(dimension: Int, sliceSize: Int): this.type = {
    //Slicing2.simpleSlice(this.index, dimension, sliceSize)
    this
  }

  def restrictedSafeRandomSlice(dimensions: Set[Int], referenceDim: Int, alpha: Double): Array[Boolean] = {
    Slicing2.restrictedSafeRandomSlice(this.index, dimensions, referenceDim, alpha)
  }

  def restrictedRandomSlice(dimensions: Set[Int], referenceDim: Int, alpha: Double): Array[Boolean] = {
    Slicing2.restrictedRandomSlice(this.index, dimensions, referenceDim, alpha)
  }

  def getSafeCut(cut: Int, reference: Int): Int = Slicing2.getSafeCut(cut, this.index, reference)
}
