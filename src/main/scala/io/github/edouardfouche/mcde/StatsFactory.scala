package io.github.edouardfouche.mcde

object StatsFactory {
  def getTest(test: String, m: Int, alpha: Double, beta: Double,  calibrate: Boolean, parallelize: Int): Stats =
  test.toLowerCase match {
    case "ks" => KS(m, alpha, beta, calibrate, parallelize) // preferred
    case "mwb" => MWB(m, alpha, beta, calibrate, parallelize) // preferred
    case "mwz" => MWZ(m, alpha, beta, calibrate, parallelize)
    case "mwp" => MWP(m, alpha, beta, calibrate, parallelize) // preferred
  case _ => throw new Error(s"Unknown statistical test ${test}")
  }
}
