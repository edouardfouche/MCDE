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

import io.github.edouardfouche.index.AdjustedRankIndex

import scala.annotation.tailrec

/**
  * Weighted average of the Rank-Biserial correlation
  * @alpha Expected share of instances in slice (independent dimensions).
  * @beta  Expected share of instances in marginal restriction (reference dimension).
  *        Added with respect to the original paper to loose the dependence of beta from alpha.
  */
case class MWB(M: Int = 50, alpha: Double = 0.5, beta: Double = 0.5, calibrate: Boolean = false, var parallelize: Int = 0) extends McdeStats {
  type PreprocessedData = AdjustedRankIndex
  val id = "MWB"

  def preprocess(input: Array[Array[Double]]): PreprocessedData = {
    new AdjustedRankIndex(input, 0) //TODO: seems that giving parallelize another value that 0 leads to slower execution, why?
  }

  /**
    * Compute a statistical test based on  Mann-Whitney U test using a reference vector (the indices of a dimension
    * ordered by the rank) and a set of Int that correspond to the intersection of the position of the element in the
    * slices in the other dimensions.
    *
    * @param reference      The original position of the elements of a reference dimension ordered by their rank
    * @param indexSelection An array of Boolean where true means the value is part of the slice
    * @return The Mann-Whitney statistic
    */
  def twoSample(index: PreprocessedData, reference: Int, indexSelection: Array[Boolean]): Double = {
    //require(reference.length == indexSelection.length, "reference and indexSelection should have the same size")
    val sliceStart = index.getSafeCut(scala.util.Random.nextInt((indexSelection.length * (1-beta)).toInt), reference)
    val sliceEndSearchStart = (sliceStart + (indexSelection.length * beta).toInt).min(indexSelection.length - 1)
    val sliceEnd = index.getSafeCut(sliceEndSearchStart, reference)

    val ref = index(reference)

    def getStat(cutStart: Int, cutEnd: Int): Double = {
      @tailrec def cumulative(n: Int, acc: Double, count: Int): (Double, Int) = {
        if (n == cutEnd) (acc - (cutStart * count), count) // correct the accumulator in case the cut does not start at 0
        else if (indexSelection(ref(n)._1)) cumulative(n + 1, acc + ref(n)._2, count + 1)
        else cumulative(n + 1, acc, count)
      }
      lazy val cutLength = cutEnd - cutStart
      val (r1, n1) = cumulative(cutStart, 0, 0)
      //println(s"indexCount: ${indexSelection.count(_ == true)}, sliceStart: ${sliceStart}, sliceEnd: ${sliceEnd}, r1: $r1, n1: $n1, cutLength: $cutLength")
      val B = if (n1 == 0 | n1 == cutLength) 1 // in the case it is empty or contains them all, the value should be maximal (here: 1)
      else {
        val n2 = cutLength - n1
        val U1 = r1 - n1 * (n1 - 1) / 2 // -1 because our ranking starts from 0
        val U2 = n1 * n2 - U1
        //U1.min(U2) / (n1 * n2)
        1 - (2 * U1.min(U2) / (n1 * n2).toDouble)
      }
      //println(s"U: ${U}, sliceStart: ${sliceStart}, sliceEnd: ${sliceEnd}, r1: $r1, n1: $n1, cutLength: $cutLength")
      B
    }
    getStat(sliceStart, sliceEnd)
  }
}
