package io.github.edouardfouche.mcde

import io.github.edouardfouche.index.Index

/**
  * Created by fouchee on 07.07.17.
  */
trait Stats {
  type PreprocessedData <: Index // PreprocessedData are subtypes of Index, which are column oriented structures
  val id: String
  val alpha: Double
  val beta: Double
  val M: Int

  /**
    * @param input A data set (row oriented)
   */
  def preprocess(input: Array[Array[Double]]): PreprocessedData

  /**
    * @param m A data set (row oriented)
    */
  def contrast(m: Array[Array[Double]], dimensions: Set[Int]): Double = {
    this.contrast(this.preprocess(m), dimensions)
  }

  def contrast(m: PreprocessedData, dimensions: Set[Int]): Double

  /**
    * @param m A data set (row oriented)
    */
  def contrastMatrix(m: Array[Array[Double]]): Array[Array[Double]] = {
    this.contrastMatrix(this.preprocess(m))
  }

  def contrastMatrix(m: PreprocessedData): Array[Array[Double]]
}
