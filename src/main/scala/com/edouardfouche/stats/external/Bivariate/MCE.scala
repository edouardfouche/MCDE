package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.MCEDependenceMeasure

case class MCE() extends BivariateStats {
  val id = "MCE"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    MCEDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
