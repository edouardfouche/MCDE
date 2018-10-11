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

import breeze.stats.{mean, stddev}
import com.edouardfouche.generators._
import com.edouardfouche.preprocess.DataRef
import com.edouardfouche.stats.mcde.MWPr

/**
  * Created by fouchee on 12.07.17.
  *
  * This experiment investigate the empirical distribution of contrast of MWP as M increases from 1 to 500
  * For each value of M we compute contrast against 500 freshly generated independent and linear spaces and save the values
  * Each sample have size 5000 with 3 dimensions.
  */
object VarianceBoundM_ND extends Experiment {
  override val alpha_range = Vector()
  override val M_range: Vector[Int] = (1 to 500).toVector
  override val nRep = 500 // number of repetition for each point
  override val data: Vector[DataRef] = Vector()
  val n_range = Vector(100, 200, 500, 1000, 2000, 5000, 10000)
  val d_range = Vector(2,3,4,5)
  val noise=0.2

  def run(): Unit = {
    info(s"Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")
    info(s"Parameters:")
    info(s"M_range: ${M_range mkString ","}")
    info(s"nrep: $nRep")
    info(s"n: ${n_range mkString ","}")
    info(s"dims: ${d_range mkString ","}")
    info(s"Started on: ${java.net.InetAddress.getLocalHost.getHostName}")

    info(s"Preprocessing ...")

    val test = MWPr(500)
    val stat = MWPr(1)

    for {
      d <- d_range
      n <- n_range
    } {
      info(s"Starting for d: $d, n: $n")
      val linear = Linear(d, 0.2)
      val independent = Independent(d, 0.0)

      val generators = Vector(linear,
        independent)

      val generators_datasets = generators.map(x => {
        val dat = x.generate(n)
        val prep = MWPr().preprocess(dat)
        (x,dat,prep)
      })

      for{gendat <- generators_datasets} {
        val gen = gendat._1
        val data = gendat._2
        val data_preprocessed = gendat._3

        val estimates = (1 to 500).map(x => stat.contrast(data_preprocessed, data_preprocessed.indices.toSet))

        info(s"${gen.id}-${gen.noise}-${gen.nDim}-$n \t : (avg) : ${mean(estimates)}")
        info(s"${gen.id}-${gen.noise}-${gen.nDim}-$n \t : (std) : ${stddev(estimates)}")
      }

      utils.createFolderIfNotExisting(experiment_folder + "/data")

      for{gendat <- generators_datasets} {
        utils.saveDataSet(gendat._2, experiment_folder + "/data/" + s"${gendat._1.id}-${gendat._1.noise}-${gendat._1.nDim}")
      }

      for {
        m <- M_range.par
      } yield {
        estimateEmpiricalVarianceBound(m, n, d, nRep, generators_datasets)
      }
      info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")

    }
  }
}
