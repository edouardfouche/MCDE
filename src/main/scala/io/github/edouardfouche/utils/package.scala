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
import java.io.{BufferedWriter, File, FileWriter}

//import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec

/**
  * Created by fouchee on 11.07.17.
  */
package object utils {
  def time[A](f: => A) = {
    val s = System.nanoTime
    val ret = f
    println("Time: " + (System.nanoTime - s) / 1e6 + "ms")
    ret
  }

  // expect rows
  def saveDataSet[T](res: Array[Array[T]], path: String): Unit = {
    val file = new File(path)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(s"${(1 to res(0).length) mkString ","} \n") // a little header
    res.foreach(x => bw.write(s"${x mkString ","} \n"))
    bw.close()
  }


  def save[T](res: Array[T], path: String): Unit = {
    val file = new File(path)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(res mkString ",")
    bw.close()
  }

  def saveSubspaces(res: Array[(Set[Int], Double)], path: String): Unit = {
    val file = new File(path)
    val bw = new BufferedWriter(new FileWriter(file))
    res.foreach(x => bw.write(s"${x._1 mkString ","} : ${x._2} \n"))
    bw.close()
  }

  def createFolderIfNotExisting(path: String): Unit = {
    val directory = new File(path)
    if (!directory.exists()) {
      directory.mkdir()
    }
  }

  def initiateSummaryCSV(path: String, header: String): FileWriter = {
    val fileA = new File(path)
    val fwA = new FileWriter(fileA, true) // append set to true
    fwA.write(header) // this is the header
    fwA
  }

  def extractFieldNames[T <: Product](implicit m: Manifest[T]) = m.runtimeClass.getDeclaredFields.map(_.getName)

  // Should return (False positive rate, True positive rate)
  // https://en.wikipedia.org/wiki/Receiver_operating_characteristic
  def getROC(labels: Array[Boolean], predictions: Array[Double]): Array[(Double, Double)] = {
    require(labels.length == predictions.length, "labels and predictions should have the same length")
    val sortedPairs = labels.zip(predictions).sortBy(-_._2) // order the pairs by decreasing order of prediction
    val nP = labels.count(_ == true).toDouble // number of positives
    val nN = labels.length - nP // number of negatives
    val fP_inc = 1/nN
    val tP_inc = 1/nP
    @tailrec def cumulative(sortedPairs: Array[(Boolean, Double)], acc: (Double, Double), result: Array[(Double, Double)]): Array[(Double, Double)] = {
      if(sortedPairs.isEmpty) result
      else if(sortedPairs.head._1) {
        val newAcc = (acc._1, acc._2 + tP_inc)
        cumulative(sortedPairs.tail, newAcc, result :+ newAcc)
      } else {
        val newAcc = (acc._1 + fP_inc, acc._2)
        cumulative(sortedPairs.tail, newAcc, result :+ newAcc)
      }
    }
    cumulative(sortedPairs, (0,0), Array())
  }

  def getAreaUnderCurveOfROC(labels: Array[Boolean], predictions: Array[Double]): (Double, Array[(Double, Double)]) = {
    require(labels.length == predictions.length, "labels and predictions should have the same length")
    val fpr_tpr = getROC(labels, predictions)
    @tailrec def cumulative(fpr_tpr: Array[(Double, Double)], res: Double): Double = {
      if(fpr_tpr.length == 1) res
      else cumulative(fpr_tpr.tail, res + (fpr_tpr.tail.head._1 - fpr_tpr.head._1)*fpr_tpr.head._2)
    }
    (cumulative(fpr_tpr, 0.0), fpr_tpr)
  }
}
