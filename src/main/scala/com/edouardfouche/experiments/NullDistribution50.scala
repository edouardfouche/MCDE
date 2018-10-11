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

import com.edouardfouche.stats.mcde.MWP

/**
  * Created by fouchee on 09.08.17.
  * Look at the distribution of the tests with M=50
  */
object NullDistribution50 extends Experiment {
  override val alpha_range = Vector(0.1)
  override val M_range = Vector(50) // I am trying something here
  override val nRep = 10000 // number of repetition for each point
  override val data = Vector() // not using it in this experiment
  val N = 1000

  def run(): Unit = {

    for {
      alpha <- alpha_range
      m <- M_range
    } yield {
      info(s"Starting com.edouardfouche.experiments with configuration: n=$N, M: $m, alpha: $alpha nDim: 2")

      val tests = Vector(MWP(M = m))
      compareNullDistribution(nDim = 2, n = N, tests = tests)
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }

}
