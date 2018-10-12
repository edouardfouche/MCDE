package com.edouardfouche.experiments

import java.io.{File, FileWriter}

import com.edouardfouche.generators.DataGenerator
import com.edouardfouche.index.RankIndex
import com.edouardfouche.preprocess.DataRef
import com.edouardfouche.stats._
import com.edouardfouche.stats.mcde._
import com.edouardfouche.utils.StopWatch
import org.slf4j.MDC
//import grizzled.slf4j.Logging
import breeze.stats.{mean, stddev}
import com.edouardfouche.stats.external.II
import com.typesafe.scalalogging.LazyLogging

//TODO: When we look at the logs of the experiments, we see that the external measure MAC and UDS randomly run into ArrayOutOfBoundException
// when this happens, we coerce the value to 0 (as a workaround). I could not find another better solution for now, but
// would be interesting to understand why and to correct it.

/**
  * Created by fouchee on 26.07.17.
  */
trait Experiment extends LazyLogging {
  val alpha_range: Vector[Double]
  val M_range: Vector[Int]
  val nRep: Int
  val data: Vector[DataRef]

  val defaultM = 50

  val output_folder: String = System.getProperty("user.dir")

  val master_experiment_folder: String = output_folder concat "/" concat "experiments"
  utils.createFolderIfNotExisting(master_experiment_folder)

  val formatter = new java.text.SimpleDateFormat("yyy-MM-dd-HH-mm")
  val dirname: String = s"${formatter.format(java.util.Calendar.getInstance().getTime)}_${this.getClass.getSimpleName.init}_"
  val experiment_folder: String = master_experiment_folder concat "/" concat dirname
  val summaryPath = experiment_folder + "/" + this.getClass.getSimpleName.init + ".csv"

  MDC.remove("path")
  MDC.clear()
  MDC.put("path", s"$experiment_folder/${this.getClass.getSimpleName.init}")
  // TODO: When we run several experiments (e.g. com.edouardfouche.experiments.MCDE) parts of the logs get
  // systematically written in the first log file (e.g. Power.log) this is not what we wanted.
  // It turns out that the logs written in the wrong log file are those that are issued in parallel threads.


  info(s"${formatter.format(java.util.Calendar.getInstance().getTime)} - Starting the experiment ${this.getClass.getSimpleName.init}\n")
  utils.createFolderIfNotExisting(experiment_folder)

  val defaulttests = Vector(MWB(defaultM, 0.5), MWZ(defaultM, 0.5), II())

  def estimateEmpiricalBound(m: Int, n: Int, d: Int, nrep: Int, linear_preprocessed: RankIndex, independent_preprocessed: RankIndex): Unit = {
    val test = MWPr(m)
    info(s"--- Starting ${test.id}, M=${test.M}, ${test.alpha}, $n, $d, $nrep")
    for(r <- (1 to nrep)) {
      val linear_values = StopWatch.measureTime(test.contrast(linear_preprocessed, linear_preprocessed.indices.toSet))
      val independent_values = StopWatch.measureTime(test.contrast(independent_preprocessed, independent_preprocessed.indices.toSet))

      val linear_CPUtime = linear_values._1
      val linear_Walltime = linear_values._2
      val linear_contrast = linear_values._3

      val independent_CPUtime = independent_values._1
      val independent_Walltime = independent_values._2
      val independent_contrast = independent_values._3

      val independent_summary = ExperimentSummary()
      independent_summary.add("refId", "I")
      independent_summary.add("nDim", d)
      independent_summary.add("n", n)
      independent_summary.add("testId", test.id)

      independent_summary.add("alpha", test.alpha)
      independent_summary.add("M", test.M)

      independent_summary.add("contrast", independent_contrast)
      independent_summary.add("walltime", independent_Walltime)
      independent_summary.add("CPUtime", independent_CPUtime)

      independent_summary.add("rep", r)
      independent_summary.write(summaryPath)

      val linear_summary = ExperimentSummary()
      linear_summary.add("refId", "L")
      linear_summary.add("nDim", d)
      linear_summary.add("n", n)
      linear_summary.add("testId", test.id)

      linear_summary.add("alpha", test.alpha)
      linear_summary.add("M", test.M)

      linear_summary.add("contrast", linear_contrast)
      linear_summary.add("walltime", linear_Walltime)
      linear_summary.add("CPUtime", linear_CPUtime)

      linear_summary.add("rep", r)
      linear_summary.write(summaryPath)
    }
  }

