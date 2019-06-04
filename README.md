# Monte Carlo Dependency Estimation (MCDE)

[![Build Status](https://travis-ci.com/edouardfouche/MCDE.svg?branch=master)](https://travis-ci.com/edouardfouche/MCDE)
[![License AGPL-3.0](https://img.shields.io/badge/License-AGPL--3-brightgreen.svg)](https://github.com/edouardfouche/MCDE/blob/master/LICENSE.md)

This repository contains the most recent implementation of MCDE/MWP, originally described in the paper:

-  Edouard Fouché & Klemens Böhm. 2019. Monte Carlo Dependency Estimation. In 31st International Conference on Scientific and Statistical Database Management (SSDBM ’19), July 23–25, 2019, Santa Cruz, CA, USA. ACM, New York, NY, USA, 12 pages. https://doi.org/10.1145/3335783.3335795

To reproduce the experiment from the paper, please visit this [repository](https://github.com/edouardfouche/MCDE-experiments).
Our goal in this repository is to provide a deployable and consolidated version of the work described therein.

This repository is released under the AGPLv3 license. Please see the [LICENSE.md](LICENSE.md) file. 

## Quick Start

You can either import MCDE in your own scala project or use our external API, from the command line.

### Importing MCDE

MCDE is available on the central repository (scala 2.11 and 2.12).

You can add it as a dependency in your own project, for example, via sbt:

```
libraryDependencies += "io.github.edouardfouche" %% "mcde" % "0.1.0"
```

### Usage

We provide a detailed guide on how to use the MCDE framework in a Scala worksheet at: 
[src/main/scala/io/github/edouardfouche/worksheets/user_guide.sc](src/main/scala/io/github/edouardfouche/worksheets/user_guide.sc)

**Minimalistic Example**
```
import io.github.edouardfouche.mcde.MWP
import io.github.edouardfouche.preprocess.Preprocess

// Open a data set
val data = Preprocess.open("path/to/file.csv", header = 1, separator = ",", excludeIndex = false, dropClass = true)
// Instantiate a MWP instance with default parameters
val mwp = MWP()
// Compute the MWP score between a set of attributes (here the first and the second one)
val score:Double = mwp.contrast(m = data, dimensions = Set(0,1))
// Compute the MWP matrix on this data set
val scoreMatrix:Array[Array[Double]] = mwp.contrastMatrix(m = data)
```

Please refer to the guide for more detailed information.

### Accessing MCDE from the command line 

#### Build it and run it

**Requirements** : ([Oracle JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK 8](http://openjdk.java.net/install/)) and [sbt](https://www.scala-sbt.org/1.0/docs/Setup.html) 

The project is built with sbt (version 1.2.8). You can compile, package or run the project as follows: 

```
sbt compile
sbt package 
sbt "run <arguments>"
```

You can also export a "fat" jar, including all dependencies and scala libraries using [`sbt-assembly`](https://github.com/sbt/sbt-assembly) (0.14.9):

```
sbt assembly
```

This creates a jar in the folder `target/scala-2.12/` named `MCDE-<version>.jar`, which can be run from java (no 
sbt/scala installation required).
Once you have built the jar, you can run it as follows: 

```
java -jar target/scala-2.12/MCDE-0.1.0.jar <arguments>
```

#### External API for MCDE/MWP (via command line)

The application accepts various arguments. The first two (`-t <task>` and `-f <file>`) are mandatory. Arguments are not case sensitive and can be given in any order. 

- `-t <task>`: Task to perform. Possible choices:
    - `EstimateDependency`: Estimates the dependency of a single subspace (with 2 or more dimensions).
    - `EstimateDependencyMatrix`: Estimates the 2-D dependency matrix, i.e., the dependency of each pair in the data. 
- `-f <file>`: the path to the data source (a comma-separated text file  with 1 line header, row oriented) in your system.
- `-a <approach>`: The approach to use for dependency estimation. Possible choices: 
    - `MWP` (Default): Standard MWP approach as described in the paper.
    - `MWPi`: Like MWP but not adjusting for ties (but still adjusting for ranks).
    - `MWPr`: Like MWP but not adjusting and not correcting for ties (see Paper, Algorithm description).
    - `MWPs`: Like MWP but also adjusting for ties in the slicing process.
    - `MWPu`: Like MWP but without border effects.
    - `KSP`: Like MWP but using Kolmogorow-Smirnow-Test for dependency estimation instead of Mann–Whitney P test.
    
- `-p <plevel>`: Level of parallelism to use. Possible choices:
    - `0` (Default, running single core).
    - `1` (The number of threads is set by the program automatically).
    - Any integer >1 (The user specifies the number of threads explicitly).
    - Please note that, in the case of:
        - `EstimateDependency`, the iterations of MCDE will run in parallel.
        - `EstimateDependencyMatrix`, coefficients are estimated in parallel.

- `-m <M>`: the number of Monte Carlo simulations. More simulations lead to more accurate estimates (see Theorem 4), but also increases runtime linearly. Default: 50

Additional argument for `EstimateDependency`:

- `-d <dimensions>`: Dimensions of the subspace on which the dependency should be estimated (a list of integers, comma-separated, starting from 0). 
For instance, if `0,1` is selected only the first two columns of the data are taken into account, if `0,2` is selected only column 1 and 3.
If not specified, the dependency is estimated on the full space. 

#### Examples

We provide a sample of the data used for the experiments in `src/test/resources/data` for testing.

#### Running MCDE/MWP

- Independent data (2-D); A score around 0.5 is expected.

```bash
fouchee@Modena:~/git/MCDE$ java -jar target/scala-2.12/MCDE-0.1.0.jar -t EstimateDependency -f src/test/resources/data/Independent-2-0.0.csv -a MWP -m 50 -d 0,1
08:05:41.627 [main] INFO  Main$ - Working directory: /home/fouchee/git/MCDE
08:05:41.725 [main] INFO  Main$ - Raw parameters given: ["-t", "EstimateDependency", "-f", "src/test/resources/data/Independent-2-0.0.csv", "-a", "MWP", "-m", "50", "-d", "0,1"]
08:05:41.809 [main] WARN  Main$ - Parallelism level not specified, running on single core.
08:05:41.854 [main] INFO  Main$ - Usage: -t EstimateDependency -f <file> -a <approach> -m <M> -d <dimensions> -p <plevel>
0.4753742163666849
Data Loading time:   76.138745 ms (cpu), 76.460393 ms (wall)
Preprocessing time:      32.325762 ms (cpu), 36.009404 ms (wall)
Computation time:    37.12567 ms (cpu), 37.323588 ms (wall)
```

- Linear dependent data (2-D); A score very close to 1.0 is expected.

```bash
fouchee@Modena:~/git/MCDE$ java -jar target/scala-2.12/MCDE-0.1.0.jar -t EstimateDependency -f src/test/resources/data/Linear-2-0.0.csv -a MWP -m 50 -d 0,1
08:06:10.943 [main] INFO  Main$ - Working directory: /home/fouchee/git/MCDE
08:06:11.033 [main] INFO  Main$ - Raw parameters given: ["-t", "EstimateDependency", "-f", "src/test/resources/data/Linear-2-0.0.csv", "-a", "MWP", "-m", "50", "-d", "0,1"]
08:06:11.112 [main] WARN  Main$ - Parallelism level not specified, running on single core.
08:06:11.153 [main] INFO  Main$ - Usage: -t EstimateDependency -f <file> -a <approach> -m <M> -d <dimensions> -p <plevel>
0.9999998904398533
Data Loading time:   72.585293 ms (cpu), 73.281843 ms (wall)
Preprocessing time:      31.995782 ms (cpu), 32.164805 ms (wall)
Computation time:    35.695044 ms (cpu), 35.812886 ms (wall)
```

####  Computing the 2D-matrix of MCDE/MWP

- Independent data (2-D)

```bash
fouchee@Modena:~/git/MCDE$ java -jar target/scala-2.12/MCDE-0.1.0.jar -t EstimateDependencyMatrix -f src/test/resources/data/Independent-5-0.0.csv -a MWP -m 50 -p 0
08:06:46.343 [main] INFO  Main$ - Working directory: /home/fouchee/git/MCDE
08:06:46.434 [main] INFO  Main$ - Raw parameters given: ["-t", "EstimateDependencyMatrix", "-f", "src/test/resources/data/Independent-5-0.0.csv", "-a", "MWP", "-m", "50", "-p", "0"]
08:06:46.536 [main] WARN  Main$ - Running with parallelism level: 0
08:06:46.580 [main] INFO  Main$ - Usage: -t EstimateDependencyMatrix -f <file> -a <approach> -m <M> -p <plevel>
0.00    0.53    0.41    0.44    0.43
0.53    0.00    0.55    0.38    0.57
0.41    0.55    0.00    0.43    0.57
0.44    0.38    0.43    0.00    0.58
0.43    0.57    0.57    0.58    0.00
Data Loading time:   92.681802 ms (cpu), 92.88746 ms (wall)
Preprocessing time:      39.954535 ms (cpu), 39.999726 ms (wall)
Computation time:    85.453125 ms (cpu), 86.205151 ms (wall)
```

For larger tables (e.g., with 100 dimensions), parallelism provides a significant runtime improvement:

- Independent data (100-D, no parallelism)

```bash
fouchee@Modena:~/git/MCDE$ java -jar target/scala-2.12/MCDE-0.1.0.jar -t EstimateDependencyMatrix -f src/test/resources/data/Independent-100-0.0.csv -a MWP -m 50 -p 0
08:07:14.583 [main] INFO  Main$ - Working directory: /home/fouchee/git/MCDE
08:07:14.670 [main] INFO  Main$ - Raw parameters given: ["-t", "EstimateDependencyMatrix", "-f", "src/test/resources/data/Independent-100-0.0.csv", "-a", "MWP", "-m", "50", "-p", "0"]
08:07:14.961 [main] WARN  Main$ - Running with parallelism level: 0
08:07:15.063 [main] INFO  Main$ - Usage: -t EstimateDependencyMatrix -f <file> -a <approach> -m <M> -p <plevel>
0.00    0.50    0.44    0.48    0.43    0.51    0.57    0.59    0.58    0.53     ... (truncated)
0.50    0.00    0.50    0.55    0.52    0.59    0.59    0.55    0.55    0.50     ... (truncated)
0.44    0.50    0.00    0.45    0.44    0.54    0.49    0.44    0.46    0.66     ... (truncated)
0.48    0.55    0.45    0.00    0.51    0.43    0.54    0.47    0.55    0.35     ... (truncated)
0.43    0.52    0.44    0.51    0.00    0.51    0.47    0.44    0.53    0.55     ... (truncated)
0.51    0.59    0.54    0.43    0.51    0.00    0.60    0.56    0.46    0.56     ... (truncated)
0.57    0.59    0.49    0.54    0.47    0.60    0.00    0.57    0.40    0.43     ... (truncated)
0.59    0.55    0.44    0.47    0.44    0.56    0.57    0.00    0.45    0.60     ... (truncated)
0.58    0.55    0.46    0.55    0.53    0.46    0.40    0.45    0.00    0.53     ... (truncated)
0.53    0.50    0.66    0.35    0.55    0.56    0.43    0.60    0.53    0.00     ... (truncated)
... ... ... ... ... ... ... ... ... ...
(truncated)
Data Loading time:   269.652442 ms (cpu), 280.788656 ms (wall)
Preprocessing time:      96.618713 ms (cpu), 96.945092 ms (wall)
Computation time:    2596.539562 ms (cpu), 2643.743548 ms (wall)
```

- Independent data (100-D, with parallelism)

```bash
fouchee@Modena:~/git/MCDE$ java -jar target/scala-2.12/MCDE-0.1.0.jar -t EstimateDependencyMatrix -f src/test/resources/data/Independent-100-0.0.csv -a MWP -m 50 -p 1
08:07:34.170 [main] INFO  Main$ - Working directory: /home/fouchee/git/MCDE
08:07:34.260 [main] INFO  Main$ - Raw parameters given: ["-t", "EstimateDependencyMatrix", "-f", "src/test/resources/data/Independent-100-0.0.csv", "-a", "MWP", "-m", "50", "-p", "1"]
08:07:34.543 [main] WARN  Main$ - Running with default parallelism level.
08:07:34.674 [main] INFO  Main$ - Usage: -t EstimateDependencyMatrix -f <file> -a <approach> -m <M> -p <plevel>
0.00    0.47    0.48    0.41    0.46    0.42    0.49    0.51    0.57    0.50     ... (truncated)
0.47    0.00    0.62    0.53    0.53    0.51    0.52    0.48    0.52    0.63     ... (truncated)
0.48    0.62    0.00    0.35    0.42    0.57    0.48    0.49    0.48    0.64     ... (truncated)
0.41    0.53    0.35    0.00    0.41    0.45    0.50    0.39    0.57    0.42     ... (truncated)
0.46    0.53    0.42    0.41    0.00    0.53    0.42    0.39    0.51    0.49     ... (truncated)
0.42    0.51    0.57    0.45    0.53    0.00    0.47    0.49    0.46    0.52     ... (truncated)
0.49    0.52    0.48    0.50    0.42    0.47    0.00    0.63    0.45    0.44     ... (truncated)
0.51    0.48    0.49    0.39    0.39    0.49    0.63    0.00    0.55    0.59     ... (truncated)
0.57    0.52    0.48    0.57    0.51    0.46    0.45    0.55    0.00    0.58     ... (truncated)
0.50    0.63    0.64    0.42    0.49    0.52    0.44    0.59    0.58    0.00     ... (truncated)
... ... ... ... ... ... ... ... ... ...
(truncated)
Data Loading time:   263.604825 ms (cpu), 275.675212 ms (wall)
Preprocessing time:      123.729107 ms (cpu), 123.756542 ms (wall)
Computation time:    51.002643 ms (cpu), 1123.108306 ms (wall)
```


## Reproducing the experiments

Please see this [repository](https://github.com/edouardfouche/MCDE-experiments).

## Contributing

We welcome contributions to the repository and bug reports on GitHub.

For questions and comments, please contact `edouard.fouche@kit.edu`