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
import com.edouardfouche.stats.external._
import com.edouardfouche.stats.mcde.{KS, MWP, MWPr}


/**
  * Created by fouchee on 12.07.17.
  * Test the scalability of each approach w.r.t. n, the number of ponit
  */
object ScalabilityN extends Experiment {
  val alpha_range = Vector()
  val M_range: Vector[Int] = Vector()
  val nRep = 500 // number of data sets we generate to compute contrast
  val data: Vector[DataRef] = Vector()
  val N_range = Vector(10, 20, 50, 100, 200, 300, 500, 1000, 2000, 3000, 5000, 10000) // number of data points for each data set
  val generators: Vector[(Int) => (Double) => DataGenerator] = GeneratorFactory.independent
  val dims = Vector(3);

  def run(): Unit = {
    info(s"Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")
    info(s"Parameters:")
    info(s"M_range: q${M_range mkString ","}")
    info(s"nrep: $nRep")
    info(s"Datasets: ${data.map(_.id) mkString ","}")
    info(s"N_range: ${N_range mkString ","}")
    info(s"nDim: " + dims(0))
    info(s"Started on: ${java.net.InetAddress.getLocalHost.getHostName}")

    info(s"Starting com.edouardfouche.experiments with configuration, nDim: " + dims(0))
    for {
      n <- N_range
    } {
      info(s"Status: n: $n")
      for {
        r <- (1 to nRep)
      } yield {
        //val ks = KS(50, 0.1)
        val mwp = MWP(50, 0.5)
        //val mwpr = MWPr(50, 0.5)
        val uds = UDS()
        val cmi = CMI()
        val hics = HICS()
        val ii = II()
        val tc = TC()
        val ms = MS()
        val mac = MAC()

        val tests = Vector(mwp, uds, cmi, hics, ii, tc, ms, mac)

        generators.par.foreach(x => compareScalability(x, dims(0), n, tests, r))
      }
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}
