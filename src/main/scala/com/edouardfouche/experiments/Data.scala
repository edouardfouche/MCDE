package com.edouardfouche.experiments

import java.io.File

import com.edouardfouche.preprocess.DataRef

/**
  * Created by fouchee on 26.07.17.
  */
object Data {
  val home: String = System.getProperty("user.home")

  lazy val independent_2D = DataRef("Independent-2-0.0", getClass.getResource("/data/Independent-2-0.0.csv").getPath, 1, ",", "Independent")
  lazy val linear_2D = DataRef("Linear-2-0.0", getClass.getResource("/data/Linear-2-0.0.csv").getPath, 1, ",", "Linear")

  lazy val Linear_folder = new File(getClass.getResource("/data/").getPath)
  lazy val Linear = Linear_folder.listFiles.filter(_.isFile).map(_.toString).toVector.filter(x => x.split("/").last startsWith "Linear").map(x =>
    DataRef(x.split("/").last, x, 1, ",", "Linear"))
}
