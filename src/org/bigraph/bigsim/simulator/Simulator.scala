package org.bigraph.bigsim.simulator

import scala.collection.immutable.TreeMap
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.mutable.Stack
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.BRS.Graph
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.model.ReactionRule
import org.bigraph.bigsim.BRS.Vertex
import org.bigraph.bigsim.data.DataModel
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.model.Bigraph

abstract class Simulator {
  def simulate: Unit;
}

object Simulator {

  var matchDiscard: Set[Match] = Set();

  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }

  def simulate(sn: String, b: Bigraph): Unit = {
    var simulator: Simulator = null;
    sn match {
      case "TimeSlicingSimulator" => {
        simulator = new TimeSlicingSimulator(b)
      }
      case "EnumSimulator" => {
        simulator = new EnumSimulator(b)
      }
      case "DiscreteEventSimulator" => {
        simulator = new DiscreteEventSimulator(b)
      }
      case "StochasticSimulator" => {
        simulator = new StochasticSimulator(b)
      }
      case _ => {
        println("Error with Simulator: Class " + sn + " not found.")
        return
      }
    }
    simulator.simulate
  }

}
