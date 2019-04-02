import io.github.edouardfouche.generators.Independent

import scala.math._
import io.github.edouardfouche.mcde.KS

// https://stats.stackexchange.com/questions/389034/kolmogorov-smirnov-test-calculating-the-p-value-manually
// https://en.wikipedia.org/wiki/Kolmogorov–Smirnov_test

// Create linear data
val attribute1: Array[Double] = (1 to 100).map(_.toDouble).toArray
val attribute2: Array[Double] = attribute1.map(x => x * 2)
val linear_2: Array[Array[Double]] = Array(attribute1, attribute2).transpose

// Create independent data
val independent_generator = Independent(5, 0.0, "gaussian", 0)
val independent = independent_generator.generate(100000)

val ks = KS()
val D1 = ks.contrast(linear_2, Set(0,1))
val D2 = ks.contrast(independent, Set(0,1))


def get_score3(D: Double, n1: Int, n2: Int): Double = {
  val z = D * sqrt(n1*n2 / (n1+n2))
  def exp(k: Int):Double = pow(-1, k-1) * pow(E, -2 * pow(k,2) * pow(z, 2))

  def loop(sumation: Double, i: Int, end: Int):Double = {
    if(i == end) exp(i) + sumation
    else loop(exp(i) + sumation, i+1, end)
  }

  1 - 2 * loop(0, 1, 10000)
}

// See Rscript, given D yields same result
1 - get_score3(0.5, 100, 100) // Linear from R
1 - get_score3(0.06, 100, 100) // Independent from R

// Data create in Scala, D values from HICS are quite different, which is hopefully expected?
get_score3(D1, 100, 100) // Linear
get_score3(D2, 100000, 100000) // Independent
