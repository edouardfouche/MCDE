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
package io.github.edouardfouche.generators

object GeneratorFactory {
  val all = Vector(
    Cross.curried,
    DoubleLinear.curried(0.25), DoubleLinear.curried(0.5), DoubleLinear.curried(0.75),
    Parabolic.curried(1), Parabolic.curried(2), Parabolic.curried(3),
    HypercubeGraph.curried, Hourglass.curried, Hypercube.curried,
    Independent.curried,
    Linear.curried,
    LinearPeriodic.curried(2), LinearPeriodic.curried(5), LinearPeriodic.curried(10), LinearPeriodic.curried(20),
    LinearStairs.curried(2), LinearStairs.curried(5), LinearStairs.curried(10), LinearStairs.curried(20),
    LinearSteps.curried(2), LinearSteps.curried(5), LinearSteps.curried(10), LinearSteps.curried(20),
    LinearThenDummy.curried, LinearThenNoise.curried,
    NonCoexistence.curried,
    Cubic.curried(1), Cubic.curried(2), Cubic.curried(3),
    RandomSteps.curried(2), RandomSteps.curried(5), RandomSteps.curried(10), RandomSteps.curried(20),
    Sine.curried(1), Sine.curried(2), Sine.curried(5), Sine.curried(10), Sine.curried(20),
    HyperSphere.curried,
    Sqrt.curried(1), Sqrt.curried(2), Sqrt.curried(3),
    Star.curried,
    StraightLines.curried,
    Z.curried,
    Zinv.curried
  )

  val selected = Vector(
    Cross.curried,
    DoubleLinear.curried(0.25),
    Hourglass.curried,
    Hypercube.curried,
    HypercubeGraph.curried,
    HyperSphere.curried,
    Linear.curried,
    Parabolic.curried(1),
    Sine.curried(1), Sine.curried(5),
    Star.curried,
    Zinv.curried,
    Independent.curried
  )

  val extended_selection = Vector(
    Cross.curried,
    DoubleLinear.curried(0.25),
    Parabolic.curried(1),
    Cubic.curried(1),
    HypercubeGraph.curried, Hourglass.curried, Hypercube.curried,
    Independent.curried,
    Linear.curried,
    Sine.curried(1), Sine.curried(5),
    HyperSphere.curried,
    Star.curried,
    Zinv.curried,
    Z.curried,
    LinearThenDummy.curried,
    LinearThenNoise.curried,
    NonCoexistence.curried,
    RandomSteps.curried(10)
  )

  val independent = Vector(
    Independent.curried
  )

  val sinus = Vector(
    Sine.curried(1), Sine.curried(2), Sine.curried(3), Sine.curried(5), Sine.curried(7),
    Sine.curried(10), Sine.curried(15), Sine.curried(20), Sine.curried(30)
  )

  val linearperiodic = Vector(
    LinearPeriodic.curried(1), LinearPeriodic.curried(2), LinearPeriodic.curried(3),
    LinearPeriodic.curried(5), LinearPeriodic.curried(7), LinearPeriodic.curried(10),
    LinearPeriodic.curried(15), LinearPeriodic.curried(20), LinearPeriodic.curried(30)
  )

  val linear = Vector(
    Linear.curried
  )

  val independentandlinear = Vector(
    Independent.curried,
    Linear.curried
  )

  def saveSampleAll(noise: Double = 0.0): Unit = selected.foreach(x => {
    val generator = x(2)(noise)
    generator.saveSample()
    val generator3D = x(3)(noise)
    generator3D.saveSample()
  })

}