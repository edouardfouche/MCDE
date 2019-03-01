package com.edouardfouche.stats.external.bivariate

import breeze.stats.distributions.Gaussian
import de.lmu.ifi.dbs.elki.math.statistics.dependence.JensenShannonEquiwidthDependenceMeasure

case class JSEquity() extends BivariateStats {
  val id = "Jensen Shannon Equity Width"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    JensenShannonEquiwidthDependenceMeasure.STATIC.dependence(data(0).map(x => x + Gaussian(0, 1).draw() * 0.0000000001), data(1).map(x => x + Gaussian(0, 1).draw() * 0.0000000001))
  }
}
