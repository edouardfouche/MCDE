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

import io.github.edouardfouche.index.CorrectedRankIndex
import io.github.edouardfouche.utils.HalfGaussian

import scala.annotation.tailrec
import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Same as MWP but with caring for ties also in the slicing process
  * @alpha Expected share of instances in slice (independent dimensions).
  * @beta  Expected share of instances in marginal restriction (reference dimension).
  *        Added with respect to the original paper to loose the dependence of beta from alpha.
  */

case class MWPs(M: Int = 50, alpha: Double = 0.5, beta: Double = 0.5, var parallelize: Int = 0) extends McdeStats {
  type PreprocessedData = CorrectedRankIndex
  val id = "MWPs"
  //val slicer = Slicing3

  def preprocess(input: Array[Array[Double]]): PreprocessedData = {
    new CorrectedRankIndex(input, 0) //TODO: seems that giving parallelize another value that 0 leads to slower execution, why?
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
    val start = scala.util.Random.nextInt((indexSelection.length * (1-beta)).toInt)
    val sliceStart = index.getSafeCut(start, reference)
    val sliceEndSearchStart = (sliceStart + (indexSelection.length * beta).toInt).min(indexSelection.length - 1)
    val sliceEnd = index.getSafeCut(sliceEndSearchStart, reference)

    //println(s"indexSelection.length: ${indexSelection.length}, start: $start, actualStart: $sliceStart, sliceEnd: $sliceEnd, reference: $reference")

    val ref = index(reference)

    def getStat(cutStart: Int, cutEnd: Int): Double = {
      @tailrec def cumulative(n: Int, acc: Double, count: Int): (Double, Int) = {
        if (n == cutEnd) (acc - (cutStart * count), count) // correct the accumulator in case the cut does not start at 0
        else if (indexSelection(ref(n)._1)) cumulative(n + 1, acc + ref(n)._2, count + 1)
        else cumulative(n + 1, acc, count)
      }

      lazy val cutLength = cutEnd - cutStart
      val (r1, n1) = cumulative(cutStart, 0, 0)

      if (n1 == 0 | n1 == cutLength) {
        1 // when one of the two sample is empty, this just means maximal possible score.
      }
      else {
        val n2 = cutLength - n1
        val U1 = r1 - (n1 * (n1 - 1)) / 2 // -1 because our ranking starts from 0
        val corrMax = ref(cutEnd-1)._3
        val corrMin = if(cutStart == 0) 0 else ref(cutStart-1)._3
        val correction = (corrMax - corrMin) / (cutLength * (cutLength - 1))
        val std = math.sqrt((n1 * n2 / 12.0) * (cutLength + 1 - correction)) // handle ties https://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U_test
        if(std == 0) 0 // This happens in the extreme case that the cut consists in only one unique value
        else {
          val mean = (n1 * n2) / 2.0
          val Z = math.abs((U1 - mean) / std)
          val res = HalfGaussian.cdf(Z)
          if (res.isNaN) {
            print(s"reference: ${ref.slice(cutStart, cutEnd).take(5) mkString ","}")
            println(s"U1: $U1, U2: ${n1 * n2 - U1}, n1: $n1, n2: $n2, std: $std, correction: $correction -> res: $res")
          }
          res
        }
      }
    }
    val res = getStat(sliceStart, sliceEnd)
    //println(s"res : $res")
    res
  }

  /**
    * Compute the contrast of a subspace
    * Note: I override it because I need to change the slicing strategy slightly to a uniform one.
    *
    * @param m          The indexes from the original data ordered by the rank of the points
    * @param dimensions The dimensions in the subspace, each value should be smaller than the number of arrays in m
    * @return The contrast of the subspace (value between 0 and 1)
    */
  override def contrast(m: PreprocessedData, dimensions: Set[Int]): Double = {
    // Sanity check
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    val sliceSize = (math.pow(alpha, 1.0 / (dimensions.size - 1.0)) * m.numRows).ceil.toInt /// WARNING: Do not forget -1
    //println(s"dimensions $dimensions, sliceSize: ${sliceSize}")

    val result = if (parallelize == 0) {
      (1 to M).map(i => {
        val referenceDim = dimensions.toVector(scala.util.Random.nextInt(dimensions.size))
        twoSample(m, referenceDim, m.safeSlice(dimensions, referenceDim, sliceSize))
      }).sum / M
    } else {
      val iterations = (1 to M).par
      if (parallelize > 1) {
        //iterations.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        iterations.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      iterations.map(i => {
        val referenceDim = dimensions.toVector(scala.util.Random.nextInt(dimensions.size))
        twoSample(m, referenceDim, m.randomUniformSlice(dimensions, referenceDim, sliceSize))
      }).sum / M
    }

    result
  }
}