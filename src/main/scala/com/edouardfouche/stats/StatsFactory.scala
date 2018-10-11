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
package com.edouardfouche.stats

import com.edouardfouche.stats.mcde._
import com.edouardfouche.stats.external._

object StatsFactory {
  def getTest(test: String, m: Int, alpha: Double, calibrate: Boolean, parallelize: Int): Stats =
  test.toLowerCase match {
    case "ks" => KS(m, alpha, calibrate, parallelize) // preferred
    case "mwb" => MWB(m, alpha, calibrate, parallelize) // preferred
    case "mwz" => MWZ(m, alpha, calibrate, parallelize)
    case "mwp" => MWP(m, alpha, calibrate, parallelize) // preferred
    case "ii" => II()
    case "hics" => HICS()
    case "tc" => TC()
    case "ms" => MS()
    case "uds" => UDS()
    case "mac" => MAC()
    case "cmi" => CMI()
  case _ => throw new Error(s"Unknown statistical test ${test}")
  }
}
