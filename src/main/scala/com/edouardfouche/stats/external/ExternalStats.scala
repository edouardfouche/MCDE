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

import com.edouardfouche.stats.{Stats, StatsFactory}
import com.edouardfouche.stats.mcde.Calibrator
import com.typesafe.scalalogging.LazyLogging

import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Created by fouchee on 07.07.17.
  */
trait ExternalStats extends Stats with LazyLogging {
  val id: String
  val alpha = 0.0
  val beta = 0.0
  val M = 0
  val calibrate: Boolean
  var parallelize: Int

  def preprocess(input: Array[Array[Double]]): PreprocessedData

  def contrast(m: PreprocessedData, dimensions: Set[Int]): Double

  // Here we assume the data is row-oriented (e.g. ready for UDS functions)
  // Note: some measures may return NaN, for example NonCoexistence UDS.contrast(StraightLines(2, 0.0).generate(1000).transpose, Set(0, 1))
  def score(data: Array[Array[Double]], preRank: Array[Array[Int]]): Double

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
        colspar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
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

    if (calibrate) {
      val uncalibrated = StatsFactory.getTest(this.id, this.M, this.alpha, this.beta, calibrate = false, parallelize)
      for {
        x <- cols
        y <- 0 until x
      } {
        val calibrated = Calibrator.calibrateValue(matrix(x)(y), uncalibrated, 2, m(0).length)
        matrix(x)(y) = calibrated
        matrix(y)(x) = calibrated
      }
    }

    parallelize = currentparallelismlevel
    matrix
  }
}
