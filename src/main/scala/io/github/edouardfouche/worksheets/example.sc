/**
  * This file demonstrates how to use MWP in your own code.
  */


// Import all MWP classes or just the one you need
import io.github.edouardfouche.mcde._
// import io.github.edouardfouche.mcde.{MWP, MWB, MWPi, MWPr, MWPs, MWPu, MWZ, KS, S}

// Optionally import data generators for creating given dependencies
// For more information see: https://github.com/edouardfouche/DataGenerator
import io.github.edouardfouche.generators.Independent

// Generating linear bivariate data. Note that MWP classes expect an Array[Array[Double] which
// is tuple oriented (Array containing Arrays which contain each row/tuple). Therefore
// we transpose the data.
val attribute1: Array[Double] = (1 to 100).map(_.toDouble).toArray
val attribute2: Array[Double] = attribute1.map(x => x * 2)
val data1: Array[Array[Double]] = Array(attribute1, attribute2).transpose

// Generating linear multivariate data.
val attribute3: Array[Double] = attribute1.map(_ * 4)
val attribute4: Array[Double] = attribute1.map(_ * 6)
val data2: Array[Array[Double]] = Array(attribute1, attribute2, attribute3, attribute4).transpose

// Generating independent data using data generator class.
val data3 = Independent(5, 0.0, "gaussian", 0).generate(100000)



/**
  * Create an instance of MWP class. Note that MWP() is a case class.
  * @M:Int Number of repetitions. Default = 50
  * @alpha:Double Expected share of instances in slice (independent dimensions). Default = 0.5
  * @beta:Double Expected share of instances in marginal restriction (reference dimension). Default = 0.5
  * @calibrate:Boolean Default = false
  * @parallelize:Int Level of parallelization. Default = 0
  */

// MWP as described in the paper Monte Carlo Dependency Estimation
val mwp = MWP(M = 50, alpha = 0.5, beta = 0.5,  calibrate = false, parallelize = 0)
mwp.contrast(data1, Set(0,1)) // Note that MWP should generate values close to 1.0
mwp.contrast(data2, Set(0,1,2,3)) // Note that MWP should generate values close to 1.0
mwp.contrast(data3, Set(0,1,2,3,4)) // Note that MWP should generate values around to 0.5

// Note that you can choose to include only a subspace of your attributes e.g. only attribute 1 and 4
mwp.contrast(data2, Set(0,3))



// It works equivalently for the other variations of MWP
val mwpi = MWPi()
mwpi.contrast(data1, Set(0,1))
mwpi.contrast(data2, Set(0,1,2,3))
mwpi.contrast(data3, Set(0,1,2,3,4))

val mwpr = MWPr()
mwpr.contrast(data1, Set(0,1))
mwpr.contrast(data2, Set(0,1,2,3))
mwpr.contrast(data3, Set(0,1,2,3,4))

val mwps = MWPs()
mwps.contrast(data1, Set(0,1))
mwps.contrast(data2, Set(0,1,2,3))
mwps.contrast(data3, Set(0,1,2,3,4))

val mwpu = MWPu()
mwpu.contrast(data1, Set(0,1))
mwpu.contrast(data2, Set(0,1,2,3))
mwpu.contrast(data3, Set(0,1,2,3,4))

val mwz = MWZ()
mwz.contrast(data1, Set(0,1))
mwz.contrast(data2, Set(0,1,2,3))
mwz.contrast(data3, Set(0,1,2,3,4))

val s = S()
s.contrast(data1, Set(0,1))
s.contrast(data2, Set(0,1,2,3))
s.contrast(data3, Set(0,1,2,3,4))

val ks = KS()
ks.contrast(data1, Set(0,1))
ks.contrast(data2, Set(0,1,2,3))
ks.contrast(data3, Set(0,1,2,3,4))

val mwb = MWB()
mwb.contrast(data1, Set(0,1))
mwb.contrast(data2, Set(0,1,2,3))
mwb.contrast(data3, Set(0,1,2,3,4))