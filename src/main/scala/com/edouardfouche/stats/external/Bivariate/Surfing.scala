package com.edouardfouche.stats.external.Bivariate

import breeze.stats.distributions.Gaussian
import de.lmu.ifi.dbs.elki.math.statistics.dependence.SURFINGDependenceMeasure

case class Surfing() extends BivariateStats {
  val id = "Surfing"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    SURFINGDependenceMeasure.STATIC.dependence(data(0).map(x => x + Gaussian(0, 1).draw() * 0.0000000001), data(1).map(x => x + Gaussian(0, 1).draw() * 0.0000000001))
  }
}
