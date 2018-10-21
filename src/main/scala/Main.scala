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
import com.edouardfouche.experiments._
import com.edouardfouche.preprocess.Preprocess
import com.edouardfouche.stats.StatsFactory
import com.edouardfouche.utils.StopWatch
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by fouchee on 01.06.17.
  */

// example usage: sbt "run HiCS src/test/resources/iris.csv"
// or sbt package and then
// scala target/scala-2.11/subspacesearch_2.11-1.0.jar GMD src/test/resources/iris.csv
// or sbt assembly and then
// java -jar target/scala-2.11/SubspaceSearch-assembly-1.0.jar GMD src/test/resources/iris.csv
// A real example: scala target/scala-2.11/subspacesearch_2.11-1.0.jar GMD /home/fouchee/git/SubspaceSearch/src/test/resources/11-12_25-26_37-38-39_55-56-57_40-41-42-43_46-47-48-49_30-31-32-33-34_73-74-75-76-77_data.txt

// Experiment1
// java -jar /home/fouchee/git/SubspaceSearch/target/scala-2.11/SubspaceSearch-assembly-1.0.jar com.edouardfouche.experiments.KS_MWB_extern
// scala /home/fouchee/git/SubspaceSearch/target/scala-2.11/subspacesearch_2.11-1.0.jar com.edouardfouche.experiments.KS_MWB