  def estimateEmpiricalVarianceBound(m: Int, n: Int, d: Int, nrep: Int, generators_datasets: Vector[(DataGenerator, Array[Array[Double]], RankIndex)]): Unit = {
    val test = MWPr(m)
    info(s"--- Starting ${test.id}, M=${test.M}, ${test.alpha}, $n, $d, $nrep")
    for(gendat <- generators_datasets) {
      for(r <- (1 to nrep)) {
        val values = StopWatch.measureTime(test.contrast(gendat._3, gendat._3.indices.toSet))

        val CPUtime = values._1
        val Walltime = values._2
        val contrast = values._3

        val summary = ExperimentSummary()
        summary.add("refId", gendat._1.id)
        summary.add("nDim", gendat._1.nDim)
        summary.add("noise", gendat._1.noise)
        summary.add("n", gendat._2.length)
        //summary.add("testId", test.id)

        //summary.add("alpha", test.alpha)
        summary.add("M", test.M)

        summary.add("contrast", contrast)
        //summary.add("walltime", Walltime)
        summary.add("CPUtime", CPUtime)

        summary.add("rep", r)
        summary.write(summaryPath)
      }
    }
  }


  def comparePower(generator: (Int) => (Double) => DataGenerator,
                   nDim: Int, n: Int, tests: Vector[Stats],
                   ThresholdMap90: scala.collection.mutable.Map[String, Double],
                   ThresholdMap95: scala.collection.mutable.Map[String, Double],
                   ThresholdMap99: scala.collection.mutable.Map[String, Double],
                   noiseLevels: Int): Unit = {

    for (noise <- (0 to noiseLevels).toArray.map(x => x.toDouble / noiseLevels.toDouble)) {
      val gen = generator(nDim)(noise)

      info(s"--- Computing ${gen.id}")
      val datasets = (0 until nRep).map(x => {
        gen.generate(n)
      })

      // Save data samples (debugging purpose)
      //utils.createFolderIfNotExisting(experiment_folder + "/data")
      //if (noise % 0.5 == 0) utils.saveDataSet(datasets(0), experiment_folder + "/data/" + s"${gen.id}-$n")

      for (test <- tests.par) {
        val preprocessing = datasets.map(x => {
          StopWatch.measureTime(test.preprocess(x))
        }).toArray

        val prepCPUtime = preprocessing.map(_._1)
        val prepWalltime = preprocessing.map(_._2)
        val preprocessed = preprocessing.map(_._3)

        val values = preprocessed.map(x => {
          StopWatch.measureTime(
            try {
              test.contrast(x, x.indices.toSet)
            } catch {
              case e: Exception => {
                info(s"Weird exception ${e.getMessage} ${e.toString} / test:${test.id}, gen: ${gen.id} coerced to 0.0")
                0.0
              }
            }
          )
        }).toArray

        val CPUtime = values.map(_._1)
        val Walltime = values.map(_._2)
        val contrast = values.map(_._3)
        val abscontrast = contrast.map(x => math.abs(x))

        val summary = ExperimentSummary()
        summary.add("refId", gen.id)
        summary.add("nDim", nDim)
        summary.add("noise", noise)
        summary.add("n", n)
        summary.add("nRep", nRep)
        summary.add("testId", test.id)

        test match {
          case x: McdeStats => {
            summary.add("alpha", x.alpha)
            summary.add("M", x.M)
          }
          case _ => {
            summary.add("alpha", "NULL")
            summary.add("M", "NULL")
          }
        }
        summary.add("powerAt90", abscontrast.count(_ > ThresholdMap90(test.id)).toDouble / nRep.toDouble)
        summary.add("powerAt95", abscontrast.count(_ > ThresholdMap95(test.id)).toDouble / nRep.toDouble)
        summary.add("powerAt99", abscontrast.count(_ > ThresholdMap99(test.id)).toDouble / nRep.toDouble)
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
    }
  }

  def info(s: String): Unit = logger.info(s)

  def compareScalability(generator: (Int) => (Double) => DataGenerator,
                         nDim: Int, n: Int, tests: Vector[Stats], r: Int): Unit = {
    val gen = generator(nDim)(0.0)

    val dataset = gen.generate(n)

    for (test <- tests.par) {
      val preprocessing = StopWatch.measureTime(test.preprocess(dataset))

      val prepCPUtime = preprocessing._1
      val prepWalltime = preprocessing._2
      val preprocessed = preprocessing._3

      val values = StopWatch.measureTime(
        try {
          test.contrast(preprocessed, preprocessed.indices.toSet)
        } catch {
          case e: Exception => {
            info(s"Weird exception ${e.getMessage} ${e.toString} / test:${test.id}, gen: ${gen.id} coerced to 0.0")
            0.0
          }
        }
        )

      val CPUtime = values._1
      val Walltime = values._2
      val contrast = values._3

      val summary = ExperimentSummary()
      summary.add("refId", gen.id)
      summary.add("nDim", nDim)
      summary.add("n", n)
      summary.add("nRep", nRep)
      summary.add("testId", test.id)

      test match {
        case x: McdeStats => {
          summary.add("alpha", x.alpha)
          summary.add("M", x.M)
        }
        case _ => {
          summary.add("alpha", "NULL")
          summary.add("M", "NULL")
        }
      }

      summary.add("Contrast", contrast)
      summary.add("Walltime", Walltime)
      summary.add("CPUtime", CPUtime)

      summary.add("prepWalltime", prepWalltime)
      summary.add("prepCPUtime", prepCPUtime)
      summary.add("r", r)

      summary.write(summaryPath)
    }
  }

  def compareParallelScalability(generator: (Int) => (Double) => DataGenerator,
                                 nDim: Int, n: Int, par: Int, tests: Vector[McdeStats], r: Int): Unit = {
    val gen = generator(nDim)(0.0)

    val dataset = gen.generate(n)

    for (test <- tests) {
      val preprocessing = StopWatch.measureTime(test.preprocess(dataset))

      val prepCPUtime = preprocessing._1
      val prepWalltime = preprocessing._2
      val preprocessed = preprocessing._3

      val values = StopWatch.measureTime(
        try {
          test.contrast(preprocessed, preprocessed.indices.toSet)
        } catch {
          case e: Exception => {
            info(s"Weird exception ${e.getMessage} ${e.toString} / test:${test.id}, gen: ${gen.id} coerced to 0.0")
            0.0
          }
        }
        )

      val CPUtime = values._1
      val Walltime = values._2
      val contrast = values._3

      val summary = ExperimentSummary()
      summary.add("refId", gen.id)
      summary.add("n", n)
      summary.add("nDim", nDim)
      summary.add("par", par)
      summary.add("nRep", nRep)
      summary.add("testId", test.id)
      summary.add("alpha", test.alpha)
      summary.add("M", test.M)

      summary.add("Walltime", Walltime)
      summary.add("CPUtime", CPUtime)
      summary.add("PrepWalltime", prepWalltime)
      summary.add("PrepWCPUtime", prepCPUtime)
      summary.add("r", r)

      summary.write(summaryPath)
    }
  }

  def comparePowerDiscrete(generator: (Int) => (Double) => DataGenerator,
                           nDim: Int, n: Int, disc: Int, tests: Vector[Stats],
                           ThresholdMap90: scala.collection.mutable.Map[String, Double],
                           ThresholdMap95: scala.collection.mutable.Map[String, Double],
                           ThresholdMap99: scala.collection.mutable.Map[String, Double],
                           noiseLevels: Int): Unit = {

    for (noise <- (0 to noiseLevels).toArray.map(x => x.toDouble / noiseLevels.toDouble)) {
      val gen = generator(nDim)(noise)

      val datasets = (0 until nRep).map(x => {
        gen.generate(n, disc)
      })

      // Save data samples (debugging purpose)
      //utils.createFolderIfNotExisting(experiment_folder + "/data")
      // if (noise % 0.5 == 0) utils.saveDataSet(datasets(0), experiment_folder + "/data/" + s"${gen.id}-$n-$disc")

      for (test <- tests.par) {
        val preprocessing = datasets.par.map(x => {
          StopWatch.measureTime(test.preprocess(x))
        }).toArray

        val prepCPUtime = preprocessing.map(_._1)
        val prepWalltime = preprocessing.map(_._2)
        val preprocessed = preprocessing.map(_._3)

        val values = preprocessed.map(x => {
          StopWatch.measureTime(
            try {
              test.contrast(x, x.indices.toSet)
            } catch {
              case e: Exception => {
                // some tests (such as UDS) create much unexpected exceptions. This is just to catch them.
                // info(s"Weird exception ${e.getMessage} ${e.toString} / test:${test.id}, gen: ${gen.id} coerced to 0.0")
                0.0
              }
            }
            )
        }).toArray

        val CPUtime = values.map(_._1)
        val Walltime = values.map(_._2)
        val contrast = values.map(_._3)

        val summary = ExperimentSummary()
        summary.add("refId", gen.id)
        summary.add("nDim", nDim)
        summary.add("noise", noise)
        summary.add("n", n)
        summary.add("d", disc)
        summary.add("nRep", nRep)
        summary.add("testId", test.id)
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
    }
  }

  def compareNullDistribution(nDim: Int, n: Int, tests: Vector[Stats]): Unit = {
    // Question: Why can't I call the trait?

    for {
      test <- tests.par
    } {
      info(s"Starting with test $test")
      for {
        rep <- 0 until nRep
      } {
        if (rep % 1000 == 0) info(s"Reached iteration $rep")
        val (prepCPUtimeMin, prepWalltimeMin, minData) = StopWatch.measureTime(Calibrator.prepareMinimumData(nDim, n)) // Test with minimum data.
        val (prepCPUtimeMax, prepWalltimeMax, maxData) = StopWatch.measureTime(Calibrator.prepareMaximumData(nDim, n)) // Test with maximum data.
        val (runCPUtimeMax, runWalltimeMax, contrastMax) = StopWatch.measureTime(test.contrast(maxData, maxData.indices.toSet))
        val (runCPUtimeMin, runWalltimeMin, contrastMin) = StopWatch.measureTime(test.contrast(minData, minData.indices.toSet))

        //val (maxTime, maxVal) = StopWatch.measureCPUTime(test.contrast(maxData, minData.indices.toSet, alpha, M=nIteration))

        val summary = ExperimentSummary()
        summary.add("testId", test.id)
        summary.add("nDim", minData.length)
        summary.add("n", n)
        summary.add("alpha", test.alpha)
        summary.add("M", test.M)

        summary.add("prepCPUtimeMin", prepCPUtimeMin)
        summary.add("prepWalltimeMin", prepWalltimeMin)

        summary.add("prepCPUtimeMax", prepCPUtimeMax)
        summary.add("prepWalltimeMax", prepWalltimeMax)

        summary.add("runCPUtimeMin", runCPUtimeMin)
        summary.add("runWalltimeMin", runWalltimeMin)

        summary.add("runCPUtimeMax", runCPUtimeMax)
        summary.add("runWalltimeMax", runWalltimeMax)

        summary.add("contrastMin", contrastMin)
        summary.add("contrastMax", contrastMax)
        summary.add("rep", rep)

        summary.write(summaryPath)
      }
    }
  }


  def compareContrast(ref: DataRef, tests: Vector[Stats]): Unit = {
    for {
      test <- tests
      rep <- 0 until nRep
    } {
      val raw = ref.open(max1000 = false)
      val (prepCPUtime, prepWalltime, data) = StopWatch.measureTime(test.preprocess(raw))

      val (runCPUtime, runWalltime, contrast) = StopWatch.measureTime(test.contrast(data, data.indices.toSet))

      val summary = ExperimentSummary()
      summary.add("refId", ref.id)
      summary.add("refCategory", ref.category)
      summary.add("testId", test.id)
      summary.add("nDim", data.numCols)
      summary.add("n", raw(0).length)
      summary.add("alpha", test.alpha)
      summary.add("M", test.M)

      summary.add("prepCPUtime", prepCPUtime)
      summary.add("prepWalltime", prepWalltime)

      summary.add("runCPUtime", runCPUtime)
      summary.add("runWalltime", runWalltime)

      summary.add("contrast", contrast)
      summary.add("rep", rep)

      summary.write(summaryPath)
    }
  }

  def compareCalibration(nDim: Int, tests: Vector[Stats], n: Int = 1000): Unit = {
    for {
      rep <- 0 until nRep
    } {
      val (prepCPUtimeMin, prepWalltimeMin, minData) = StopWatch.measureTime(Calibrator.prepareMinimumData(nDim, n))
      val (prepCPUtimeMax, prepWalltimeMax, maxData) = StopWatch.measureTime(Calibrator.prepareMaximumData(nDim, n))

      for {
        test <- tests
      } {
        val (runCPUtimeMin, runWalltimeMin, contrastMin) = StopWatch.measureTime(test.contrast(minData, minData.indices.toSet))
        val (runCPUtimeMax, runWalltimeMax, contrastMax) = StopWatch.measureTime(test.contrast(maxData, maxData.indices.toSet))

        val summary = ExperimentSummary()
        summary.add("testId", test.id)
        summary.add("nDim", nDim)
        summary.add("n", n)
        summary.add("alpha", test.alpha)
        summary.add("M", test.M)

        summary.add("prepCPUtimeMin", prepCPUtimeMin)
        summary.add("prepWalltimeMin", prepWalltimeMin)
        summary.add("prepCPUtimeMax", prepCPUtimeMax)
        summary.add("prepWalltimeMax", prepWalltimeMax)

        summary.add("runCPUtimeMin", runCPUtimeMin)
        summary.add("runWalltimeMin", runWalltimeMin)

        summary.add("runCPUtimeMax", runCPUtimeMax)
        summary.add("runWalltimeMax", runWalltimeMax)

        summary.add("contrastMin", contrastMin)
        summary.add("contrastMax", contrastMax)
        summary.add("rep", rep)

        summary.write(summaryPath)
      }
    }
  }

  case class ExperimentSummary() {
    var results: List[(String, Any)] = List()

    def add(name: String, v: Any): Unit = results = results :+ (name, v)

    def write(path: String): Unit = {
      synchronized {
        val fileA = new File(path)
        val fwA = new FileWriter(fileA, true) // append set to true
        if (fileA.length() == 0) {
          fwA.write(getHeader) // this is the header
          fwA.write(this.toString) // this is the string
          fwA.flush()
          fwA.close()
        } else {
          fwA.write(this.toString) // this is the string
          fwA.flush()
          fwA.close()
        }

      }
    }

    override def toString: String = (results.map(x => x._2.toString) mkString ",") + "\n"

    def getHeader: String = (results.map(x => x._1.toString) mkString ",") + "\n"
  }
}
