package io.github.edouardfouche.worksheets

import io.github.edouardfouche.generators.{Independent, Linear}
import io.github.edouardfouche.mcde.{KSP, MWP}


object test extends App {

  override def main(args: Array[String]): Unit = {

    // https://stats.stackexchange.com/questions/389034/kolmogorov-smirnov-test-calculating-the-p-value-manually
    // https://en.wikipedia.org/wiki/Kolmogorov–Smirnov_test
    // https://stats.stackexchange.com/questions/149595/ks-test-how-is-the-p-value-calculated

    def time[R](block: => R): R = {
      val t0 = System.nanoTime()
      val result = block    // call-by-name
      val t1 = System.nanoTime()
      println("Elapsed time: " + (t1 / 1e+6 - t0 / 1e+6) + "ms")
      result
    }

    // Create linear data
    val linear_generator = Linear(15, 0.0, "gaussian", 0)
    val linear = linear_generator.generate(100)
    val long_linear = linear_generator.generate(10000)

    // Create independent data
    val independent_generator = Independent(10, 0.0, "gaussian", 0)

    val mwp = MWP(1000)
    val ks = KSP(1000)


    val K1 = ks.contrast(linear, (0 until 2).toSet)
    val K2 = ks.contrast(linear, (0 until 15).toSet)
    val M1 = mwp.contrast(linear, (0 until 2).toSet)
    val M2 = mwp.contrast(linear, (0 until 15).toSet)
    val K3 = ks.contrast(long_linear, (0 until 15).toSet)
    val M3 = mwp.contrast(long_linear, (0 until 15).toSet)
    println("MWP and KS behave the same way for linear data. The issue that increasing dim strongly reduces KS score does not " +
      "appear that strongly. Note that it also works with 0 noise (which was not the case initially)")
    println("linear_2 KSP: " + K1 + " MWP: " + M1)
    println("linear_15 KSP: " + K2 + " MWP: " + M2)
    println("long_linear_15 KSP: " + K3 + " MWP: " + M3)


    println("")
    println("See Independent behaviour")
    val lst = List.fill(5)(2) ::: List.fill(5)(10)
    var a = 0
    for(a <- lst) {
      val independent = independent_generator.generate(100000)
      val t00 = System.nanoTime()
      val K4 = ks.contrast(independent, (0 until a).toSet)
      val t01 = System.nanoTime()
      val t0 = (math rint ((t01 - t00) / 1e+6) * 100) / 100

      val t10 = System.nanoTime()
      val M4 = mwp.contrast(independent, (0 until a).toSet)
      val t11 = System.nanoTime()
      val t1 = (math rint ((t11 - t10) / 1e+6) * 100) / 100

      println("Dims = " + a + "; KSP Score: " + K4 + ", Time (ms): "+ t0 + "; MWP " + M4 + ", Time (ms): " + t1)
    }

  }
}
