/*
 * Copyright (C) 2018 Edouard Fouché
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import com.edouardfouche.experiments.Data._
import com.edouardfouche.generators.{Hourglass, Independent, Linear, Zinv}
import com.edouardfouche.preprocess.Preprocess
import com.edouardfouche.stats.mcde.{KS, MWP}
import com.edouardfouche.stats.external.{HICS, II, MAC, UDS}


val gen1 = Independent(2, 0).generate(10000)
val gen2 = Linear(2, 0).generate(10000)
Linear(2, 0).saveSample()
gen1.length
gen1(0).length

val preprocess = MWP().preprocess(gen1)
preprocess.values.length

MWP().contrast(gen1, Set(0, 1))
MWP().contrast(gen2, Set(0, 1))

val IndependentData = independent_2D
val openedData = independent_2D.open()
val preprocessedData = independent_2D.openAndPreprocess(MWP())
MWP(M=1000).contrast(openedData, Set(0,1))
MWP(M=1000).contrast(preprocessedData, Set(0,1))

val data1 = Preprocess.open(getClass.getResource("/data/Linear-2-0.0.csv").getPath, header = 1, separator = ",", excludeIndex = false, dropClass = true)
val data2 = Preprocess.open(getClass.getResource("/data/Independent-2-0.0.csv").getPath, header = 1, separator = ",", excludeIndex = false, dropClass = true)
data1.length
data1(0).length

KS().deviation(data1, Set(0,1), 0)

MWP().contrast(data1, Set(0, 1))
MWP().contrast(data2, Set(0, 1))

MWP().contrast(independent_2D.open(), Set(0, 1))
MWP().contrast(linear_2D.open(), Set(0, 1))

HICS().contrast(independent_2D.open(), Set(0, 1))
HICS().contrast(linear_2D.open(), Set(0, 1))

II().contrast(independent_2D.open(), Set(0, 1))
II().contrast(linear_2D.open(), Set(0, 1))

MAC().contrast(MAC().preprocess(independent_2D.open()), Set(0, 1))
MAC().contrast(linear_2D.open(), Set(0, 1))

val independent = Independent(3, 0.0).generate(1000)
val linear = Linear(3, 1.0/30.0).generate(1000)
UDS().contrast(UDS().preprocess(independent), Set(0, 1, 2))
UDS().contrast(UDS().preprocess(linear), Set(0, 1, 2))
MAC().contrast(MAC().preprocess(independent), Set(0, 1, 2))
MAC().contrast(MAC().preprocess(linear), Set(0, 1, 2))

val z = Zinv(3, 1.0).generate(1000)
UDS().contrast(UDS().preprocess(z), Set(0, 1, 2))
MAC().contrast(MAC().preprocess(z), Set(0, 1, 2))

val hourglass = Hourglass(3, 0.73).generate(1000)
UDS().contrast(UDS().preprocess(hourglass), Set(0, 1, 2))
MAC().contrast(MAC().preprocess(hourglass), Set(0, 1, 2))

val a = scala.util.Random.shuffle(Array(0, 1, 1, 2, 3, 4, 5, 5, 5, 6, 7, 8, 9).toList).toArray
val c = MWP(50).preprocess(Array(a.map(_.toDouble)).transpose)
c(0)