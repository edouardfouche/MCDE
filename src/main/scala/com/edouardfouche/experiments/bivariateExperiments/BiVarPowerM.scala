package com.edouardfouche.experiments

import breeze.stats.DescriptiveStats.percentile
import breeze.stats.{mean, stddev}
import com.edouardfouche.generators.{DataGenerator, GeneratorFactory, Independent}
import com.edouardfouche.preprocess.DataRef
import com.edouardfouche.stats.mcde.{KS, MWP, MWPr, MWPu}
import com.edouardfouche.stats.external.Bivariate._
import com.edouardfouche.utils.StopWatch

object BiVarPowerM extends BiVarExperiments  {
  val alpha_range = Vector()
  val nRep = 500 // number of data sets we use to estimate rejection rate
  val data: Vector[DataRef] = Vector()
  val N_range = Vector(1000) // number of data points for each data set
  val noiseLevels = 30
  val generators: Vector[(Int) => (Double) => DataGenerator] = GeneratorFactory.selected

  override val M_range: Vector[Int] = Vector(10, 50, 200, 500)
  override val tests = Vector()

  def run(): Unit = {
    info(s"Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")
    info(s"Parameters:")
    info(s"M_range: q${M_range mkString ","}")
    info(s"nrep: $nRep")
    info(s"Datasets: ${data.map(_.id) mkString ","}")
    info(s"N_range: ${N_range mkString ","}")
    info(s"dims: ${dims mkString ","}")
    info(s"noiseLevels: $noiseLevels")
    info(s"Started on: ${java.net.InetAddress.getLocalHost.getHostName}")

    for {
      m <- M_range
      nDim <- dims
      n <- N_range
    } yield {
      info(s"Starting com.edouardfouche.experiments with configuration M: ${m}, nDim: $nDim, n: $n")

      val tests = Vector(MWP(m, 0.5),  KS(m, 0.1), MWPr(m, 0.5), MWPu(m, 0.5), Correlation(), DistanceCorrelation(), HoeffdingsD(), HSM(), JSEquity(),
        MCE(), MutualInformation(), Slope(), SlopeInversion(), SpearmanCorrelation(), Surfing())


      var ThresholdMap90 = scala.collection.mutable.Map[String, Double]()
      var ThresholdMap95 = scala.collection.mutable.Map[String, Double]()
      var ThresholdMap99 = scala.collection.mutable.Map[String, Double]()

      var ValueMap = scala.collection.mutable.Map[String, Array[Double]]()

      info(s"Computing Null Distribution")
      for (test <- tests.par) {
        info(s"Preparing data sets for Computing Null Distribution")
        val preprocessing = (1 to nRep).par.map(x => {
          val data = Independent(nDim, 0.0).generate(n)
          StopWatch.measureTime(test.preprocess(data))
        }).toArray

        val prepCPUtime = preprocessing.map(_._1)
        val prepWalltime = preprocessing.map(_._2)
        val preprocessed = preprocessing.map(_._3)
        info(s"Done preparing data sets computing Null Distribution")

        // I think the collection of datasets is already parallel
        val values = preprocessed.map(x => {
          StopWatch.measureTime(test.contrast(x, x.indices.toSet))
        }).toArray

        val CPUtime = values.map(_._1)
        val Walltime = values.map(_._2)
        val contrast = values.map(_._3)
        //println(s"${test.id}: ${values.take(10) mkString ";"}")

        ThresholdMap90 += (test.id -> percentile(contrast, 0.90))
        ThresholdMap95 += (test.id -> percentile(contrast, 0.95))
        ThresholdMap99 += (test.id -> percentile(contrast, 0.99))

        val summary = ExperimentSummary()
        summary.add("refId", "0-Hypothesis")
        summary.add("nDim", nDim)
        summary.add("noise", 0)
        summary.add("n", n)
        summary.add("nRep", nRep)
        summary.add("testId", test.id)
        summary.add("alpha", test.alpha)
        summary.add("M", test.M)
        summary.add("powerAt90", contrast.count(_ > ThresholdMap90(test.id)).toDouble / nRep.toDouble)
        summary.add("powerAt95", contrast.count(_ > ThresholdMap95(test.id)).toDouble / nRep.toDouble)
        summary.add("powerAt99", contrast.count(_ > ThresholdMap99(test.id)).toDouble / nRep.toDouble)
        summary.add("thresholdAt90", ThresholdMap90(test.id))
        summary.add("thresholdAt95", ThresholdMap95(test.id))
        summary.add("thresholdAt99", ThresholdMap99(test.id))
        summary.add("avgContrast", mean(contrast))
        summary.add("stdContrast", stddev(contrast))
        summary.add("avgWalltime", mean(Walltime))
        summary.add("stdWalltime", stddev(Walltime))
        summary.add("avgCPUtime", mean(CPUtime))
        summary.add("stdCPUtime", stddev(CPUtime))

        summary.add("avgPrepWalltime", mean(prepWalltime))
        summary.add("stdPrepWalltime", stddev(prepWalltime))
        summary.add("avgPrepCPUtime", mean(prepCPUtime))
        summary.add("stdPrepCPUtime", stddev(prepCPUtime))
        summary.write(summaryPath)
      }
      info(s"Done Computing Null Distribution")

      generators.par.foreach(x => comparePower(x, nDim, n, tests, ThresholdMap90, ThresholdMap95, ThresholdMap99, noiseLevels))
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}

