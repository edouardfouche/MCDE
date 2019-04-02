import io.github.edouardfouche.generators.Independent

import scala.math._
import io.github.edouardfouche.mcde.KS
import io.github.edouardfouche.index.RankIndex

// https://stats.stackexchange.com/questions/389034/kolmogorov-smirnov-test-calculating-the-p-value-manually
// https://en.wikipedia.org/wiki/Kolmogorov–Smirnov_test

// Create linear data
val attribute1: Array[Double] = (1 to 100).map(_.toDouble).toArray
val attribute2: Array[Double] = attribute1.map(x => x * 2)
val attribute3: Array[Double] = attribute1.map(x => x * 4)
val linear_2: Array[Array[Double]] = Array(attribute1, attribute2, attribute3).transpose

// Create independent data
val independent_generator = Independent(5, 0.0, "gaussian", 0)
val independent = independent_generator.generate(100)

val ks = KS()
val D1 = ks.contrast(linear_2, Set(0,1))
val D2 = ks.contrast(independent, Set(0,1))


val m = new RankIndex(independent)
m.index
val dimensions = Set(0,1,2)
dimensions.size
m.numRows
val sliceSize = (math.pow(0.5, 1.0 / (dimensions.size - 1.0)) * m.numRows).ceil.toInt
val slice = m.randomSlice(dimensions, 0, sliceSize)
slice.groupBy(identity).mapValues(_.size)
