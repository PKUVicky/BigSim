package org.bigraph.bigsim.simulator

/**
 * @author liangwei
 * version 0.1
 */

import scala.collection.mutable.Queue
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.immutable.TreeMap
import scala.util.Random
import java.io._
import org.bigraph.bigsim._
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.model._
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.BRS.Graph
import org.bigraph.bigsim.BRS.Vertex
import org.bigraph.bigsim.data.Data

object TimeSlicingSimulator {

  var matchDiscard: Set[Match] = Set();

  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }
}

class TimeSlicingSimulator(b: Bigraph) extends Simulator {
  var v: Vertex = new Vertex(b, null, null);
  var g: Graph = new Graph(v);
  var states: Queue[Tuple2[Int, Vertex]] = Queue();
  var simQueue: TreeMap[Int, Queue[Match]] = TreeMap();
  var reactNodes: Set[String] = Set();

  var steps: Int = 0;
  var checked: Map[Long, Boolean] = Map();

  def simulate: Unit = {
    // add the initial agent to the simQueue
    states += ((0, v))

    if (b == null || b.root == null) {
      println("time slicing simulator::simulate(): null");
      return ;
    } else {
      // keep simulate until the end
      while (step()) {
        //println("workStack size : " + workStack.size +" : agent : "+workStack.top.bigraph.root.toString)
      };
      report(1)
      TimeSlicingSimulator.matchGC;
    }
  }

  def report(step: Int): String = {
    GlobalCfg.node = false
    if (GlobalCfg.pathOutput != "")
      g.dumpPaths
    GlobalCfg.node = true
    g.dumpDotForward;
  }

  def step(): Boolean = {

    /**
     * 0: update
     * If sim queue contains reactions happen at this time,
     * try match and apply match.
     */
    update()

    /**
     * 1: add match
     */
    if (!addMatch()) {
      return false
    }

    /**
     * 2: update if current match doesn't need reaction time
     * If sim queue contains reactions happen at this time,
     * try match and apply match.
     */
    update()

    TimeSlicingSimulator.matchGC;
    // update the system clk
    GlobalCfg.SysClk = GlobalCfg.SysClk + GlobalCfg.SysClkIncr
    Data.update("SysClk", GlobalCfg.SysClk.toString)
    true;
  }

  /**
   * update
   * update matches once the system clock meets
   */
  def update() {

    //println("System Clock now:" + GlobalCfg.SysClk)
    if (simQueue.contains(GlobalCfg.SysClk)
      && !simQueue.get(GlobalCfg.SysClk).isEmpty) {
      // apply these matches
      var reactList: Queue[Match] = simQueue.getOrElse(GlobalCfg.SysClk, Queue())
      // match and find match and apply match!!!
      var v: Vertex = states.last._2
      var curBigraph = v.bigraph
      var curRRs: Set[ReactionRule] = Set()

      reactList.map(tm => {
        var matches: Set[Match] = curBigraph.findMatchesOfRR(tm.rule)
        if (matches != null) {
          var matched: Boolean = false
          matches.map(m => {
            if (!matched && m.getReactNodes.equals(tm.getReactNodes)) {
              if (!GlobalCfg.checkData || m.rule.check) {
                var nb: Bigraph = curBigraph.applyMatch(m)

                /**
                 * update a reaction rule data model
                 */
                m.rule.update
                /**
                 * update agent data with clock
                 */
                Data.updateDataCalcsWithClk(m.rule.pSysClikIncr.toString)

                curRRs += m.rule
                // update the reactNodes
                //reactNodes -- m.reactNodes
                reactNodes = reactNodes.filter(m.reactNodes.contains(_))

                matched = true
                if (nb.root == null) {
                  nb.root = new Nil();
                }
                //println("System Clock:" + GlobalCfg.SysClk)
                //println("middle result match RR " + tm.rule.name + " : " + nb.root.toString)
                if (GlobalCfg.printMode) {
                  printf("%s:%s\n", "N_" + Math.abs(nb.hashCode()), nb.root.toString);
                  println(v.variables)
                }
                curBigraph = nb
              }
            }
          })
        }
      })

      if (curBigraph != null && curRRs != null) {
        var nv = new Vertex(curBigraph, v, curRRs, true)
        nv.CLK = GlobalCfg.SysClk
        v.addTargets(curRRs, nv);
        states += (GlobalCfg.SysClk -> nv)
        if (GlobalCfg.printMode) {
          //printf("%s:%s\n", "N_" + Math.abs(nv.hash), nv.bigraph.root.toString);
          //println(v.variables)
        }
      }

      // finally, delete it!
      simQueue.-(GlobalCfg.SysClk)
    }

  }

