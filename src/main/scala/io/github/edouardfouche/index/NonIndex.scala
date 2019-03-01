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
package io.github.edouardfouche.index

/**
  * A dummy index structure
  * @param values
  * @param parallelize
  */
class NonIndex(val values: Array[Array[Double]], val parallelize: Int = 0) extends Index {
  type T = Double

  def createIndex(input: Array[Array[Double]]): Array[Array[T]] = input

  def randomSlice(dimensions: Set[Int], referenceDim: Int, sliceSize: Int): Array[Boolean] = {
    Array.fill[Boolean](this.numRows)(true)
  }

  def allSlice(dimensions: Set[Int], sliceSize: Int): Array[Boolean] = {
    Array.fill[Boolean](this.numRows)(true)
  }

  def safeSlice(dimensions: Set[Int], referenceDim: Int, sliceSize: Int): Array[Boolean] = {
    Array.fill[Boolean](this.numRows)(true)
  }

  // the slicing scheme used for conditional independence
  def simpleSlice(dimension: Int, sliceSize: Int): this.type = {
    this
  }

  def restrictedSafeRandomSlice(dimensions: Set[Int], referenceDim: Int, alpha: Double): Array[Boolean] = {
    Array.fill[Boolean](this.numRows)(true)
  }

  def restrictedRandomSlice(dimensions: Set[Int], referenceDim: Int, alpha: Double): Array[Boolean] = {
    Array.fill[Boolean](this.numRows)(true)
  }

  def getSafeCut(cut: Int, reference: Int): Int = cut
}
