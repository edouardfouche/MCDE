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
package io.github.edouardfouche.index

import scala.annotation.tailrec

/**
  * Created by fouchee on 07.07.17.
  */
object Slicing2 extends Slicer[(Int,Float)] {
  /**
    * Produce a subspace slice by conditioning on all dimensions, except a reference dimension
    * @param m An index structure. Array of 2-D Tuple where the first element in the index, the second the rank
    * @param dimensions The set of dimensions of the subspaces
    * @param referenceDim The dimension that is considered as reference
    * @param sliceSize The size of the slice for each dimensions, determined by alpha
    * @return Returns an array of booleans. True corresponds to indexes included in the slice.
    */
  //TODO: Question : Is it problematic to slice on ties? Its seems not.
  def randomSlice(m: S, dimensions: Set[Int], referenceDim: Int, sliceSize: Int): Array[Boolean] = {
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    //require(dimensions.contains(referenceDim), "The reference dimensions should be contained in the set of dimensions")
    //require(sliceSize > 0, "sliceSize should be greater than 0")

    val logicalArray = Array.fill[Boolean](m(0).length)(true)
    for {dim <- dimensions.filter(_ != referenceDim)} {
      val sliceStart = scala.util.Random.nextInt((m(0).length - sliceSize).max(1))
      for {x <- 0 until sliceStart} {logicalArray(m(dim)(x)._1) = false}
      for {x <- sliceStart + sliceSize until m(0).length} {logicalArray(m(dim)(x)._1) = false}
    }
    logicalArray
  }

  // for S, do not have a reference dimension
  def allSlice(m: S, dimensions: Set[Int], sliceSize: Int): Array[Boolean] = {
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    //require(dimensions.contains(referenceDim), "The reference dimensions should be contained in the set of dimensions")
    //require(sliceSize > 0, "sliceSize should be greater than 0")

    val logicalArray = Array.fill[Boolean](m(0).length)(true)
    for {dim <- dimensions} {
      val sliceStart = scala.util.Random.nextInt((m(0).length - sliceSize).max(1))
      for {x <- 0 until sliceStart} {
        logicalArray(m(dim)(x)._1) = false
      }
      for {x <- sliceStart + sliceSize until m(0).length} {
        logicalArray(m(dim)(x)._1) = false
      }
    }
    logicalArray
  }

  def safeSlice(m: S, dimensions: Set[Int], referenceDim: Int, sliceSize: Int): Array[Boolean] = {
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    //require(dimensions.contains(referenceDim), "The reference dimensions should be contained in the set of dimensions")
    //require(sliceSize > 0, "sliceSize should be greater than 0")

    val logicalArray = Array.fill[Boolean](m(0).length)(true)

    val dims = dimensions.filter(_ != referenceDim).toArray.zipWithIndex
    val sliceStarts = dims.map(_ => scala.util.Random.nextInt((m(0).length - sliceSize).max(1)))
    val sliceEnds = sliceStarts.map(_ + sliceSize)


    for {dim <- dims} {
      for {x <- 0 until sliceStarts(dim._2)} {
        logicalArray(m(dim._1)(x)._1) = false
      }
      for {x <- sliceEnds(dim._2) until m(0).length} {
        logicalArray(m(dim._1)(x)._1) = false
      }
    }

    //println(s"LogicalArray: ${logicalArray.count(_ == true)}")
    //println(s"m(0).length: ${m(0).length}, m.length: ${m.length}, sliceStarts: ${sliceStarts}")

    // this is the backtracking step
    /*
    while (logicalArray.count(_ == true) > math.ceil(m(0).length * alpha)) {
      val nrep = logicalArray.count(_ == true) - math.ceil(m(0).length * alpha).toInt
      for {
        x <- 0 to nrep
      } {
        val dim = dims(scala.util.Random.nextInt(dims.size))
        if (scala.util.Random.nextInt(2) == 0) {
          logicalArray(m(dim._1)(sliceStarts(dim._2))._1) = false
          sliceStarts(dim._2) = sliceStarts(dim._2) + 1
        } else {
          sliceEnds(dim._2) = sliceEnds(dim._2) - 1
          logicalArray(m(dim._1)(sliceEnds(dim._2))._1) = false
        }
      }
    }
    //println(s"Corrected: ${logicalArray.count(_ == true)}")//, sliceStart: ${sliceStarts}")
    */
    logicalArray

  }


