package io.github.edouardfouche.worksheets
import io.github.edouardfouche.generators.{Independent, Linear}
import io.github.edouardfouche.mcde.{KS, MWP}

object test extends App{

  override def main(args: Array[String]): Unit = {


    // https://stats.stackexchange.com/questions/389034/kolmogorov-smirnov-test-calculating-the-p-value-manually
    // https://en.wikipedia.org/wiki/Kolmogorov–Smirnov_test

    // Create linear data
    val linear_generator = Linear(2, 0.00001, "gaussian", 0)
    val linear = linear_generator.generate(1000)

    // Create independent data
    val independent_generator = Independent(20, 0.0, "gaussian", 0)
    val independent = independent_generator.generate(100)

    val ks = KS(10000)
    val mwp = MWP(1000)

    val D1 = ks.contrast(linear, (0 until 2).toSet)
    val D2 = ks.contrast(independent, (0 until 20).toSet)
    val M1 = mwp.contrast(linear, (0 until 2).toSet)
    val M2 = mwp.contrast(independent, (0 until 20).toSet)

    println(D1, D2)
    println(M1, M2)

  }
}
