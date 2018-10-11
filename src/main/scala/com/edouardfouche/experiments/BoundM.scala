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

import com.edouardfouche.generators.{Independent, Linear}
import com.edouardfouche.preprocess.DataRef
import com.edouardfouche.stats.mcde.MWPr
import com.edouardfouche.utils.StopWatch

/**
  * Created by fouchee on 12.07.17.
  *
  * This experiment investigate the empirical distribution of MWP as M increases from 1 to 500
  * For each value of M we compute contrast against 500 freshly generated independent and linear spaces and save the values
  * Each sample have size 1000 with 3 dimensions.
  */
object BoundM extends Experiment {
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


    val linear = Linear(d, noise)
    val independent = Independent(d, 0.0)

    val linear_data = linear.generate(n)
    val independent_data =  independent.generate(n)

    val linear_preprocessing = StopWatch.measureTime(MWPr().preprocess(linear_data))
    val independent_preprocessing = StopWatch.measureTime(MWPr().preprocess(independent_data))

    //val linear_prepCPUtime = linear_preprocessing._1
    //val linear_prepWalltime = linear_preprocessing._2
    val linear_preprocessed = linear_preprocessing._3

    //val independent_prepCPUtime = independent_preprocessing._1
    //val independent_prepWalltime = independent_preprocessing._2
    val independent_preprocessed = independent_preprocessing._3

    val test = MWPr(500)
    info(s"Linear      : ${test.contrast(linear_preprocessed, linear_preprocessed.indices.toSet)}")
    info(s"Independent : ${test.contrast(independent_preprocessed, independent_preprocessed.indices.toSet)}")

    utils.createFolderIfNotExisting(experiment_folder + "/data")
    utils.saveDataSet(linear_data, experiment_folder + "/data/" + s"linear-$n-$d")
    utils.saveDataSet(independent_data, experiment_folder + "/data/" + s"independent-$n-$d")

    for {
      m <- M_range.par
    } yield {
      estimateEmpiricalBound(m, n, d, nRep, linear_preprocessed, independent_preprocessed)
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")

  }
}
