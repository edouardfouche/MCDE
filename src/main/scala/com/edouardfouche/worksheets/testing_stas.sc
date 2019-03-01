import de.lmu.ifi.dbs.elki.math.statistics.dependence._
import com.edouardfouche.stats.external.Bivariate._
import com.edouardfouche.stats.mcde.MWP
import org.apache.commons.math3.stat.correlation.KendallsCorrelation
import breeze.stats.distributions.Gaussian

import scala.util.Random


val data1: Array[Double] = (1 to 10).map(_.toDouble).toArray
val data2: Array[Double] = data1.map(x => x * 2)
val data3: Array[Array[Double]] = Array(data1, data2).transpose


def get_dim[T](arr: Array[Array[T]]): (Int, Int) = {
  (arr.length, arr(0).length)
}

get_dim(data3)

/**
  * On Eds List
  */

CorrelationDependenceMeasure.STATIC.dependence(data3(0), data3(1))
SpearmanCorrelationDependenceMeasure.STATIC.dependence(data1, data2)
MutualInformationEquiwidthDependenceMeasure.STATIC.dependence(data1, data2)
HoeffdingsDDependenceMeasure.STATIC.dependence(data1, data2)
DistanceCorrelationDependenceMeasure.STATIC.dependence(data1, data2)
// Kendalls Tau

/**
  * The others
  */

HSMDependenceMeasure.STATIC.dependence(data1, data2)
JensenShannonEquiwidthDependenceMeasure.STATIC.dependence(data1, data2)
MCEDependenceMeasure.STATIC.dependence(data1, data2)
SlopeDependenceMeasure.STATIC.dependence(data1, data2)
SlopeInversionDependenceMeasure.STATIC.dependence(data1, data2)

val ken = new KendallsCorrelation()
ken.correlation(data1, data2)

val own_ken = KendallsTau()
own_ken.contrast(data3, Set(0,1))

val corr = Correlation()

corr.contrast(data3, Set(0,1))



val slope = Slope()
slope.contrast(data3, Set(0,1))

val randArray1:Array[Double] = Array.fill(100)(Random.nextInt).map(x => x.toDouble)
val randArray2:Array[Double] = Array.fill(100)(Random.nextInt).map(_.toDouble)

val data4: Array[Array[Double]] = Array(randArray1, randArray2).transpose

slope.contrast(data4, Set(0,1))