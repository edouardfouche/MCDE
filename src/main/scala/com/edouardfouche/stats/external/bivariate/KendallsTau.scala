package com.edouardfouche.stats.external.bivariate

import breeze.stats.distributions.Gaussian
import org.apache.commons.math3.stat.correlation.KendallsCorrelation

case class KendallsTau() extends BivariateStats {
  val id = "Kendalls Tau"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    val ken = new KendallsCorrelation()
    ken.correlation(data(0).map(x => x + Gaussian(0, 1).draw() * 0.0000000001), data(1).map(x => x + Gaussian(0, 1).draw() * 0.0000000001))
  }
}
