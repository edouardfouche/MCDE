import com.edouardfouche.index._
import com.edouardfouche.generators.Linear


// CMI, HICS MAC and UDS use External Rank Index

val data = Linear(2, 0).generate(100)
val ind = new ExternalRankIndex(data)

data(0)
data(1)

ind.index(0).sorted
ind.index(1).sorted // There is no ordering here according to rank

ind.index(0)

val adind = new RankIndex(data)
adind.index(0)
adind.index(1)

val trans = data.transpose
trans(0).sorted // This is how it should be ordered

// NonIndex