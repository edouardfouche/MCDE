package com.edouardfouche.stats.external.Bivariate

import org.apache.commons.math3.stat.correlation.KendallsCorrelation

case class KendallsTau() extends BivariateStats {
  val id = "Kendalls Tau"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    val ken = new KendallsCorrelation()
    ken.correlation(data(0), data(1))
  }
}
