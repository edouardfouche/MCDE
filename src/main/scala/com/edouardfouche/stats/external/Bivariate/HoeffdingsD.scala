package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.HoeffdingsDDependenceMeasure


case class HoeffdingsD() extends BivariateStats {
  val id = "HoeffdingsD"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    HoeffdingsDDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
