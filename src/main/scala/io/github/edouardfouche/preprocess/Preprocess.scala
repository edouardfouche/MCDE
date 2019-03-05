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
package io.github.edouardfouche.preprocess

import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Created by fouchee on 02.05.17.
  */

/**
  * Encapsulate a few preprocessing steps (open a CSV file, compute the rank index structure).
  */
object Preprocess extends Preprocessing {
  /**
    * Helper function that redirects to openArff in case an arff is given else openCSV
    * @return A data set (row oriented)
    */
  def open(path: String, header: Int = 1, separator: String = ",", excludeIndex: Boolean = false, dropClass: Boolean = true, sample1000: Boolean = false): Array[Array[Double]] = {
    require(header >= 0, "header cannot be a negative number")
    require(separator.length == 1, "separator cannot be longer than 1")
    if (path.endsWith("arff")) openArff(path, dropClass, sample1000)
    else openCSV(path, header, separator, excludeIndex, dropClass, sample1000)
  }

  /**
    * Get the last column of a data file, assume it is the class and that it is numerical, even binary
    * @param path Path of the file in the system.
    * @param header Number of lines to discard (header), by default 1.
    * @param separator Number of lines to discard (header), by default 1.
    * @param excludeIndex Whether to exclude an index (the first column) or not.
    * @return The "class" column, should be an Array of Double
    *
    * @note This is quick and dirty, open normally by keeping the class and only keep the last column
    */
  def getLabels(path: String, header: Int = 1, separator: String = ",", excludeIndex: Boolean = false): Array[Boolean] = {
    require(header >= 0, "header cannot be a negative number")
    require(separator.length == 1, "Data separator cannot be longer than 1")
    val lastcolumns = if(path.endsWith("arff")) {
      openArff(path, dropClass = false).last
    } else openCSV(path, header, separator, excludeIndex, dropClass = false).last
    lastcolumns.map(x => if(x > 0) true else false)  // This trick should be done for the arff data HiCS synthetic
    // That's because the class column has a binary encoding which then trick the auc computation
  }

  /**
    * Get the columns names of a data set. Assumes the names are placed in the first line and separated by a comma.
    * @param path Path of the file in the system.
    * @param header Number of lines to discard (header), by default 1.
    * @param separator Number of lines to discard (header), by default 1.
    * @return An array of strings, where each string is a column name. Names are in the original order.
    *
    * @note This is quick and dirty, open normally by keeping the class and only keep the last column
    */
  def getColumnNames(path: String, header: Int = 1, separator: String = ","): Array[String] = {
    require(header >= 0, "header cannot be a negative number")
    require(separator.length == 1, "Data separator cannot be longer than 1")
    val bufferedSource = scala.io.Source.fromFile(path)
    bufferedSource.getLines().drop(header-1).next.split(separator)
  }

  /**
    * Get the columns names of a data set in a map, assigning the position index (integer) to the corresponding name (string)
    * @param path Path of the file in the system.
    * @param header Number of lines to discard (header), by default 1.
    * @param separator Number of lines to discard (header), by default 1.
    * @return An array of strings, where each string is a column name. Names are in the original order.
    *
    * @note This is quick and dirty, open normally by keeping the class and only keep the last column
    */
  def getColumnNamesMap(path: String, header: Int = 1, separator: String = ","): Map[Int,String] = {
    val names = getColumnNames(path, header, separator)
    names.zipWithIndex.map(x => (x._2,x._1)).toMap
  }


  /**
    * Open a csv file at a specified path. Currently, only handle numerical values.
    *
    * @param path      Path of the file in the system.
    * @param header    Number of lines to discard (header), by default 1.
    * @param separator Separator used, by default, comma.
    * @param excludeIndex Whether to exclude an index (the first column) or not.
    * @param dropClass Whether to drop the "class" column if there is one. (assumes it is the last one)
    * @param max1000 cap the opened data to 1000 rows. If the original data has more rows, sample 1000 without replacement
    * @return A 2-D Array of Double containing the values from the csv. (row-oriented)
    */
  def openCSV(path: String, header: Int = 1, separator: String = ",", excludeIndex: Boolean = false, dropClass: Boolean = true, max1000: Boolean = false): Array[Array[Double]] = {
    require(header >= 0, "header cannot be a negative number")
    require(separator.length == 1, "separator cannot be longer than 1")
    val bufferedSource = scala.io.Source.fromFile(path)
    //val result = bufferedSource.getLines.drop(header).map(x => x.split(separator).map(_.trim.toDouble)).toArray

    val result = if (!excludeIndex) {
      bufferedSource.getLines.filter(!_.isEmpty).drop(header).map(x => x.split(separator).map(_.trim)).toArray.transpose
    } else {
      val result = bufferedSource.getLines.filter(!_.isEmpty).drop(header).map(x => x.split(separator).map(_.trim)).toArray.transpose
      result.slice(1, result.length + 1)
    }
    bufferedSource.close()

    // This could be improved a bit, because toDouble is done twice

    val parser = result.map(x => Some(x.map(_.toDouble)))

    val data: Array[Array[Double]] = parser.collect{
      case Some(i) => i
    }

    //val data: Array[Array[Double]] = parser.flatten

    val droppedData = if(dropClass & header == 1) {
      val head = scala.io.Source.fromFile(path).getLines.next.split(" ")
      if(head.exists(_ contains "class")) data.init
      else data
    } else data

    val resultData = if(!max1000) droppedData
    else {
      if(droppedData(0).length < 1000) droppedData
      else {
        val indexes = scala.util.Random.shuffle(droppedData(0).indices.toList).take(1000).toArray
        data.map(x => indexes.map(x(_)))
      }
    }

    resultData.transpose
  }

