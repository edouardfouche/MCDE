package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.HSMDependenceMeasure

case class HSM() extends BivariateStats {
  val id = "HSM"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    HSMDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