  // the slicing scheme used for conditional independence
  def simpleSlice(m: S, dimension: Int, sliceSize: Int): S = {
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    //require(dimensions.contains(referenceDim), "The reference dimensions should be contained in the set of dimensions")
    //require(sliceSize > 0, "sliceSize should be greater than 0")

    //val logicalArray = Array.fill[Boolean](m(0).length)(true)
    //for {x <- 0 until sliceStart} {logicalArray(m(dimension)(x)._1) = false}
    //for {x <- sliceStart + sliceSize until m(0).length} {logicalArray(m(dimension)(x)._1) = false}
    //val mm = flattenRank(m.map(x => x.zip(logicalArray).collect { case (v, true) => v }))

    val sliceStart = scala.util.Random.nextInt((m(0).length - sliceSize).max(1))
    val selectedIndexes = m(dimension).slice(sliceStart, sliceStart + sliceSize).map(_._1)
    val indexSet = selectedIndexes.toSet
    val mm = m.map(x => x filter (y => indexSet contains y._1))

    val indexMap = selectedIndexes.sorted.zipWithIndex.map(x => x._1 -> x._2).toMap

    mm.map(x => {
      val (col1, col2) = x.unzip
      val col1flat = col1.map(indexMap(_))
      val col2index = col2.zipWithIndex
      val col2flat = col2index.map(x => mean(col2index.filter(_._1 == x._1).map(_._2)))
      //col1flat.indices.map(x => (col1flat(x), col2flat(x), col3(x))).toArray
      col1flat.zip(col2flat)
    })
  }

  def restrictedSafeRandomSlice(m: S, dimensions: Set[Int], referenceDim: Int, alpha: Double): Array[Boolean] = {
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    //require(dimensions.contains(referenceDim), "The reference dimensions should be contained in the set of dimensions")
    //require(sliceSize > 0, "sliceSize should be greater than 0")

    val logicalArray = Array.fill[Boolean](m(0).length)(true)
    val possibleDim = dimensions.filter(_ != referenceDim)
    val selectedDim = scala.util.Random.shuffle(possibleDim.toList).take(scala.util.Random.nextInt(possibleDim.size) + 1).toArray.zipWithIndex // you don't want 0
    val sliceSize = (math.pow(alpha, 1.0 / selectedDim.size) * m(0).length).ceil.toInt // we took our selectedDim.size-1 because referenceDim in not included.

    val sliceStarts = selectedDim.map(_ => scala.util.Random.nextInt((m(0).length - sliceSize).max(1)))
    val sliceEnds = sliceStarts.map(_ + sliceSize)

    //println(s"ref: $referenceDim, selected: ${selectedDim mkString ","}, sliceSize: $sliceSize")
    // I tried and it looks like it works as expected :) (not sure about the quality yet)
    for {dim <- selectedDim} {
      //val sliceStart = scala.util.Random.nextInt(m(0).length - sliceSize)
      for {x <- 0 until sliceStarts(dim._2)} {
        //logicalArray(m(dim)(x)._1) = false
        logicalArray(m(dim._1)(x)._1) = false
      }
      for {x <- sliceEnds(dim._2) until m(0).length} {
        logicalArray(m(dim._1)(x)._1) = false
      }
    }

    while (logicalArray.count(_ == true) > math.ceil(m(0).length * alpha)) {
      val nrep = logicalArray.count(_ == true) - math.ceil(m(0).length * alpha).toInt
      for {
        x <- 0 to nrep
      } {
        val dim = selectedDim(scala.util.Random.nextInt(selectedDim.size))
        if (scala.util.Random.nextInt(2) == 0) {
          logicalArray(m(dim._1)(sliceStarts(dim._2))._1) = false
          sliceStarts(dim._2) = sliceStarts(dim._2) + 1
        } else {
          sliceEnds(dim._2) = sliceEnds(dim._2) - 1
          logicalArray(m(dim._1)(sliceEnds(dim._2))._1) = false
        }
      }
    }

    logicalArray
  }

