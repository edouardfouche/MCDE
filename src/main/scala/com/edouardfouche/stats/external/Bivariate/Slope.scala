package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.SlopeDependenceMeasure

case class Slope() extends BivariateStats {
  val id = "Slope"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    SlopeDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
