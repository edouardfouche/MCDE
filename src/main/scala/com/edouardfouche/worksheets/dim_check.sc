import com.edouardfouche.generators._
import com.edouardfouche.stats.Stats
import com.edouardfouche.stats.external._
import com.edouardfouche.stats.mcde._
import com.edouardfouche.index._

// TODO: What if new generators?

// Checking Generators

val rows = 50
val dims = 2

val arr = Independent(dims, 0.0).generate(rows)
val arr2 = Cross(dims, 0.0).generate(rows)

val all_gens = List(Cross(dims, 0.0).generate(rows), Cubic(1,dims, 0.0).generate(rows), DoubleLinear(1,dims, 0.0).generate(rows),
  Hourglass(dims, 0.0).generate(rows), Hypercube(dims, 0.0).generate(rows), HypercubeGraph(dims, 0.0).generate(rows),
  HyperSphere(dims, 0.0).generate(rows), Independent(dims, 0.0).generate(rows), Linear(dims, 0.0).generate(rows), LinearPeriodic(1, dims, 0.0).generate(rows),
  LinearStairs(4, dims, 0.0).generate(rows), LinearThenDummy(dims, 0.0).generate(rows), LinearThenNoise(dims, 0.0).generate(rows),
  NonCoexistence(dims, 0.0).generate(rows), Parabolic(1, dims, 0.0).generate(rows), RandomSteps(4, dims, 0.0).generate(rows),
  Sine(1, dims, 0.0).generate(rows), Sqrt(1, dims, 0.0).generate(rows), Star(dims, 0.0).generate(rows), StraightLines(dims, 0.0).generate(rows),
  Z(dims, 0.0).generate(rows), Zinv(dims, 0.0).generate(rows))


def get_dim[T](arr: Array[Array[T]]): (Int, Int) = {
  (arr.length, arr(0).length)
}


def test_dim (arr_l: List[Array[Array[Double]]], size: Int = 0, tru: Int = 0): Boolean = {
  if(arr_l == Nil) size == tru
  else if (get_dim(arr_l.head) == (rows, dims)) test_dim(arr_l.tail, size + 1 , tru + 1)
  else test_dim(arr_l.tail, size +1, tru)
}

test_dim(all_gens)

/**
  * Generated Data is row oriented (row x cols)
  */


// Checking External Tests
// TODO: What if new Tests?

val all_ex_stats = List(CMI(), HICS(), II(), MAC(), MS(), TC(), UDS())
val all_mcde_stats = List(KS(), MWB(), MWP(), MWPi(), MWPr(), MWPs(), MWPu(), MWZ(), S())

def which_row_orient(stats: List[Stats]): List[Boolean] = {
  {for{
    stat <- stats
    data = stat.preprocess(arr)
  } yield get_dim(data.index)}.map(x => x == (rows, dims))
}

which_row_orient(all_ex_stats)
which_row_orient(all_mcde_stats)

/**
  * After preprocess() --> PreprocessedData = all are row oriented
  * However val index is col oriented --> See Index Class
  */

// Also check for all indexstructres --> To be sure that they are all implemented the same

val all_indecies = List(new AdjustedRankIndex(arr), new CorrectedRankIndex(arr), new ExternalRankIndex(arr),
  new NonIndex(arr), new RankIndex(arr))

def which_row_orient_index(ind: List[Index]):List[Boolean] = {
    {for {
      index <- ind
    } yield get_dim(index.index)}.map(x => x == (rows, dims))

}


which_row_orient_index(all_indecies).map(x => !x)

// Apply Method calls the index at dim n (see Index)
val exRank = new ExternalRankIndex(arr)
val noIndex = new NonIndex(arr)

exRank(0).size
noIndex(0).size
noIndex(1).size

// Check for Saved Data


Independent(dims, 0.0).saveSample() // TODO: If you call this without having a "datagenerator" dir it wont work













