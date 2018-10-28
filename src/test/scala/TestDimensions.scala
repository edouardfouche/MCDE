import com.edouardfouche.generators._
import com.edouardfouche.index._
import com.edouardfouche.stats.Stats
import org.scalatest.FunSuite
import com.edouardfouche.stats.mcde._
import com.edouardfouche.stats.external._


class TestDimensions extends FunSuite {

  val rows = 50
  val dims = 2
  val arr = Independent(dims, 0.0).generate(rows)

  // TODO: What if new Tests?
  val all_ex_stats = List(CMI(), HICS(), II(), MAC(), MS(), TC(), UDS())
  val all_mcde_stats = List(KS(), MWB(), MWP(), MWPi(), MWPr(), MWPs(), MWPu(), MWZ(), S())

  val all_indecies = List(new AdjustedRankIndex(arr), new CorrectedRankIndex(arr), new ExternalRankIndex(arr),
    new NonIndex(arr), new RankIndex(arr))

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

  def which_row_orient_stats(stats: List[Stats]): List[Boolean] = {
      {for{
        stat <- stats
        data = stat.preprocess(arr)
      } yield get_dim(data.index)}.map(x => x == (rows, dims))
  }

  def which_row_orient_index(ind: List[Index]):List[Boolean] = {
      {for {
        index <- ind
      } yield get_dim(index.index)}.map(x => x == (rows, dims))

  }


  test("Checking if generated data has the format rows x cols"){

    def test_dim (arr_l: List[Array[Array[Double]]], size: Int = 0, tru: Int = 0): Boolean = {
      if(arr_l == Nil) size == tru
      else if (get_dim(arr_l.head) == (rows, dims)) test_dim(arr_l.tail, size + 1 , tru + 1)
      else test_dim(arr_l.tail, size +1, tru)
    }

    assert(test_dim(all_gens))
  }

  test("Checking if val index is col oriented for all Stats"){
    which_row_orient_stats(all_ex_stats).map(x => assert(!x))
    which_row_orient_stats(all_mcde_stats).map(x => assert(!x))
  }

  // To be sure we may be testing twice the same stuff

  test("Checking if val index is col oriented for all Indexstructures"){
    which_row_orient_index(all_indecies).map(x => assert(!x))
  }
}
