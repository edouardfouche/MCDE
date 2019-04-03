package io.github.edouardfouche.worksheets

import io.github.edouardfouche.generators.{Independent, Linear}
import io.github.edouardfouche.mcde.{KSP, MWP}
import io.github.edouardfouche.preprocess.Preprocess

import scala.annotation.tailrec
import scala.math.{E, pow, sqrt}

object test extends App {

  override def main(args: Array[String]): Unit = {

    // https://stats.stackexchange.com/questions/389034/kolmogorov-smirnov-test-calculating-the-p-value-manually
    // https://en.wikipedia.org/wiki/Kolmogorov–Smirnov_test
    // https://stats.stackexchange.com/questions/149595/ks-test-how-is-the-p-value-calculated

    // Create linear data
    val linear_generator = Linear(15, 0.0, "gaussian", 0)
    //linear_generator.save(100)
    // val path = s"${System.getProperty("user.dir")}/"
    // val linear = Preprocess.open(path + linear_generator.id + ".csv", header = 1, separator = ",", excludeIndex = false, dropClass = true)
    val linear = linear_generator.generate(100)

    // Create independent data
    // val independent_generator = Independent(2, 0.0, "gaussian", 0)
    // independent_generator.save(100)
    // val independent = Preprocess.open(path + independent_generator.id + ".csv", header = 1, separator = ",", excludeIndex = false, dropClass = true)

    val ks = KSP(10000)
    val mwp = MWP(1000)


    var D1 = ks.contrast(linear, (0 until 2).toSet)
    // var D2 = ks.contrast(independent, (0 until 2).toSet)
    val M1 = mwp.contrast(linear, (0 until 2).toSet)
    // val M2 = mwp.contrast(independent, (0 until 20).toSet)
    println(D1, M1)
    // println(M1, M2)

  }
}
