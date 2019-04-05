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

import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Created by fouchee on 07.07.17.
  * @alpha Expected share of instances in slice (independent dimensions).
  * @beta  Expected share of instances in marginal restriction (reference dimension).
  *        Added with respect to the original paper to loose the dependence of beta from alpha.
  */
trait McdeStats extends Stats {
  type U
  //val slicer: Slicer[U]
  val id: String
  val alpha: Double
  val beta: Double // Added to loose the dependence of beta from alpha
  val M: Int
  var parallelize: Int

  require(alpha > 0 & alpha < 1, "alpha should be greater than 0 and lower than 1")
  require(M > 0, "M should be greater than 0")
  require(beta > 0 & beta <= 1, "beta should be greater than 0 and lower than 1")

  //def contrast(m: PreprocessedData, dimensions: Set[Int]): Double
  // I think this expected a number of records
  def preprocess(input: Array[Array[Double]]): PreprocessedData

  /**
    * Statistical test computation
    *
    * @param reference      The vector of the reference dimension as an array of 2-Tuple. First element is the index, the second is the rank
    * @param indexSelection An array of Boolean that contains the information if a given index is in the slice
    */
  def twoSample(index: PreprocessedData, reference: Int, indexSelection: Array[Boolean]): Double

  /**
    * Compute the contrast of a subspace
    *
    * @param m          The indexes from the original data ordered by the rank of the points
    * @param dimensions The dimensions in the subspace, each value should be smaller than the number of arrays in m
    * @return The contrast of the subspace (value between 0 and 1)
    */
  def contrast(m: PreprocessedData, dimensions: Set[Int]): Double = {
    // Sanity check
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    val sliceSize = (math.pow(alpha, 1.0 / (dimensions.size - 1.0)) * m.numRows).ceil.toInt /// WARNING: Do not forget -1
    //println(s"dimensions $dimensions, sliceSize: ${sliceSize}")

    val result = if (parallelize == 0) {
      (1 to M).map(i => {
        val referenceDim = dimensions.toVector(scala.util.Random.nextInt(dimensions.size))
        twoSample(m, referenceDim, m.randomSlice(dimensions, referenceDim, sliceSize))
      }).sum / M
    } else {
      val iterations = (1 to M).par
      if (parallelize > 1) {
        //iterations.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        iterations.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      iterations.map(i => {
        val referenceDim = dimensions.toVector(scala.util.Random.nextInt(dimensions.size))
        twoSample(m, referenceDim, m.randomSlice(dimensions, referenceDim, sliceSize))
      }).sum / M
    }