  def addMatch(): Boolean = {
    var v: Vertex = states.last._2
    steps += 1;
    var b: Bigraph = v.bigraph;
    var matches: Set[Match] = b.findMatches;

    if (steps >= GlobalCfg.maxSteps) {
      println("sim::step Interrupted!  Reached maximum steps: " + GlobalCfg.maxSteps);
      report(steps);
      return false;
    }

    /**
     * if current model can not find matches and the sim queue is empty,
     * then the simulation is over.
     */
    if (matches.isEmpty && simQueue.isEmpty) {
      println("sim::step Complete!");
      report(steps);
      TimeSlicingSimulator.matchGC;
      return false;
    }

    /**
     * if there is no match, but the sim queue is not emputy
     *
     * if (matches.isEmpty && !simQueue.isEmpty) {
     * println("Current Agent Can not match rules, jump to time:" + simQueue.firstKey)
     * GlobalCfg.SysClk = simQueue.firstKey
     * return true
     * }
     */

    println("matches size: " + matches.size)

    /**
     * If a reaction rule is not random and not conflict,
     * it must happen when it is matched.
     */
    matches.map(m => {
      println("reactNodes:" + reactNodes)
      val conflict = m.conflict(reactNodes.toList)
      if (!conflict && !m.rule.random) {
        var key = GlobalCfg.SysClk + m.rule.sysClkIncr
        var queue: Queue[Match] = null
        if (simQueue.contains(key)) {
          queue = simQueue(key)
          queue += m
        } else {
          queue = Queue(m)
        }
        simQueue += key -> queue
        reactNodes ++= m.reactNodes
        println("match: " + m.rule.name + "\treact nodes:" + m.reactNodes)
        //matches -= m
      } else if (conflict) {
        //matches -= m
      }
    })
    return true;
  }

  def formatHash(hash: Int): String = {
    if (hash < 0) "_" + hash.abs;
    else hash.toString;
  }

  def dumpDotForward: String = {
    //if (GlobalCfg.graphOutput == "") return "";
    var out: String = "";
    out += "digraph reaction_graph {\n";
    out += "   rankdir=LR;\n";
    out += "   Node [shape = circle];\n";
    out += " N_" + formatHash(g.root.hash) + "\n"; // + " [shape=doublecircle, color=lightblue2, style=filled, label=\"" +
    //root.bigraph.root.toString + "\"];\n";
    g.lut.values.map(x => {
      var rr: String = "root";
      var dc: String = "";

      if (x.terminal) {
        dc = "shape = doublecircle, color=darkolivegreen3, style=filled, ";
      }
      //out += "N_" + formatHash(x.hash) + "\n";//+ "[ " + dc + "label=\"" + x.bigraph.root.toString + "\"];\n";
      x.target.map(y => {
        rr = "?";
        if (y._2 != null)
          rr = y._2.name;

        if (y._1 != null) {
          if (GlobalCfg.checkTime)
            rr = rr + "\n" + y._1.CLK
          if (GlobalCfg.checkData)
            rr = rr + "\n" + y._2.getExps + "\n" + y._2.getConds
          out += " N_" + formatHash(x.hash) + " -> N_" + formatHash(y._1.hash) + "[ label = \"" + rr + "\"];\n"
        }
      });

    });
    out += "}\n";
    if (GlobalCfg.graphOutput != "") {
      var file: File = new File(GlobalCfg.graphOutput);
      var writer: Writer = new FileWriter(file);
      writer.write(out);
      writer.flush;
    }
    out;
  }
}
