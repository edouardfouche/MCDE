import io.github.edouardfouche.mcde._
import io.github.edouardfouche.generators._

val data1: Array[Double] = (1 to 10).map(_.toDouble).toArray
val data2: Array[Double] = data1.map(x => x * 2)
val data3: Array[Array[Double]] = Array(data1, data2).transpose

val data4: Array[Double] = data1.map(_ * 4)
val data5: Array[Double] = data1.map(_ * 6)
val data6: Array[Double] = data1.map(_ * 8)
val data7: Array[Array[Double]] = Array(data1, data2, data4, data5, data6).transpose

val indiArr = Independent(5, 0.0).generate(100000)


val mwp = MWP()
mwp.contrast(data3, Set(0,1))
mwp.contrast(data7, Set(0,1,2,3,4))
mwp.contrast(indiArr, Set(0,1,2,3,4))

val mwpi = MWPi()
mwpi.contrast(data3, Set(0,1))
mwpi.contrast(data7, Set(0,1,2,3,4))
mwpi.contrast(indiArr, Set(0,1,2,3,4))

val mwb = MWB()
mwb.contrast(data3, Set(0,1))
mwb.contrast(data7, Set(0,1,2,3,4))
mwb.contrast(indiArr, Set(0,1,2,3,4))

val ks = KS()
ks.contrast(data3, Set(0,1))
ks.contrast(data7, Set(0,1,2,3,4))
ks.contrast(indiArr, Set(0,1,2,3,4))

val mwpr = MWPr()
mwpr.contrast(data3, Set(0,1))
mwpr.contrast(data7, Set(0,1,2,3,4))
mwpr.contrast(indiArr, Set(0,1,2,3,4))

val mwps = MWPs()
mwps.contrast(data3, Set(0,1))
mwps.contrast(data7, Set(0,1,2,3,4))
mwps.contrast(indiArr, Set(0,1,2,3,4))

val mwpu = MWPu()
mwpu.contrast(data3, Set(0,1))
mwpu.contrast(data7, Set(0,1,2,3,4))
mwpu.contrast(indiArr, Set(0,1,2,3,4))

val mwz = MWZ()
mwz.contrast(data3, Set(0,1))
mwz.contrast(data7, Set(0,1,2,3,4))
mwz.contrast(indiArr, Set(0,1,2,3,4))

val s = S()
s.contrast(data3, Set(0,1))
s.contrast(data7, Set(0,1,2,3,4))
s.contrast(indiArr, Set(0,1,2,3,4))