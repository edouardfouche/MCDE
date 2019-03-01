package com.edouardfouche.stats.external.bivariate

import com.edouardfouche.index.NonIndex
import com.edouardfouche.stats.external.ExternalStats

// TODO: Implement Kendalls Tau

trait BivariateStats extends ExternalStats {
  val id: String
  val calibrate:Boolean = false
  var parallelize:Int = 0
  type PreprocessedData = NonIndex

  private def get_dim[T](arr: Array[Array[T]]): (Int, Int) = {
    (arr.length, arr(0).length)
  }

  def preprocess(input: Array[Array[Double]]): PreprocessedData = {
    require(get_dim(input)._2 == 2, "bivariate Measure only accepts 2-D row oriented Data")
    new NonIndex(input)
  }

  def contrast(m: PreprocessedData, dimensions: Set[Int]): Double = {
    val data: Array[Array[Double]] = Array(m(0), m(1))
    val s =  score(data)

    if (s.isNaN | s.isInfinite) {
      logger.info(s"$s value in ${this.getClass.getSimpleName} coerced to 0.0")
      0.0
    }
    else s
  }

  def score(data: Array[Array[Double]], preRank: Array[Array[Int]] = null): Double

}
