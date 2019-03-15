package io.github.edouardfouche.mcde

object StatsFactory {
  def getTest(test: String, m: Int, alpha: Double, beta: Double, parallelize: Int): Stats =
  test.toLowerCase match {
    case "ks" => KS(m, alpha, beta, parallelize) // preferred
    case "mwp" => MWP(m, alpha, beta, parallelize) // preferred
  case _ => throw new Error(s"Unknown statistical test ${test}")
  }
}
