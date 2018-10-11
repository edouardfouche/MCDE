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
package com.edouardfouche.experiments

/**
  * Created by fouchee on 09.08.17.
  * Compare the theoretical maximum and minimum of contrast-based approaches w.r.t. N
  */
object CalibrationN extends Experiment {
  override val alpha_range = Vector() // not used here
  override val M_range = Vector() // not use here
  override val data = Vector() // not using it in this experiment
  override val nRep = 100 // number of repetition for each point

  val n: Vector[Int] = (100 to 10000).toVector
  val d: Int = 2

  def run(): Unit = {
    val tests = defaulttests

    for { // do first the least expensive
      n <- (100 to 1000).par
    } yield {
      info(s"Starting com.edouardfouche.experiments with configuration: n=$n, M: $defaultM, nDim: $d")
      compareCalibration(nDim = d, tests = tests, n = n)
    }

    for { // do the rest if we have time
      n <- (1001 to 10000).par
    } yield {
      info(s"Starting com.edouardfouche.experiments with configuration: n=$n, M: $defaultM, nDim: $d")
      compareCalibration(nDim = d, tests = tests, n = n)
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}
