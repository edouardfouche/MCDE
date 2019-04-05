package io.github.edouardfouche.worksheets
import io.github.edouardfouche.generators.{Independent}
import io.github.edouardfouche.mcde.{KSP, MWP}

object bug extends App{
  override def main(args: Array[String]): Unit = {

    // TODO: Does not happen with lower n or KS, its probably because n1*n2 > maxint
    /**
      * See how handled for KSP (long + lim n,m -> Infi), does something similar exists for MWP?
      */

    val independent_generator = Independent(2, 0.0, "gaussian", 0)
    val independent = independent_generator.generate(1000000)
    val mwp = MWP(10)
    val ks = KSP(10)
    val a = mwp.contrast(independent, (0 until 2).toSet)
  }

}
