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
import io.github.edouardfouche.preprocess.Preprocess
import io.github.edouardfouche.mcde.StatsFactory
import io.github.edouardfouche.utils.StopWatch
import com.typesafe.scalalogging.LazyLogging


/**
  * Created by fouchee on 01.06.17.
  */


// this is a nice table: http://www.normaltable.com/
object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val unit = "ms"


    info("Working directory: " + System.getProperty("user.dir"))
    info("Raw parameters given: " + args.map(s => "\"" + s + "\"").mkString("[", ", ", "]"))

    val MCDE_Stats = Vector("mwp", "mwpi", "mwpr", "mwps", "mwpu", "ks")

    require(args.length > 0, "No arguments given. Please see README.md")
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

    if((aindex != 0 & (aindex != args.length)) & !(MCDE_Stats contains args(aindex))) throw new Error("Approch not found for -a, possible choices are MWP, KS, MWPi, MWPr, MWPs, MWPu")

    val plevel = if (pindex == 0 | (pindex == args.length)) {
      warn("Parallelism level not specified, running on single core.")
      0
    } else {
      val p = args(pindex).toInt

      if (p == 1) warn("Running with default parallelism level.")
      else warn(s"Running with parallelism level: $p")

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

      StatsFactory.getTest("MWP", M, 0.5, 0.5, plevel)
    } else {

      val mindex = (args indexWhere (_ == "-m")) + 1
      val M = if(mindex == 0 | (mindex == args.length)) {
        if(MCDE_Stats contains args(aindex).toLowerCase) {
          warn("M value not specified, using the default 50.")
        }
        50
      } else {
        args(mindex).toInt
      }
      StatsFactory.getTest(args(aindex), M, 0.5, 0.5, plevel)
    }

    val opening_CPUtime =  opening._1
    val opening_Walltime =  opening._2
    val data = opening._3

    val preprocessing = StopWatch.measureTime(approach.preprocess(data))
    val preprocessing_CPUtime =  preprocessing._1
    val preprocessing_Walltime =  preprocessing._2
    val preprocessed_data = preprocessing._3

    val result = if (args(tindex).toLowerCase == "estimatedependency") {

      info(s"Usage: -t EstimateDependency -f <file> -a <approach> -m <M> -d <dimensions> -p <plevel>")

      val dimensions = if(dindex == 0 | (dindex == args.length)) {
        warn("Dimensions not specified, computing the contrast on the full space of the input file.")
        data(0).indices.toSet
      } else {
        args(dindex).split(",").map(_.toInt).toSet
      }

      startJob(approach.contrast(preprocessed_data, dimensions))
    } else {
      if (args(tindex).toLowerCase == "estimatedependencymatrix") {
        info(s"Usage: -t EstimateDependencyMatrix -f <file> -a <approach> -m <M> -p <plevel>")

        startJob(approach.contrastMatrix(preprocessed_data))
      } else {
        throw new Error(s"Unknown argument -t ${args(tindex)}, possible values are = ['EstimateDependency', 'EstimateDependencyMatrix']. Please see README.md.")
      }
    }
    println(s"Data Loading time: \t ${opening_CPUtime} $unit (cpu), ${opening_Walltime} $unit (wall)")
    println(s"Preprocessing time: \t ${preprocessing_CPUtime} $unit (cpu), ${preprocessing_Walltime} $unit (wall)")
    println(s"Computation time: \t ${result._1} $unit (cpu), ${result._2} $unit (wall)")
    System.exit(0)

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

  def warn(s: String): Unit = logger.warn(s)
}
