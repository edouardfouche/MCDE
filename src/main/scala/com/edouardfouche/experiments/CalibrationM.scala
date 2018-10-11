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
  * Compare the theoretical maximum and minimum of contrast-based approaches w.r.t. M
  */
object CalibrationM extends Experiment {
  override val alpha_range = Vector()
  override val M_range: Vector[Int] = (1 until 200).toVector
  override val nRep = 100 // number of repetition for each point
  override val data = Vector() // not using it in this experiment
  val nDim_range: Vector[Int] = (2 to 5).toVector

  def run(): Unit = {
    for {
      nDim <- nDim_range
      m <- M_range.par
    } yield {
      info(s"Starting com.edouardfouche.experiments with configuration: M: $m, nDim: $nDim")

      val tests = defaulttests

      compareCalibration(nDim = nDim, tests = tests)
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}
