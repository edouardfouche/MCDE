package io.github.edouardfouche.mcde

object StatsFactory {
  def getTest(test: String, m: Int, alpha: Double, beta: Double, parallelize: Int): Stats =
  test.toLowerCase match {
    case "ksp" => KSP(m, alpha, beta, parallelize) // preferred
    case "mwp" => MWP(m, alpha, beta, parallelize) // preferred
    case "mwpi" => MWPi(m, alpha, beta, parallelize)
    case "mwpr" => MWPr(m, alpha, beta, parallelize)
    case "mwps" => MWPs(m, alpha, beta, parallelize)
    case "mwpu" => MWPu(m, alpha, beta, parallelize)
  case _ => throw new Error(s"Unknown statistical test ${test}")
  }
}
