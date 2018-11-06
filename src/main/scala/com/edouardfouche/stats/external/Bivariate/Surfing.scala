package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.SURFINGDependenceMeasure

case class Surfing() extends BivariateStats {
  val id = "Surfing"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    SURFINGDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
