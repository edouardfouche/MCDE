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

import com.edouardfouche.experiments.Data._
import com.edouardfouche.preprocess.DataRef

/**
  * Created by fouchee on 12.07.17.
  * Test the influence of M on the scores
  */
object ContrastM extends Experiment {
  override val alpha_range = Vector()
  override val M_range: Vector[Int] = (1 until 200).toVector
  override val nRep = 100 // number of repetition for each point
  override val data: Vector[DataRef] = Linear // those are a selection of subspaces of different dimensionality and noise

  def run(): Unit = {

    for {
      m <- M_range
    } yield {
      info(s"Starting com.edouardfouche.experiments with configuration: M=$m")

      val tests = defaulttests

      data.par.foreach(x => compareContrast(x, tests = tests))
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}
