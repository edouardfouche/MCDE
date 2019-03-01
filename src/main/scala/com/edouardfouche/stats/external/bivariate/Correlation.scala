package com.edouardfouche.stats.external.bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.CorrelationDependenceMeasure
import breeze.stats.distributions.Gaussian


case class Correlation() extends BivariateStats {
  val id = "Correlation"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    CorrelationDependenceMeasure.STATIC.dependence(data(0).map(x => x + Gaussian(0, 1).draw() * 0.0000000001), data(1).map(x => x + Gaussian(0, 1).draw() * 0.0000000001))
  }
}
