package com.edouardfouche.experiments

import com.edouardfouche.stats.external.Bivariate._
import com.edouardfouche.stats.mcde._



trait BiVarExperiments extends Experiment {
  val M_range = Vector(50)
  val m = M_range(0)

  //TODO: More MCDE Test?
  //TODO: Kendlls Tau

  val tests = Vector(MWP(m, 0.5),  KS(m, 0.1), MWPr(m, 0.5), MWPu(m, 0.5), Correlation(), DistanceCorrelation(), HoeffdingsD(), HSM(), JSEquity(),
    MCE(), MutualInformation(), Slope(), SlopeInversion(), SpearmanCorrelation(), Surfing())

  final val dims = Vector(2)
}