  def restrictedRandomSlice(m: S, dimensions: Set[Int], referenceDim: Int, alpha: Double): Array[Boolean] = {
    //require(dimensions.forall(x => x>=0 & x < m.length), "The dimensions for deviation need to be greater or equal to 0 and lower than the total number of dimensions")
    require(dimensions.contains(referenceDim), "The reference dimensions should be contained in the set of dimensions")
    require(dimensions.size > 1, "The set of dimensions should be greater than 1")
    //require(sliceSize > 0, "sliceSize should be greater than 0")

    val logicalArray = Array.fill[Boolean](m(0).length)(true)
    val possibleDim = dimensions.filter(_ != referenceDim)
    val selectedDim = scala.util.Random.shuffle(possibleDim.toList).take(scala.util.Random.nextInt(possibleDim.size) + 1) // you don't want 0
    val sliceSize = (math.pow(alpha, 1.0 / selectedDim.size) * m(0).length).ceil.toInt // we took our selectedDim.size-1 because referenceDim in not included.
    //println(s"ref: $referenceDim, selected: ${selectedDim mkString ","}, sliceSize: $sliceSize")
    // I tried and it looks like it works as expected :) (not sure about the quality yet)
    for {dim <- selectedDim} {
      val sliceStart = scala.util.Random.nextInt((m(0).length - sliceSize).max(1))
      for {x <- 0 until sliceStart} {
        logicalArray(m(dim)(x)._1) = false
      }
      for {x <- sliceStart + sliceSize until m(0).length} {
        logicalArray(m(dim)(x)._1) = false
      }
    }
    logicalArray
  }

  /**
    * Return a "safe cut" in a reference vector, given an original random cut.
    * A "safe cut" is a cut that do not fall in the middle of a tie. In the case the value has ties, the cut should
    * fall on the first element of the tie, so that no tie will be splitted accross the 2 partitions
    * @param cut An index in the reference array
    * @param reference The reference array. It is an array of 2-Tuple, first element is the position in the data, the second is the rank
    * @return An index which is a safe cut (the original cut might be safe already)
    */
  def getSafeCut(cut: Int, index: S, reference: Int): Int = {
    //require(cut >= 0 & cut <= reference.length)
    val ref = index(reference)
    @tailrec def cutSearch(a: Int, inc: Int = 0, ref: T): Int = {
      // "It's easier to ask forgiveness than it is to get permission"
      try if(ref(a+inc)._2 != ref(a+inc-1)._2) return a+inc
      else {
        try if (ref(a - inc)._2 != ref(a - inc - 1)._2) return a - inc
        catch{case _: Throwable => return a-inc}
      }
      catch {case _: Throwable => return a+inc}
      cutSearch(a, inc+1, ref)
    }
    cutSearch(cut, 0, ref)
  }

  def getSafeLowerCut(cut: Int, reference: T): Int = {
    //require(cut >= 0 & cut <= reference.length)
    @tailrec def cutSearch(a: Int, inc: Int = 0, reference: T): Int = {
      try if (reference(a - inc)._2 != reference(a - inc - 1)._2) return a - inc
      catch {
        case _: Throwable => return a - inc
      }
      cutSearch(a, inc + 1, reference)
    }

    cutSearch(cut, 0, reference)
  }

  def getSafeUpperCut(cut: Int, reference: T): Int = {
    //require(cut >= 0 & cut <= reference.length)
    @tailrec def cutSearch(a: Int, inc: Int = 0, reference: T): Int = {
      try if (reference(a + inc)._2 != reference(a + inc - 1)._2) return a + inc
      catch {
        case _: Throwable => return a + inc
      }
      cutSearch(a, inc + 1, reference)
    }

    cutSearch(cut, 0, reference)
  }

}
