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

import scala.annotation.tailrec

trait Index {
  val values: Array[Array[Double]]
  val parallelize:Int

  type T
  val index: Array[Array[T]] = createIndex(values.transpose) // IMPORTANT: The transpose, makes the input column-oriented

  /**
    *
    * @param data a data set (column-oriented!)
    * @return An index, which is also column-oriented
    */
  protected def createIndex(data: Array[Array[Double]]): Array[Array[T]]

  def apply(n: Int) = index(n) // access the columns of the index

  def indices = index.indices // this is supposed to give the indices of the columns

  def numCols = index.length
  def numRows = index(0).length

  def isEmpty: Boolean = index.length == 0

  /**
    * Produce a subspace slice by conditioning on all dimensions, except a reference dimension
    * @param m An index structure. Array of 2-D Tuple where the first element in the index, the second the rank
    * @param dimensions The set of dimensions of the subspaces
    * @param referenceDim The dimension that is considered as reference
    * @param sliceSize The size of the slice for each dimensions, determined by alpha
    * @return Returns an array of booleans. True corresponds to indexes included in the slice.
    */
  //TODO: Question : Is it problematic to slice on ties? Its seems not.
  def randomSlice(dimensions: Set[Int], referenceDim: Int, sliceSize: Int): Array[Boolean]

  def allSlice(dimensions: Set[Int], sliceSize: Int): Array[Boolean]

  def safeSlice(dimensions: Set[Int], referenceDim: Int, sliceSize: Int): Array[Boolean]

  // the slicing scheme used for conditional independence
  def simpleSlice(dimension: Int, sliceSize: Int): this.type

  def getSafeCut(cut: Int, reference: Int): Int

  def mean(xs: Array[Int]): Float = xs.sum / xs.length.toFloat

  def restrictedSafeRandomSlice(dimensions: Set[Int], referenceDim: Int, alpha: Double): Array[Boolean]

  def restrictedRandomSlice(dimensions: Set[Int], referenceDim: Int, alpha: Double): Array[Boolean]

  /**
    * Find the greatest common divisor of a and b
    * @param a integer
    * @param b integer
    * @return An integer, the greatest common divisor of a and b
    */
  @tailrec
  final def gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

  /**
    * Coprimality test between two integers a and b
    * @param a integer
    * @param b integer
    * @return boolean, true if a and b are coprimes
    */
  def areCoPrimes(a: Int, b: Int): Boolean = gcd(a, b) == 1

  /**
    * Helper function that generates a stream of integers that are close to a, shall not be smaller than 1
    * @param a starting integer
    * @param inc increment (internal parameter with default value 1)
    * @return A stream of integer close to a and greater or equal to 1
    */
  def closeSearch(a: Int, inc: Int = 1): Stream[Int] = {
    //(a+1) #:: closeSearch(a+1)
    if (a - inc > 1) (a + inc) #:: (a - inc) #:: closeSearch(a, inc + 1)
    else (a + inc) #:: closeSearch(a, inc + 1)
  }

  /**
    * Find the closest integer from a, which is coprime with ref
    * @param a An integer. We want to find the closest coprime from this position
    * @param ref A reference integer.
    * @return The cloest integer from a which is coprime with ref
    */
  def findClosestCoPrime(a: Int, ref: Int): Int = {
    if (areCoPrimes(a, ref)) a
    else closeSearch(a).filter(areCoPrimes(_, ref)).head
  }
}
