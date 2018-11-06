package com.edouardfouche.experiments

import com.edouardfouche.generators.{DataGenerator, GeneratorFactory}
import com.edouardfouche.preprocess.DataRef
import com.edouardfouche.stats.external._
import com.edouardfouche.stats.mcde.{KS, MWP, MWPr}

object BiVarScalabilityN extends BiVarExperiments {

  val alpha_range = Vector()
  val nRep = 500 // number of data sets we generate to compute contrast
  val data: Vector[DataRef] = Vector()
  val N_range = Vector(10, 20, 50, 100, 200, 300, 500, 1000, 2000, 3000, 5000, 10000) // number of data points for each data set
  val generators: Vector[(Int) => (Double) => DataGenerator] = GeneratorFactory.independent

  def run(): Unit = {
    info(s"Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")
    info(s"Parameters:")
    info(s"M_range: q${M_range mkString ","}")
    info(s"nrep: $nRep")
    info(s"Datasets: ${data.map(_.id) mkString ","}")
    info(s"N_range: ${N_range mkString ","}")
    info(s"nDim: 3")
    info(s"Started on: ${java.net.InetAddress.getLocalHost.getHostName}")

    info(s"Starting com.edouardfouche.experiments with configuration, nDim: 3")
    for {
      n <- N_range
    } {
      info(s"Status: n: $n")
      for {
        r <- (1 to nRep)
      } yield {

        generators.par.foreach(x => compareScalability(x, 3, n, tests, r))
      }
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}