    //if(calibrate) Calibrator.calibrateValue(result, StatsFactory.getTest(this.id, this.M, this.alpha, calibrate=false), dimensions.size, m(0).length)// calibrateValue(result, dimensions.size, alpha, M)
    //else result
    result
  }

  /**
    * Compute the contrast of a subspace // This is a version where alpha is always choosen at random between 0.1 and 0.9
    *
    * @param m          The indexes from the original data ordered by the rank of the points
    * @param dimensions The dimensions in the subspace, each value should be smaller than the number of arrays in m
    * @return The contrast of the subspace (value between 0 and 1)
    */
  def contrastAlpha(m: PreprocessedData, dimensions: Set[Int]): Double = {
    // Sanity check
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")

    //println(s"dimensions $dimensions, sliceSize: ${sliceSize}")

    val result = if (parallelize == 0) {
      (1 to M).map(i => {
        val alpha = (scala.util.Random.nextInt(9)+1) / 10.0
        val sliceSize = (math.pow(alpha, 1.0 / (dimensions.size - 1.0)) * m.numRows).ceil.toInt /// WARNING: Do not forget -1
        val referenceDim = dimensions.toVector(scala.util.Random.nextInt(dimensions.size))
        twoSample(m, referenceDim, m.randomSlice(dimensions, referenceDim, sliceSize))
      }).sum / M
    } else {
      val iterations = (1 to M).par
      if (parallelize > 1) {
        //iterations.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        iterations.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      iterations.map(i => {
        val alpha = (scala.util.Random.nextInt(9)+1) / 10.0
        val sliceSize = (math.pow(alpha, 1.0 / (dimensions.size - 1.0)) * m.numRows).ceil.toInt /// WARNING: Do not forget -1
        val referenceDim = dimensions.toVector(scala.util.Random.nextInt(dimensions.size))
        twoSample(m, referenceDim, m.randomSlice(dimensions, referenceDim, sliceSize))
      }).sum / M
    }

    //if(calibrate) Calibrator.calibrateValue(result, StatsFactory.getTest(this.id, this.M, this.alpha, calibrate=false), dimensions.size, m(0).length)// calibrateValue(result, dimensions.size, alpha, M)
    //else result
    result
  }

  /**
    * Compute the deviation of a subspace with respect to a particular dimension
    *
    * @param m            The indexes from the original data ordered by the rank of the points
    * @param dimensions   The dimensions in the subspace, each value should be smaller than the number of arrays in m
    * @param referenceDim The reference dimensions, should be contained in dimensions
    * @return A 2-D Array contains the contrast for each pairwise dimension
    */
  def deviation(m: PreprocessedData, dimensions: Set[Int], referenceDim: Int): Double = {
    // Sanity check
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    //require(dimensions.contains(referenceDim), "The reference dimensions should be contained in the set of dimensions")

    val sliceSize = (math.pow(alpha, 1.0 / (dimensions.size - 1.0)) * m(0).length).ceil.toInt /// WARNING: Do not forget -1

    val result = (1 to M).map(i => twoSample(m, referenceDim, m.randomSlice(dimensions, referenceDim, sliceSize))).sum / M //, targetSampleSize))).sum / M

    //if(calibrate) Calibrator.calibrateValue(result, StatsFactory.getTest(this.id, this.M, this.alpha, calibrate=false), dimensions.size, m(0).length)// calibrateValue(result, dimensions.size, alpha, M)
    //else result
    result
  }

  /**
    * Compute the deviation of a subspace with respect to a particular dimension // This is a version where alpha is always choosen at random between 0.1 and 0.9
    *
    * @param m            The indexes from the original data ordered by the rank of the points
    * @param dimensions   The dimensions in the subspace, each value should be smaller than the number of arrays in m
    * @param referenceDim The reference dimensions, should be contained in dimensions
    * @return A 2-D Array contains the contrast for each pairwise dimension
    */
  def deviationAlpha(m: PreprocessedData, dimensions: Set[Int], referenceDim: Int): Double = {
    // Sanity check
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    //require(dimensions.contains(referenceDim), "The reference dimensions should be contained in the set of dimensions")

    val result = (1 to M).map(i => {
      val alpha = (scala.util.Random.nextInt(9)+1) / 10.0
      val sliceSize = (math.pow(alpha, 1.0 / (dimensions.size - 1.0)) * m.numRows).ceil.toInt /// WARNING: Do not forget -1
      twoSample(m, referenceDim, m.randomSlice(dimensions, referenceDim, sliceSize))
    }).sum / M //, targetSampleSize))).sum / M

    //if(calibrate) Calibrator.calibrateValue(result, StatsFactory.getTest(this.id, this.M, this.alpha, calibrate=false), dimensions.size, m(0).length)// calibrateValue(result, dimensions.size, alpha, M)
    //else result
    result
  }

  def conditionalIndependence(m: PreprocessedData, dimensions: Set[Int]): Double = {
    val otherdimensions = m.indices.toSet -- dimensions
    val independences = for {
      dim <- otherdimensions
    } yield {
      val result = (1 to M).map(i => {
        val sliceSize = (m(0).length * 0.1).toInt //(values(scala.util.Random.nextInt(values.length)) * m(0).length).toInt  // (m(0).length * alpha).toInt // I've put alpha, but a scala.util.Random.nextInt(m(0).length)
        val mm = m.simpleSlice(dim, sliceSize)
        contrast(mm, dimensions)
      }).sum / M
      result
    }
    independences.min
  }


  def deviation(m: Array[Array[Double]], dimensions: Set[Int], referenceDim: Int): Double = {
    this.deviation(this.preprocess(m), dimensions, referenceDim)
  }

  /**
    * Compute the pairwise contrast matrix for a given data set
    * Note: This matrix is symmetric
    *
    * @param m The indexes from the original data ordered by the rank of the points
    * @return A 2-D Array contains the contrast for each pairwise dimension
    */
  def contrastMatrix(m: PreprocessedData): Array[Array[Double]] = {
    val numCols = m.numCols
    val matrix = Array.ofDim[Double](numCols, numCols)

    val cols = if (parallelize == 0) {
      0 until numCols
    } else {
      val colspar = (0 until numCols).par
      if (parallelize > 1) {
        //colspar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        colspar.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      colspar
    }

    // TODO: The problem here is that the underlying stat is also parallelized, when parallelism is activated.
    // The current workaround is to have parallize as a variable and set it temporarily to 0 (but that is not very good)
    val currentparallelismlevel = parallelize
    parallelize = 0

    for {
      x <- cols
      y <- 0 until x
    } yield {
      val c = contrast(m, Set(x, y))
      matrix(x)(y) = c
      matrix(y)(x) = c
    }

    parallelize = currentparallelismlevel
    matrix
  }



  def deviationMatrix(m: Array[Array[Double]]): Array[Array[Double]] = {
    deviationMatrix(preprocess(m))
  }

  /**
    * Compute the pairwise deviation matrix for a given data set
    * Note: This matrix is asymmetric
    *
    * @param m The indexes from the original data ordered by the rank of the points
    * @return A 2-D Array contains the deviation for each pairwise dimension
    */
  def deviationMatrix(m: PreprocessedData): Array[Array[Double]] = {
    // Sanity check
    //require(alpha > 0 & alpha < 1, "alpha should be greater than 0 and lower than 1")
    //require(M > 0, "M should be greater than 0")
    val numCols = m.numCols
    val matrix = Array.ofDim[Double](numCols, numCols)

    val cols = if (parallelize == 0) {
      0 until numCols
    } else {
      val colspar = (0 until numCols).par
      if (parallelize > 1) {
        //colspar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        colspar.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      colspar
    }

    for {
      x <- cols //0 until numCols//.par // let's try not to parallelize here
      y <- 0 until x
    } yield {
      matrix(x)(y) = deviation(m, Set(x, y), x)
      matrix(y)(x) = deviation(m, Set(x, y), y)
    }
    matrix
  }
}
