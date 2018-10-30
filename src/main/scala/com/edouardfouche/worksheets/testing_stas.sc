import de.lmu.ifi.dbs.elki.math.statistics.dependence._
import com.edouardfouche.stats.external.CMI

val data1: Array[Double] = (1 to 10).map(_.toDouble).toArray
val data2: Array[Double] = data1.map(x => x * 2)
val data3: Array[Array[Double]] = Array(data1, data2)


/**
  * On Eds List
  */

CorrelationDependenceMeasure.STATIC.dependence(data1, data2)
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
SURFINGDependenceMeasure.STATIC.dependence(data1, data2)
SlopeDependenceMeasure.STATIC.dependence(data1, data2)
SlopeInversionDependenceMeasure.STATIC.dependence(data1, data2)

val mwp = CMI()
mwp.