// this is a nice table: http://www.normaltable.com/
object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val unit = "ms"


    info("Working directory: " + System.getProperty("user.dir"))
    info("Raw parameters given: " + args.map(s => "\"" + s + "\"").mkString("[", ", ", "]"))

    val MCDE_Stats = Vector("mwp")

    require(args.length > 0, "No arguments given. Please see README.md")

    if (args(0) startsWith "com.edouardfouche.experiments.") {
      StopWatch.start
      val result = startJob(experimentFactory(args(0)))

      val (cpu, wall) = StopWatch.stop(unit)
      println(s"Computation time: \t ${result._1} $unit (cpu), ${result._2} $unit (wall)")
      println(s"Total elapsed time: \t $cpu $unit (cpu), $wall $unit (wall)")
      System.exit(0)
    } else {
      //StopWatch.start
      require(args.length >= 4, "Arguments should consists in at least 2 items: The task '-t' to perform and the path '-f' to a file.")

      val tindex = (args indexWhere (_ == "-t")) + 1
      val aindex = (args indexWhere (_ == "-a")) + 1
      val dindex = (args indexWhere (_ == "-d")) + 1
      val rindex = (args indexWhere (_ == "-r")) + 1
      val pindex = (args indexWhere (_ == "-p")) + 1

      val pathindex = (args indexWhere (_ == "-f")) + 1

      if (tindex == 0 | (tindex == args.length)) throw new Error("Please specify the task to run using the '-t' flag.")

      val opening  = if (pathindex == 0 | (pathindex == args.length)) throw new Error("Please provide the path to a file using the '-f' flag.") else {
        if (!new java.io.File(args(pathindex)).exists) throw new Error(s"Path ${args(pathindex)} is unvalid")
        StopWatch.measureTime(Preprocess.open(args(pathindex), header = 1, separator = ",", excludeIndex = false, dropClass = true))
      }

      val plevel = if (pindex == 0 | (pindex == args.length)) {
        warn("Parallelism level not specified, running on single core.")
        0
      } else {
        val p = args(pindex).toInt
        if((args(tindex).toLowerCase == "estimatedependency") &  !(MCDE_Stats contains args(aindex).toLowerCase)) {
          warn("Parallelism is not supported for this approach.")
        } else {
          if (p == 1) warn("Running with default parallelism level.")
          else warn(s"Running with parallelism level: $p")
        }
        p
      }

      val approach = if(aindex == 0 | (aindex == args.length)) {
        warn("Approach not specified, using default MWP.")

        val mindex = (args indexWhere (_ == "-m")) + 1
        val M = if(mindex == 0 | (mindex == args.length)) {
          warn("M value not specified, using the default 50.")
          50
        } else {
          args(mindex).toInt
        }

        StatsFactory.getTest("MWP", M, 0.5, 0.5, false, plevel)
      } else {

        val mindex = (args indexWhere (_ == "-m")) + 1
        val M = if(mindex == 0 | (mindex == args.length)) {
          if(MCDE_Stats contains args(aindex).toLowerCase) {
            warn("M value not specified, using the default 50.")
          }
          50
        } else {
          if(!(MCDE_Stats contains args(aindex).toLowerCase)) {
            warn("Not an MCDE approach, the argument -m is ignored.")
          }
          args(mindex).toInt
        }
        StatsFactory.getTest(args(aindex), M, 0.5, 0.5, false, plevel)
      }

      val opening_CPUtime =  opening._1
      val opening_Walltime =  opening._2
      val data = opening._3

      val preprocessing = StopWatch.measureTime(approach.preprocess(data))
      val preprocessing_CPUtime =  preprocessing._1
      val preprocessing_Walltime =  preprocessing._2
      val preprocessed_data = preprocessing._3

      val result = if (args(tindex).toLowerCase == "estimatedependency") {
        if(MCDE_Stats contains args(aindex).toLowerCase) {
          info(s"Usage: -t EstimateDependency -f <file> -a <approach> -m <M> -d <dimensions> -p <plevel>")
        } else {
          info(s"Usage: -t EstimateDependency -f <file> -a <approach> -d <dimensions>")
        }

        val dimensions = if(dindex == 0 | (dindex == args.length)) {
          warn("Dimensions not specified, computing the contrast on the full space of the input file.")
          data(0).indices.toSet
        } else {
          args(dindex).split(",").map(_.toInt).toSet
        }

        startJob(approach.contrast(preprocessed_data, dimensions))
      } else {
        if (args(tindex).toLowerCase == "estimatedependencymatrix") {
          if(MCDE_Stats contains args(aindex).toLowerCase) {
            info(s"Usage: -t EstimateDependencyMatrix -f <file> -a <approach> -m <M> -p <plevel>")
          } else {
            info(s"Usage: -t EstimateDependencyMatrix -f <file> -a <approach> -p <plevel>")
          }
          startJob(approach.contrastMatrix(preprocessed_data))
        } else {
          throw new Error(s"Unknown argument -t ${args(tindex)}, possible values are = ['EstimateDependency', 'EstimateDependencyMatrix']. Please see README.md.")
        }
      }
      println(s"Data Loading time: \t ${opening_CPUtime} $unit (cpu), ${opening_Walltime} $unit (wall)")
      println(s"Preprocessing time: \t ${preprocessing_CPUtime} $unit (cpu), ${preprocessing_Walltime} $unit (wall)")
      println(s"Computation time: \t ${result._1} $unit (cpu), ${result._2} $unit (wall)")
      //val (cpu, wall) = StopWatch.stop(unit)
      //println(s"Total elapsed time: \t $cpu $unit (cpu), $wall $unit (wall)")
      System.exit(0)
    }


  }

  def info(s: String): Unit = logger.info(s)

  def startJob[R](block: => R, unit: String = "ms"): (Double, Double, R) = {
    val res = StopWatch.measureTime(block, unit)
    //pprint.pprintln(res._3)
    res._3 match {
      case a:Double => println(a)
      case a:Array[Array[Double]] => print_matrix(a)
      case _ => println("Unknown type")
    }
    def print_matrix(a: Array[Array[Double]]): Unit = {
      val matrix = a.map{x =>
        if(x.length > 10) (x.take(10).map(y => f"$y%1.2f") mkString "\t") ++ "\t ... (truncated)"
        else x.map(y => f"$y%1.2f") mkString "\t"
      }
      val toprint = if(matrix.length > 10)
        (matrix.take(10) ++ Array((1 to 10).map(x => "...") mkString "\t")) ++ Array("(truncated)")
      else matrix
      toprint.foreach{x => println(x)}
    }
    res
  }

  def experimentFactory(arg: String): Unit = arg match {
    case "com.edouardfouche.experiments.ContrastAlpha" => ContrastAlpha.run()
    case "com.edouardfouche.experiments.ContrastM" => ContrastM.run()
    case "com.edouardfouche.experiments.CalibrationAlpha" => CalibrationAlpha.run()
    case "com.edouardfouche.experiments.CalibrationM" => CalibrationM.run()
    case "com.edouardfouche.experiments.CalibrationN" => CalibrationN.run()
    case "com.edouardfouche.experiments.NullDistribution1" => NullDistribution1.run()
    case "com.edouardfouche.experiments.NullDistribution50" => NullDistribution50.run()
    case "com.edouardfouche.experiments.NullDistribution100" => NullDistribution100.run()

    case "com.edouardfouche.experiments.Power" => Power.run()
    case "com.edouardfouche.experiments.PowerAlpha" => PowerAlpha.run()
    case "com.edouardfouche.experiments.PowerDiscrete" => PowerDiscrete.run()
    case "com.edouardfouche.experiments.PowerM" => PowerM.run()
    case "com.edouardfouche.experiments.PowerN" => PowerN.run()

    case "com.edouardfouche.experiments.ScalabilityD" => ScalabilityD.run()
    case "com.edouardfouche.experiments.ScalabilityN" => ScalabilityN.run()

    case "com.edouardfouche.experiments.ParallelizationN" => ParallelizationN.run()

    case "com.edouardfouche.experiments.BoundM" => BoundM.run()
    case "com.edouardfouche.experiments.VarianceBoundM" => VarianceBoundM.run()
    case "com.edouardfouche.experiments.VarianceBoundM_ND" => VarianceBoundM_ND.run()

    case "com.edouardfouche.experiments.Scalability" => {
      ScalabilityD.run()
      ScalabilityN.run()
    }

    case "com.edouardfouche.experiments.MCDE" => {
      Power.run()
      PowerM.run()
      PowerN.run()
      PowerDiscrete.run()
      ScalabilityN.run()
      ScalabilityD.run()
      // ParallelizationN.run()
    }

    case _ => throw new Error(s"Unknown experiment $arg")
  }

  def warn(s: String): Unit = logger.warn(s)
}
