package io.github.edouardfouche.utils

import org.apache.commons.math3.special.Erf

object HalfGaussian {
  def cdf(x: Double): Double = Erf.erf(x / math.sqrt(2))
}
