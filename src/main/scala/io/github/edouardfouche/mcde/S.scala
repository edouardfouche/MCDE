package io.github.edouardfouche.mcde

import io.github.edouardfouche.index.AdjustedRankIndex

/**
  * The idea is just to look how the number of points in slices deviate from the expected number
  *
  * @alpha Expected share of instances in slice (independent dimensions).
  * @beta  Expected share of instances in marginal restriction (reference dimension).
  *        Added with respect to the original paper to loose the dependence of beta from alpha.
  *        TODO: Not sure whether this is clever of not.
  */
case class S(M: Int = 50, alpha: Double = 0.5, beta: Double = 0.5, calibrate: Boolean = false, var parallelize: Int = 0) extends McdeStats {
  val id = "S"
  type PreprocessedData = AdjustedRankIndex

  def preprocess(input: Array[Array[Double]]): PreprocessedData = {
    new AdjustedRankIndex(input, 0) //TODO: seems that giving parallelize another value that 0 leads to slower execution, why?

  }

  /**
    * Compute a statistical test based on  Mann-Whitney U test using a reference vector (the indices of a dimension
    * ordered by the rank) and a set of Int that correspond to the intersection of the position of the element in the
    * slices in the other dimensions.
    *
    * @param reference      The original position of the elements of a reference dimension ordered by their rank
    * @param indexSelection An array of Boolean where true means the value is part of the slice
    * @return The Mann-Whitney statistic
    */
  def twoSample(index: PreprocessedData, reference: Int, indexSelection: Array[Boolean]): Double = 0.0

  /**
    * Compute the contrast of a subspace
    *
    * @param m          The indexes from the original data ordered by the rank of the points
    * @param dimensions The dimensions in the subspace, each value should be smaller than the number of arrays in m
    * @return The contrast of the subspace (value between 0 and 1)
    */
  override def contrast(m: PreprocessedData, dimensions: Set[Int]): Double = {
    // Sanity check
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    //require(alpha > 0 & alpha < 1, "alpha should be greater than 0 and lower than 1")
    //require(M > 0, "M should be greater than 0")

    val sliceSize = (math.pow(alpha, 1.0 / (dimensions.size - 1.0)) * m.numRows).ceil.toInt /// WARNING: Do not forget -1
    //val targetSampleSize = findClosestCoPrime(math.ceil(alpha*m(0).length).toInt, m(0).length)
    //println(s"sliceSize: $sliceSize = ${math.pow(alpha, 1.0 / (dimensions.size - 1.0))} * ${m(0).length}, dim: ${dimensions.size}, alpha: ${alpha}")
    val result = ((1 to M).map(i => {
      // can add par here
      //val referenceDim = scala.util.Random.shuffle(dimensions.toList).head
      val referenceDims = scala.util.Random.shuffle(dimensions.toList).take((scala.util.Random.nextInt(dimensions.size) + 1).max(2)) // at least 2
      val sliceSize = (math.pow(alpha, 1.0 / referenceDims.size) * m.numRows).ceil.toInt //  no -1 because slicing on all
      //val n = scala.util.Random.nextInt(dimensions.size)
      //val referenceDim = dimensions.toVector(n)
      //val randomS =
      //println(s"remaining: ${randomS.count(_ == true)}")
      val slice = m.allSlice(dimensions, sliceSize) //, ))targetSampleSize))
      math.abs(slice.count(_ == true) - m.numRows * alpha)
    }).sum / M) * 2
    // TODO: I would need some kind of decorator to do that without have to redefine at lower levels
    if (calibrate) Calibrator.calibrateValue(result, StatsFactory.getTest(this.id, this.M, this.alpha, this.beta, calibrate = false, parallelize = 0), dimensions.size, m(0).length) // calibrateValue(result, dimensions.size, alpha, M)
    else result
  }
}
