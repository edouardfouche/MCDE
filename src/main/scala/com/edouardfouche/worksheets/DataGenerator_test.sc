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

import com.edouardfouche.generators._

for {noise <- Array(0.0, 0.1, 0.2, 0.5, 1.0)} {
  for {dim <- Array(2, 3, 5)} {
    Independent(dim, noise).saveSample()
    Cross(dim, noise).saveSample()
    DoubleLinear(0.25, dim, noise).saveSample()
    Hourglass(dim, noise).saveSample()
    Hypercube(dim, noise).saveSample()
    HypercubeGraph(dim, noise).saveSample()
    HyperSphere(dim, noise).saveSample()
    Linear(dim, noise).saveSample()
    Parabolic(1, dim, noise).saveSample()
    Sine(1, dim, noise).saveSample()
    Sine(5, dim, noise).saveSample()
    Star(dim, noise).saveSample()
    Zinv(dim, noise).saveSample()
  }
}

Independent(100, 0.0).saveSample()