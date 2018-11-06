package com.edouardfouche.stats.external.Bivariate

import de.lmu.ifi.dbs.elki.math.statistics.dependence.MutualInformationEquiwidthDependenceMeasure

case class MutualInformation() extends BivariateStats {
  val id = "Mutual Information"

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double = {
    MutualInformationEquiwidthDependenceMeasure.STATIC.dependence(data(0), data(1))
  }
}