  /**
    * Open an Arff file as a 2-D Array of Double
    *
    * @param path Path to the file in the current filesystem
    * @param dropClass Whether to drop the "class" column if there is one
    * @param max1000 cap the opened data to 1000 rows. If the original data has more rows, sample 1000 without replacement
    * @return A 2-D Array of Double containing the values for each numerical columns (row-oriented)
    * @note This method is inspired from the work of Fabian Keller
    */
  def openArff(path: String, dropClass: Boolean = true, max1000: Boolean = false): Array[Array[Double]] = {
    val lines = scala.io.Source.fromFile(path).getLines.toArray
    val numAttr = lines.count(x => x.toLowerCase.startsWith("@attribute"))
    //val attrNames = lines.filter(x => x.startsWith("@attribute")).map(line => line.split(" ")(1))

    val linesData = lines.drop(lines.indexWhere(x => x.toLowerCase == "@data") + 1).filter(x => x.split(",").length == numAttr)
    val numInst = linesData.length

    val matrix = Array.ofDim[Double](numInst, numAttr)

    var i = 0
    for (line <- linesData) {
      val fields = line.split(",")
      var j = 0
      for (el <- fields) {
        if (el == "'no'") matrix(i)(j) = 0.0
        else if (el == "'yes'") matrix(i)(j) = 1.0
        else matrix(i)(j) = el.toFloat
        j += 1
      }
      i += 1
    }

    val data = matrix.transpose

    val droppedData = if(!dropClass) data
    else {
      val attributes = lines.filter(x => x.toLowerCase.startsWith("@attribute")).map(_.split(" ")(1))
      //print(s"attributes: ${attributes mkString ","}")
      if (attributes.exists(_.toLowerCase contains "class") | attributes.exists(_.toLowerCase contains "outlier")) data.init
      else data
    }

    val resultData = if(!max1000) droppedData
    else {
      if(droppedData(0).length < 1000) droppedData
      else {
        val indexes = scala.util.Random.shuffle(droppedData(0).indices.toList).take(1000).toArray
        droppedData.map(x => indexes.map(x(_)))
      }
    }

    resultData.transpose
  }

