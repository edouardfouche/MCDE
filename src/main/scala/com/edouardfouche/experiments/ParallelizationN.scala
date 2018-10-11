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

import com.edouardfouche.generators.{DataGenerator, GeneratorFactory}
import com.edouardfouche.preprocess.DataRef
import com.edouardfouche.stats.mcde.{MWP, MWPr}

/**
  * Created by fouchee on 12.07.17.
  * Test the the influence of parallelization with increasing number of points N
  */
object ParallelizationN extends Experiment {
  override val alpha_range = Vector()
  override val M_range: Vector[Int] = Vector(50)
  override val nRep = 1000 // number of data sets we generate to compute contrast
  override val data: Vector[DataRef] = Vector()
  val N_range = Vector(500, 1000, 2000, 5000, 10000, 20000, 50000, 100000) // number of data points for each data set
  val par_range = Vector(0, 1, 2, 4, 8, 16, 32)
  val generators: Vector[(Int) => (Double) => DataGenerator] = GeneratorFactory.independent
  val d = 3

  def run(): Unit = {
    info(s"Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")
    info(s"Parameters:")
    info(s"M_range: q${M_range mkString ","}")
    info(s"nrep: $nRep")
    info(s"Datasets: ${data.map(_.id) mkString ","}")
    info(s"N_range: ${N_range mkString ","}")
    info(s"nDim: 3")
    info(s"Started on: ${java.net.InetAddress.getLocalHost.getHostName}")

    for {
      n <- N_range
      par <- par_range
      r <- (1 to nRep)
    } yield {
      val mwp = MWP(200, 0.5, parallelize = par)
      val mwpr = MWPr(200, 0.5, parallelize = par)
      val tests = Vector(mwp, mwpr)
      generators.par.foreach(x => compareParallelScalability(x, d, n, par, tests, r))
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}
