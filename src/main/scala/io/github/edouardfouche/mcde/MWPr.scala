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
import io.github.edouardfouche.utils.HalfGaussian

import scala.annotation.tailrec

/**
  * Simply like MWP but not adjusting and correcting for ties
  * @alpha Expected share of instances in slice (independent dimensions).
  * @beta  Expected share of instances in marginal restriction (reference dimension).
  *        Added with respect to the original paper to loose the dependence of beta from alpha.
  */
case class MWPr(M: Int = 50, alpha: Double = 0.5, beta: Double = 0.5, calibrate: Boolean = false, var parallelize: Int = 0) extends McdeStats {
  override type PreprocessedData = RankIndex
  val id = "MWPr"

  def preprocess(input: Array[Array[Double]]): PreprocessedData = {
    new RankIndex(input, 0) //TODO: seems that giving parallelize another value that 0 leads to slower execution, why?

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
    // This returns results between 0 and reference.length (both incl.)
    // i.e. the "cut" is the place from which the cut starts, if the cut starts at 0 or reference.length, this is the same as no cut.
    //val cut = getSafeCut(scala.util.Random.nextInt(reference.length + 1), reference)

    //val cutLength = (indexSelection.length*alpha).toInt
    val sliceStart = scala.util.Random.nextInt((indexSelection.length * (1-beta)).toInt)
    //val sliceEndSearchStart = (sliceStart + (indexSelection.length * alpha).toInt).min(indexSelection.length - 1) // this is rather a dirty fix
    val sliceEnd = sliceStart + (indexSelection.length * beta).toInt//.min(indexSelection.length - 1)

    //println(s"sliceStart: $sliceStart, sliceEnd: $sliceEnd, reference: $reference")

    val ref = index(reference)

    def getStat(cutStart: Int, cutEnd: Int): Double = {
      @tailrec def cumulative(n: Int, acc: Double, count: Int): (Double, Int) = {
        if (n == cutEnd) (acc - (cutStart * count), count) // correct the accumulator in case the cut does not start at 0
        else if (indexSelection(ref(n))) cumulative(n + 1, acc + n, count + 1)
        else cumulative(n + 1, acc, count)
      }

      lazy val cutLength = cutEnd - cutStart
      val (r1, n1) = cumulative(cutStart, 0, 0)
      //println(s"indexCount: ${indexSelection.count(_ == true)}, sliceStart: ${sliceStart}, sliceEnd: ${sliceEnd}, r1: $r1, n1: $n1, cutLength: $cutLength")
      val P = if (n1 == 0 | n1 == cutLength) { // in the case it is empty or contains them all, the value should be maximal
        //val nA = cutLength * alpha
        //val nB = cutLength * (1.0 - alpha)
        //val Z = (nA * nB / 2.0) / math.sqrt((nA * nB * (cutLength + 1.0)) / 12.0) //* (cutLength.toFloat / reference.length) // without correction
        //val res = HalfGaussian.cdf(Z)
        //res
        1
      }
      else {
        val n2 = cutLength - n1
        val U1 = r1 - (n1 * (n1 - 1)) / 2 // -1 because our ranking starts from 0
        val std = math.sqrt((n1 * n2 * (cutLength + 1.0)) / 12.0) // without correction
        // note: n1 + n2 = n1 + cutLength - n1 = cutLength
        val mean = (n1 * n2) / 2.0
        val Z = math.abs((U1 - mean) / std)
        val res = HalfGaussian.cdf(Z) // * (cutLength.toFloat / reference.length)
        //println(s"Z: ${Z}, P: $res,  sliceStart: ${sliceStart}, sliceEnd: ${sliceEnd}, r1: $r1, n1: $n1, cutLength: $cutLength")
        res
      }
      P
    }

    getStat(sliceStart, sliceEnd)
  }
}
