package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.SlopeInversionDependenceMeasure

case class SlopeInversion() extends BivariateStats {
  val id = "Slope Inversion"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    SlopeInversionDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
