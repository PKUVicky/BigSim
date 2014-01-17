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
import org.bigraph.bigsim._;

object FullSimulator {
  
  var matchDiscard: Set[Match] = Set();

  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }
}

class FullSimulator(b: Bigraph) {
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
      println("mc::check(): null");
      return ;
    } else {
      // keep simulate until the end
      while (step()) {
        //println("workStack size : " + workStack.size +" : agent : "+workStack.top.bigraph.root.toString)
      };
      report(1)
      FullSimulator.matchGC;
    }
  }

  def report(step: Int): String = {
    GlobalCfg.node = false
    if (GlobalCfg.pathOutput != "")
      g.dumpPathes
    GlobalCfg.node = true
    g.dumpDotForward;
  }

  def step(): Boolean = {

    /**
     * If sim queue contains reactions happen at this time,
     * try match and reaction.
     */
    println(GlobalCfg.SysClk)
    if (simQueue.contains(GlobalCfg.SysClk)
      && !simQueue.get(GlobalCfg.SysClk).isEmpty) {
      // match it!!!!!
      var reactList: Queue[Match] = simQueue.getOrElse(GlobalCfg.SysClk, null)
      // match and find match and try match!!!
      var v: Vertex = states.last._2
      var curBigraph = v.bigraph
      var curRRs: Set[ReactionRule] = Set()

      if (reactList != null) {
        reactList.map(tm => {
          if (!GlobalCfg.checkData || tm.rule.check) {
            var matches: Set[Match] = curBigraph.findMatchesOfRR(tm.rule)
            if (matches != null) {
              matches.map(m => {
                //if (m.getReactNodes.equals(tm.getReactNodes)) {
                if (true) {
                  var nb: Bigraph = curBigraph.applyMatch(m)
                  if (nb.root == null)
                    nb.root = new Nil();
                  println("System Clock:" + GlobalCfg.SysClk)
                  println("middle result match RR " + tm.rule.name + " : " + nb.root.toString)
                  curBigraph = nb
                  curRRs += tm.rule
                  // break
                }
              })
            }
          }
        })
      }

      if (curBigraph != null && curRRs != null) {
        var nv = new Vertex(curBigraph, v, curRRs, true)
        nv.CLK = GlobalCfg.SysClk
        v.addTargets(curRRs, nv);
        states += (GlobalCfg.SysClk -> nv)
        if (GlobalCfg.printMode) {
          printf("%s:%s\n", "N_" + Math.abs(nv.hash), nv.bigraph.root.toString);
        }
      }

      // finally, delete it!
      simQueue.-(GlobalCfg.SysClk)
    }

    /**
     *  Get the current state
     */
    var v: Vertex = states.last._2
    steps += 1;
    var step: Int = steps;
    var b: Bigraph = v.bigraph;
    var matches: Set[Match] = b.findMatches;

    if (steps >= GlobalCfg.maxSteps) {
      println("mc::step Interrupted!  Reached maximum steps: " + GlobalCfg.maxSteps);
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
      FullSimulator.matchGC;
      return false;
    }

    /**
     * if there is no match, but the sim queue is not emputy
     */
    if (matches.isEmpty && !simQueue.isEmpty) {
      println("Current Agent Can not match rules, jump to time:" + simQueue.firstKey)
      GlobalCfg.SysClk = simQueue.firstKey
      return true
    }

    println("matches size: " + matches.size)
    // checked(v.hash) = true;

    /**
     * If a match is conflicted with reactNodes,
     * remove it from the matches
     */
    matches.map(it => {
      var conflict = false
      it.reactNodes.map(rn => {
        if (reactNodes.contains(rn)) {
          println(rn)
          conflict = true
        }
      })
      if (conflict)
        matches -= it
    });
    println("no conflict matches size: " + matches.size)

    /**
     * If a reaction rule is not random,
     * it must happen when it is matched.
     */
    matches.map(m => {
      if (!m.rule.random) {
        var key = GlobalCfg.SysClk + m.rule.sysClkIncr
        var queue: Queue[Match] = null
        if (simQueue.contains(key)) {
          queue = simQueue(key)
          queue += m
        } else {
          queue = Queue(m)
        }
        simQueue += key -> queue
        reactNodes ++ m.reactNodes
        println("random match: " + m.rule.name)
      }
      matches -= m
    })

    /**
     * Decide whether at this time, there is a random reaction.
     * 0: not react this time
     * 1: react this time
     * if not react, system clock will move on
     */
    if (Random.nextInt(2) == 0) {
      println("No react at Clock: " + GlobalCfg.SysClk)
      GlobalCfg.SysClk += GlobalCfg.SysClkIncr
      return true
    } else {
      var simRRMap: TreeMap[String, Match] = TreeMap();
      var isFirst = true
      while (!matches.isEmpty) {
        /**
         *  Find one rule meets the condition
         */
        if (!matches.isEmpty) {
          var randIndex = Random.nextInt(matches.size)
          var curMatch = matches.toList(randIndex)

          var rr: ReactionRule = curMatch.rule;
          var key = GlobalCfg.SysClk + rr.sysClkIncr

          if (isFirst) {
            var queue: Queue[Match] = null
            if (simQueue.contains(key)) {
              queue = simQueue(key)
              queue += curMatch
            } else {
              queue = Queue(curMatch)
            }
            simQueue += key -> queue
            reactNodes ++ curMatch.reactNodes
            println("random match: " + curMatch.rule.name)
          } else if (scala.util.Random.nextInt(2) == 1) {
            var queue: Queue[Match] = null
            if (simQueue.contains(key)) {
              queue = simQueue(key)
              queue += curMatch
            } else {
              queue = Queue(curMatch)
            }
            simQueue += key -> queue
            reactNodes ++ curMatch.reactNodes
            println("random match: " + curMatch.rule.name)
          }
          matches -= curMatch
        }
        isFirst = false
      }
      matches.clear();
      FullSimulator.matchGC;
      true;
    }
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
