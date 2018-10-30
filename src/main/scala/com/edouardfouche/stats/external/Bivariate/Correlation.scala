package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.CorrelationDependenceMeasure


case class Correlation() extends BivariateStats {
  val id = "Correlation"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    CorrelationDependenceMeasure.STATIC.dependence(data(0), data(1))
  }

}
