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
package io.github.edouardfouche.generators

import java.io.{BufferedWriter, File, FileWriter}

import breeze.stats.distributions.{Gaussian, Uniform}

trait DataGenerator {
  val nDim: Int
  val noise: Double
  val id: String

  def generate(n: Int): Array[Array[Double]]

  final def saveSample(path: String = s"${System.getProperty("user.home")}/datagenerator/", discretize: Int = 0): Unit = { // prevent overriding in subclasses so that TestDimensions using IndependentData are guaranteed to be correct. If override is necessary adjust test.
    val dir = new File(path).mkdirs()
    val data = if (discretize > 0) this.generate(1000, discretize)
    else this.generate(1000)

    // TODO: This is a duplicate from code in utils package. But somehow did not find how to import it
    def saveDataSet[T](res: Array[Array[T]], path: String): Unit = {
      val file = new File(path)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(s"${(1 to res(0).length) mkString ","} \n") // a little header
      res.foreach(x => bw.write(s"${x mkString ","} \n"))
      bw.close()
    }

    println(s"Writing output of $id to $path")
    saveDataSet(data, path + this.id + ".csv")
  }

  //plotting is not so useful anyway
  //def plot(path: String = "/home/fouchee/javaplot/", discretize: Int = 0): Unit

  def generate(n: Int, discretize: Int): Array[Array[Double]] = {
    Discretizer.discretize(generate(n: Int), discretize)
  }

  def postprocess(data: Array[Double], noise: Double, t: String = "gaussian"): Array[Double] = {
    t match {
      case "gaussian" => data.map(y => addGaussianNoise(y, noise))
      case "uniform" => data.map(y => addUniformNoise(y, noise))
    }
  }

  def addGaussianNoise(x: Double, noise: Double): Double = x + Gaussian(0, noise).draw()

  def addUniformNoise(x: Double, noise: Double): Double = x + Uniform(-noise / 2.0, noise / 2.0).draw()

  def linearNormalization(x: Double): Double = (x + noise / 2.0) / (1 + 2 * (noise / 2))

  def noiselessPowerNormalization(x: Double, pow: Double): Double = {
    var t = Array(1.0)
    for (y <- 2 to nDim) {
      t = t ++ Array(math.pow(t.sum, pow))
    }
    val max = t.last
    x / max
  }

  def powerNormalization(x: Double, pow: Double): Double = {
    var t = Array(1.0)
    for (y <- 2 to nDim) {
      t = t ++ Array(math.pow(t.sum, pow))
    }
    val max = t.last
    (x + noise / 2.0) / (max + 2 * (noise / 2))
  }

}
