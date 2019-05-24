import io.github.edouardfouche.generators.Independent
import io.github.edouardfouche.mcde.MWP

val data = MWP().preprocess(Independent(3, 0.0, "gaussian", 0).generate(1000))

data.values.length

data.values(0).length

data.numCols

val test = MWP(50, parallelize = 0)
val result = test.contrastMatrix(data)

result