  /**
    * Return the rank index structure (as in HiCS).
    *
    * Note that the numbers might be different in the case of ties, in comparison with other implementations.
    *
    * @param input A 2-D Array of Double (data set).
    * @return A 2-D Array of 2-D Tuple, where the first element is the original index, the second is its value (actually not in used for the KS test)
    */
  def ksRank(input: Array[Array[Double]], parallelize: Int = 0): Array[Array[(Int, Float)]] = {
    //if (parallelize == 0) input.map(_.zipWithIndex.sortBy(_._1).map(x => (x._2, x._1.toFloat, x._1)))
    if (parallelize == 0) input.map(_.zipWithIndex.sortBy(_._1).map(x => (x._2, x._1.toFloat)))
    else {
      val inputPar = input.par
      if (parallelize > 1) {
        //inputPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        inputPar.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      //inputPar.map(_.zipWithIndex.sortBy(_._1).map(x => (x._2, x._1.toFloat, x._1))).toArray
      inputPar.map(_.zipWithIndex.sortBy(_._1).map(x => (x._2, x._1.toFloat))).toArray
    }
  }

  /**
    * Return the rank index structure (as in HiCS).
    *
    * Note that the numbers might be different in the case of ties, in comparison with other implementations.
    *
    * @param input A 2-D Array of Double (data set, column-oriented).
    * @return A 2-D Array of Int, where the element is the original index in the unsorted data set
    */
  def ksRankSimple(input: Array[Array[Double]], parallelize: Int = 0): Array[Array[Int]] = {
    //if (parallelize == 0) input.map(_.zipWithIndex.sortBy(_._1).map(x => (x._2, x._1.toFloat, x._1)))
    if (parallelize == 0) input.map(_.zipWithIndex.sortBy(_._1).map(x => x._2))
    else {
      val inputPar = input.par
      if (parallelize > 1) {
        //inputPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        inputPar.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      //inputPar.map(_.zipWithIndex.sortBy(_._1).map(x => (x._2, x._1.toFloat, x._1))).toArray
      inputPar.map(_.zipWithIndex.sortBy(_._1).map(x => x._2)).toArray
    }
  }

  /**
    * Return the rank index structure for MWP, with adjusted ranks but no correction for ties.
    *
    * @param input A 2-D Array of Double (data set, column-oriented).
    * @return A 2-D Array of 2-D Tuple, where the first element is the original index, the second is its rank.
    */
  def mwRank(input: Array[Array[Double]], parallelize: Int): Array[Array[(Int, Float)]] = {
    // Create an index for each column with this shape: (original position, adjusted rank, original value)
    // They are ordered by rank
    val nonadjusted = if (parallelize == 0) input.map(x => x.zipWithIndex.sortBy(_._1).zipWithIndex.map(y => (y._1._2, y._2.toFloat, y._1._1)))
    else {
      val inputPar = input.par
      if (parallelize > 1) {
        //inputPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        inputPar.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      inputPar.map(x => x.zipWithIndex.sortBy(_._1).zipWithIndex.map(y => (y._1._2, y._2.toFloat, y._1._1))).toArray // (i1,i2,realValue)
    } // Obliged to synchronize here

    for {
      i <- if (parallelize == 0) nonadjusted.indices
      else {
        val indicesPar = nonadjusted.indices.par
        if (parallelize > 1) {
          //indicesPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
          indicesPar.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
        }
        indicesPar
      }
    } {
      val m = nonadjusted(i).length - 1
      var j = 0
      while (j <= m) {
        var k = j
        var acc = 0.0
        // && is quite important here, as if the first condition is false you don't want to evaluate the second
        while ((k < m) && (nonadjusted(i)(k)._3 == nonadjusted(i)(k + 1)._3)) { // Wooo we are comparing doubles here, is that ok? I guess yes
          acc += nonadjusted(i)(k)._2
          k += 1
        }
        if (k > j) {
          val newval = ((acc + nonadjusted(i)(k)._2) / (k - j + 1.0)).toFloat
          (j to k).foreach(y => nonadjusted(i)(y) = (nonadjusted(i)(y)._1, newval, nonadjusted(i)(y)._3))
          j += k - j + 1 // jump to after the replacement
        } else j += 1
      }
    }
    nonadjusted.map(x => x.map(y => (y._1,y._2)))
  }

  /**
    * Return the rank index structure for MWP, with adjusted ranks AND correction for ties.
    *
    * @param input A 2-D Array of Double (data set, column-oriented).
    * @return A 2-D Array of 3-D Tuple, where the first element is the original index, the second is its rank and the
    *         the last one a cumulative correction for ties.
    */
  def mwRankCorrectionCumulative(input: Array[Array[Double]],
                       parallelize: Int): Array[Array[(Int, Float, Double)]] = {
    // Create an index for each column with this shape: (original position, adjusted rank, original value)
    // They are ordered by rank
    val nonadjusted = if (parallelize == 0) input.map(x => x.zipWithIndex.sortBy(_._1).zipWithIndex.map(y => (y._1._2, y._2.toFloat, y._1._1)))
    else {
      val inputPar = input.par
      if (parallelize > 1) {
        //inputPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
        inputPar.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
      }
      inputPar.map(x => x.zipWithIndex.sortBy(_._1).zipWithIndex.map(y => (y._1._2, y._2.toFloat, y._1._1))).toArray // (i1,i2,realValue)
    } // Obliged to synchronize here

    // TODO: Probably this is the problem of the simultaneous access to a single data structure which decreases the performance of parallelization
    for {
      i <- if (parallelize == 0) nonadjusted.indices
      else {
        val indicesPar = nonadjusted.indices.par
        if (parallelize > 1) {
          //indicesPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelize))
          indicesPar.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelize))
        }
        indicesPar
      }
    } {

      val m = nonadjusted(i).length - 1
      var j = 0
      var acc_corr = 0.0
      while (j <= m) {
        var k = j
        var acc = 0.0
        // && is quite important here, as if the first condition is false you don't want to evaluate the second
        while ((k < m) && (nonadjusted(i)(k)._3 == nonadjusted(i)(k + 1)._3)) { // Wooo we are comparing doubles here, is that ok? I guess yes
          acc += nonadjusted(i)(k)._2
          k += 1
        }
        if (k > j) {
          val newval = ((acc + nonadjusted(i)(k)._2) / (k - j + 1.0)).toFloat
          val t = k - j + 1.0
          acc_corr = acc_corr + math.pow(t , 3) - t
          (j to k).foreach(y => nonadjusted(i)(y) = (nonadjusted(i)(y)._1, newval, acc_corr))
          j += k - j + 1 // jump to after the replacement
        } else {
          nonadjusted(i)(j) = (nonadjusted(i)(j)._1, nonadjusted(i)(j)._2, acc_corr)
          j += 1
        }
      }
    }
    nonadjusted
  }
}
