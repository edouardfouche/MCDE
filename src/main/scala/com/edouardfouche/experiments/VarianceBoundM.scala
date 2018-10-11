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
object VarianceBoundM extends Experiment {
  override val alpha_range = Vector()
  override val M_range: Vector[Int] = (1 to 500).toVector
  override val nRep = 500 // number of repetition for each point
  override val data: Vector[DataRef] = Vector()
  val n = 1000
  val d =3
  val noise=0.2

  def run(): Unit = {
    info(s"Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")
    info(s"Parameters:")
    info(s"M_range: q${M_range mkString ","}")
    info(s"nrep: $nRep")
    info(s"n: $n")
    info(s"dims: $d")
    info(s"Started on: ${java.net.InetAddress.getLocalHost.getHostName}")

    info(s"Preprocessing ...")

    val test = MWPr(500)
    val stat = MWPr(1)

    val linear0 = Linear(d, 0.0)
    val linear1 = Linear(d, 0.1)
    val linear2 = Linear(d, 0.2)
    val linear3 = Linear(d, 0.3)
    val linear4 = Linear(d, 0.4)
    val linear5 = Linear(d, 0.5)
    val linear6 = Linear(d, 0.6)
    val linear7 = Linear(d, 0.7)
    val linear8 = Linear(d, 0.8)
    val linear9 = Linear(d, 0.9)
    val linear10 = Linear(d, 1.0)

    /*
    val cross0 = Cross(d, 0.0)
    val cross1 = Cross(d, 0.1)
    val cross2 = Cross(d, 0.2)
    val cross3 = Cross(d, 0.3)
    val cross4 = Cross(d, 0.4)
    val cross5 = Cross(d, 0.5)
    val cross6 = Cross(d, 0.6)
    val cross7 = Cross(d, 0.7)
    val cross8 = Cross(d, 0.8)
    val cross9 = Cross(d, 0.9)
    val cross10 = Cross(d, 1.0)

    val hollowcube = HypercubeGraph(d, 0.2)
    val hypercube = Hypercube(d, 0.2)
    val nc = NonCoexistence(d, 0.2)
    val sine = Sine(d, 1, 0.2)
    val sphere = HyperSphere(d, 0.2)
    val star = Star(d, 0.2)
    val straightLines = StraightLines(d, 0.2)
    val z = Z(d, 0.2)
    val zinv = Zinv(d, 0.2)
    */

    val cross0 = Cross(d, 0.0)
    val cross1 = Cross(d, 0.1)
    val cross2 = Cross(d, 0.2)

    val sine0 = Sine(d, 1, 0.0)
    val sine1 = Sine(d, 1, 0.1)
    val sine2 = Sine(d, 1, 0.2)

    val sphere0 = HyperSphere(d, 0.0)
    val sphere1 = HyperSphere(d, 0.1)
    val sphere2 = HyperSphere(d, 0.2)

    val zinv0 = Zinv(d, 0.0)
    val zinv1 = Zinv(d, 0.1)
    val zinv2 = Zinv(d, 0.2)

    val independent = Independent(d, 0.0)

    val generators = Vector(linear0, linear1, linear2, linear3, linear4, linear5,
                            linear6, linear7, linear8, linear9, linear10,
                            cross0, cross1, cross2,
                            sine0, sine1, sine2,
                            sphere0, sphere1, sphere2,
                            zinv0, zinv1, zinv2,
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

      info(s"${gen.id}.${gen.noise} \t : (avg) : ${mean(estimates)}")
      info(s"${gen.id}.${gen.noise} \t : (std) : ${stddev(estimates)}")
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
