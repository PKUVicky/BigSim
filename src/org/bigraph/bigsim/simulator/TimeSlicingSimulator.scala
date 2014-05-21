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
  var states: Queue[Tuple2[Double, Vertex]] = Queue();
  var simQueue: TreeMap[Double, Queue[Match]] = TreeMap();
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
      };
      report()
      TimeSlicingSimulator.matchGC;
    }
  }

  def report(): String = {
    GlobalCfg.node = false
    if (GlobalCfg.pathOutput != "")
      g.dumpPaths
    GlobalCfg.node = true
    dumpDotForward;
  }

  def step(): Boolean = {

    /**
     * if meet max system clock, simulation stop.
     */
    if (GlobalCfg.SysClk > GlobalCfg.maxSysClk) {
      println("sim::step Interrupted!  Reached maximum SysClk: " + GlobalCfg.maxSysClk);
      report();
      return false;
    }

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

    if (simQueue.contains(GlobalCfg.SysClk)
      && !simQueue.get(GlobalCfg.SysClk).isEmpty) {

      if (GlobalCfg.DEBUG)
        println("Update-----System Clock now:" + GlobalCfg.SysClk)
      // apply these matches
      var reactList: Queue[Match] = simQueue.getOrElse(GlobalCfg.SysClk, Queue())
      // match and find match and apply match!!!
      var v: Vertex = states.last._2
      var curBigraph = v.bigraph
      var curRRs: Set[ReactionRule] = Set()

      while (reactList.size > 0) {
        var tm = reactList.dequeue
        var matches: Set[Match] = curBigraph.findMatchesOfRR(tm.rule)
        if (matches != null) {
          var matched: Boolean = false
          matches.map(m => {
            if (GlobalCfg.DEBUG) {
              println(m.rule.name + "," + m.getReactNodes + "," +
                tm.getReactNodes + "matched:" + matched)
            }
            if (!matched && m.getReactNodes.equals(tm.getReactNodes)) {
              var nb: Bigraph = curBigraph.applyMatch(m)
              /**
               * update a reaction rule data model
               */
              m.rule.update(tm)
              /**
               * update agent data with clock
               */
              Data.updateDataCalcsWithClk(tm.RRIncr.toString)

              curRRs += m.rule

              if (GlobalCfg.DEBUG) {
                println("-----react nodes before:" + reactNodes)
              }
              reactNodes = reactNodes.filter(!m.reactNodes.contains(_))
              if (GlobalCfg.DEBUG) {
                println("-----reaction nodes rm:" + m.reactNodes)
                println("-----react nodes after:" + reactNodes)
              }
              matched = true
              if (nb.root == null) {
                nb.root = new Nil();
              }
              if (GlobalCfg.DEBUG) {
                println("middle result match RR " + tm.rule.name + " : " + nb.root.toString)
                println("middle result of variables: " + Data.getValues(","))
              }
              curBigraph = nb
            }
          })
        }
      }

      /*   reactList.map(tm => {
        var matches: Set[Match] = curBigraph.findMatchesOfRR(tm.rule)
        if (matches != null) {
          var matched: Boolean = false
          matches.map(m => {
            println(m.rule.name + "," + m.getReactNodes + "," + tm.getReactNodes)
            if (!matched && m.getReactNodes.equals(tm.getReactNodes)) {
              if (!GlobalCfg.checkData || m.rule.check(m)) {
                var nb: Bigraph = curBigraph.applyMatch(m)

                /**
                 * update a reaction rule data model
                 */
                m.rule.update(m)
                /**
                 * update agent data with clock
                 */
                Data.updateDataCalcsWithClk(m.RRIncr.toString)

                curRRs += m.rule
                println("-----react nodes before:" + reactNodes)
                reactNodes = reactNodes.filter(!m.reactNodes.contains(_))
                println("-----reaction nodes rm:" + m.reactNodes)
                println("-----react nodes after:" + reactNodes)

                matched = true
                if (nb.root == null) {
                  nb.root = new Nil();
                }
                println("middle result match RR " + tm.rule.name + " : " + nb.root.toString)
                println("middle result of variables: " + Data.getValues(","))
                curBigraph = nb
              }
            }
          })
        }
      }) */

      if (curBigraph != null && curRRs != null) {
        var nv = new Vertex(curBigraph, v, curRRs, true)
        nv.sysClk = GlobalCfg.SysClk

        if (g.lut.contains(nv.hash)) {
          nv = g.lut(nv.hash);
          nv.addParents(v)
        } else {
          g.add(nv);
        }

        v.addTargets(curRRs, nv);
        states += (GlobalCfg.SysClk -> nv)
        if (GlobalCfg.printMode) {
          print("SysClk:" + GlobalCfg.SysClk + "\t")
          printf("%s:%s\n", "N_" + Math.abs(nv.hash), nv.bigraph.root.toString);
          println(nv.variables)
        }
      }

      // finally, delete it!
      simQueue = simQueue.-(GlobalCfg.SysClk)
    }
  }

  def addMatch(): Boolean = {
    var v: Vertex = states.last._2
    steps += 1;
    var b: Bigraph = v.bigraph;
    var matches: Set[Match] = b.findMatches;

    if (steps >= GlobalCfg.maxSteps) {
      println("sim::step Interrupted!  Reached maximum steps: " + GlobalCfg.maxSteps);
      report();
      return false;
    }

    /**
     * if current model can not find matches and the sim queue is empty,
     * then the simulation is over.
     * Abandon! not right
     */
    /*
    if (matches.isEmpty && simQueue.isEmpty) {
      println("sim::step Complete!");
      report(steps);
      TimeSlicingSimulator.matchGC;
      return false;
    } 
    */

    /**
     * if there is no match, but the sim queue is not emputy
     *
     * if (matches.isEmpty && !simQueue.isEmpty) {
     * println("Current Agent Can not match rules, jump to time:" + simQueue.firstKey)
     * GlobalCfg.SysClk = simQueue.firstKey
     * return true
     * }
     */

    //println("Add match-----matches size: " + matches.size)

    /**
     * If a reaction rule is not random and not conflict,
     * it must happen when it is matched.
     */
    matches.map(m => {
      if (GlobalCfg.DEBUG) {
        println("All match:" + m.rule.name + "\tcond:" +
          m.rule.conds + "\treactNodes:" + m.reactNodes)
      }

      val conflict = m.conflict(reactNodes.toList)
      if (!conflict) {
        //if (!conflict && !m.rule.random) {
        val RRIncr = m.rule.getRRIncr
        var reactTime = GlobalCfg.SysClk + RRIncr
        m.RRIncr = RRIncr
        var queue: Queue[Match] = null
        if (simQueue.contains(reactTime)) {
          queue = simQueue(reactTime)
          queue += m
        } else {
          queue = Queue(m)
        }
        simQueue += reactTime -> queue
        reactNodes ++= m.reactNodes
        if (GlobalCfg.DEBUG) {
          println("add match: " + m.rule.name + "\treact nodes:" +
            m.reactNodes + "\treact time:" + reactTime)
        }
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
    var out: String = "";
    out += "digraph reaction_graph {\n";
    out += "   rankdir=LR;\n";
    out += "   Node [shape = circle];\n";
    out += "   BigSim_Report [shape = parallelogram color = aliceblue style=filled label=\"BigSim\nReport\"];\n"
    out += "BigSim_Report -> N_" + formatHash(g.root.hash) + "[color = aliceblue label = \"" +
      Data.getWeightExpr + "=" +
      Data.getReport + "\n" + //Data.getValues(",") +
      "\"];\n";
    out += " N_" + formatHash(g.root.hash) + "\n" + " [shape=circle, color=lightblue2, style=filled];\n";

    g.lut.values.map(x => {
      var rr: String = "root";
      var dc: String = "";

      if (x.terminal) {
        dc = "shape = doublecircle, color=lightblue2, style=filled, ";
      }
      out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\"];\n";
      x.targets.map(y => {
        rr = "?"
        if (y._2 != null) {
          rr = y._2.map(_.name).mkString(",")
        }
        if (y._1 != null) {
          if (GlobalCfg.checkData) {
            rr = rr + "\n" + //y._1.variables + "\n" +
              y._2.map(_.getConds).mkString(",")
          }
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
