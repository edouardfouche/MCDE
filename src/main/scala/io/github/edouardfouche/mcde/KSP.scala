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
package io.github.edouardfouche.mcde

import io.github.edouardfouche.index.RankIndex

import scala.annotation.tailrec
import scala.math.{E, pow, sqrt}

/**
  * This is a re-implementation  of the contrast measure as proposed in HiCS
  * Use the Kolmogorov-Smirnov test as basis. To the best of my knowledge, the most efficient existing implementation.
  *
  * @alpha Expected share of instances in slice (independent dimensions).
  * @beta Expected share of instances in marginal restriction (reference dimension).
  *       Added with respect to the original paper to loose the dependence of beta from alpha.
  *
  */
//TODO: It would be actually interesting to compare MCDE with a version with the KSP-test AND all the improvements proposed by MCDE
case class KSP(M: Int = 50, alpha: Double = 0.5, beta: Double = 1.0, var parallelize: Int = 0) extends McdeStats {
  type PreprocessedData = RankIndex
  val id = "KSP"

  def preprocess(input: Array[Array[Double]]): PreprocessedData = {
    new RankIndex(input, 0) //TODO: seems that giving parallelize another value that 0 leads to slower execution, why?
  }

  /**
    * Compute the Kolmogorov Smirnov test using a reference vector (the indices of a dimension ordered by the rank) and
    * a set of Int that correspond to the intersection of the position of the element in the slices in the other
    * dimensions.
    *
    * @param reference      The original position of the elements of a reference dimension ordered by their rank
    * @param indexSelection An array of Boolean where true means the value is part of the slice
    * @return The contrast score, which is 1-p of the p-value of the Kolmogorov-Smirnov statistic
    */
  def twoSample(index: PreprocessedData, reference: Int, indexSelection: Array[Boolean]): Double = {
    //require(reference.length == indexSelection.length, "reference and indexSelection should have the same size")

    val ref = index(reference)

    val inSlize = indexSelection.count(_ == true)
    val outSlize = ref.length - inSlize

    if (inSlize == 0 || outSlize == 0) 1.0 // If one is empty they are perfectly diffrent --> score = 1 (and no prob with division by 0)

    val selectIncrement = 1.0 / inSlize
    val refIncrement = 1.0 / outSlize


    // This step is impossible (or difficult) to parallelize, but at least it is tail recursive
    @tailrec def cumulative(n: Int, acc1: Double, acc2: Double, currentMax: Double): Double = {
      if (n == ref.length) currentMax
      else {
        if (indexSelection(ref(n)))
          cumulative(n + 1, acc1 + selectIncrement, acc2, currentMax max math.abs(acc2 - (acc1 + selectIncrement)))
        else
          cumulative(n + 1, acc1, acc2 + refIncrement, currentMax max math.abs(acc2 + refIncrement - acc1))
      }
    }

    /**
      * @param D  D value from KSP test
      * @param n1 n Datapoints in first sample
      * @param n2 n Datapoints in second sample
      * @return p-value of two-sided two-sample KSP
      */

    def get_p_from_D(D: Double, n1: Long, n2: Long): Double = {
      lazy val z = D * sqrt(n1 * n2 / (n1 + n2))

      def exp(k: Int): Double = pow(-1, k - 1) * pow(E, -2 * pow(k, 2) * pow(z, 2))

      def infi_exp(k: Int): Double = pow(-1, k-1) * pow(E, 2 * pow(k,2) * pow(D, 2)) // in case lim n1, n2 -> infi

      // TODO: The part inside the summation could be done easily in parallel
      @tailrec
      def loop(sumation: Double, i: Int, end: Int, f: Int => Double): Double = {
        if (i == end) f(i) + sumation
        else loop(f(i) + sumation, i + 1, end, f)
      }

      if(n1 >= 3037000499L && n2 >= 3037000499L) 1 - 2 * loop(0, 1, 10000, infi_exp) // squaring n1,n2 will reach the limit of Long
      else 1 - 2 * loop(0, 1, 10000, exp)
    }

    get_p_from_D(cumulative(0, 0, 0, 0), inSlize, outSlize)
  }
}
