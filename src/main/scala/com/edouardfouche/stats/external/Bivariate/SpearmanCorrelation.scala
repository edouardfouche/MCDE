package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.SpearmanCorrelationDependenceMeasure

case class SpearmanCorrelation() extends BivariateStats {
  val id: String = "Spearman Correlation"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    SpearmanCorrelationDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
