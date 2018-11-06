package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.JensenShannonEquiwidthDependenceMeasure

case class JSEquity() extends BivariateStats {
  val id = "Jensen Shannon Equity Width"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    JensenShannonEquiwidthDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
