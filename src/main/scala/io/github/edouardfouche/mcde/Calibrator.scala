package io.github.edouardfouche.mcde

import io.github.edouardfouche.preprocess.Preprocessing

/**
  * Created by fouchee on 09.08.17.
  *
  * The idea of the calibration is to compute a theoretical maximum and minimum of each measure.
  * However, it turns out this is better to have a measure that is already calibrated by design.
  * (not really used anymore in practice)
  */
object Calibrator extends Preprocessing {
  // This is a structure for efficient calibration.
  val cache = new scala.collection.mutable.HashMap[(Int), (Double,Double)] // TODO: Load a pre-computed table.

  val tests = Vector("KS", "MWB", "MWZ", "MWP", "MWPC", "S")

  def preComputeCache(): Unit = {
    val values = for {
      test <- tests
      nDim <- 2 to 30
      alpha <- (1 to 9).map(_/10.0)
      m <- Vector(50,100,200)
      n <- Vector(100,1000)
    } yield { // the second alpha is because beta has been added but we do not want to manipulate beta here
      val instantiatedTest = StatsFactory.getTest(test, m, alpha, alpha, calibrate = false, parallelize = 0)
      def res = (getMinimum(instantiatedTest, prepareMinimumData(nDim, n)), getMaximum(instantiatedTest, prepareMaximumData(nDim, n)))
      cache.getOrElseUpdate(Vector(instantiatedTest.id, instantiatedTest.alpha, nDim, n).hashCode(), res)
    }
  }

  //val memoizedCalibrationParameters = memoize(getCalibrationParameters _)
  /**
    * Compute the calibration parameters for this statistical test. The calibration parameters consists in two values
    * cMin and cMax that correspond to the minimum and maximum values the test can obtain given a particular number
    * of dimensions and a particular value of alpha.
    *
    * @param nDim Number of dimensions in the subspace
    * @return (cMin, cMax) the minimum and maximum values to be used for calibration à la (x - cMin / (cMax - cMin))
    */
  def getCalibrationParameters(test: Stats, nDim: Int, n:Int): (Double, Double) = {
    // For debugging purpose it is interesting
    //if(cache.contains(Vector(test, alpha, nDim, n).hashCode())) println(s"cache hit for $test, $nDim, $alpha !")
    //else println(s"cache miss for ${test.id}, ${test.alpha}, $nDim, $n !")
    cache.getOrElseUpdate(Vector(test.id, test.alpha, nDim, n).hashCode(), (getMinimum(test, prepareMinimumData(nDim, n)), getMaximum(test, prepareMaximumData(nDim, n))))
  }

  def prepareMinimumData(nDim: Int, n: Int = 1000): Array[Array[Double]] = {
    (0 until nDim).map(x => (0 until n).map(x => scala.util.Random.nextDouble).toArray).toArray
  }
  // I keep the parameter M for experimental purpose, but I suggest not to use it
  def getMinimum(test: Stats, data: Array[Array[Double]]): Double = {
    val preprocessed = test.preprocess(data)
    (0 until 1000).map(x => test.contrast(preprocessed, data.indices.toSet)).sum / 1000
  }

  def prepareMaximumData(nDim: Int, n: Int = 1000): Array[Array[Double]] = {
    val vector = (0 until n).map(_.toDouble).toArray
    (0 until nDim).map(_ => vector).toArray
  }
  // I keep the parameter M for experimental purpose, but I suggest not to use it
  def getMaximum(test: Stats, data: Array[Array[Double]]): Double = {
    val preprocessed = test.preprocess(data)
    (0 until 1000).map(x => test.contrast(preprocessed, data.indices.toSet)).sum / 1000
  }

  /**
    * Important: test shall not be calibrated.
    */
  def calibrateValue(value: Double, test: Stats, nDim: Int, n:Int): Double = {
    //val uncalibrate = StatsFactory.getTest(test.id, test.M, test.alpha, false)
    val (cMin, cMax) = getCalibrationParameters(test, nDim, n)
    (value - cMin) / (cMax - cMin)
  }

}
