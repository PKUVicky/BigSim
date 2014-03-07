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
import cern.jet.random.engine.RandomEngine
import cern.jet.random.Uniform
import cern.jet.random.Exponential
import org.bigraph.bigsim.datamodel.DataModel

object StochasticSimulator {

  var matchDiscard: Set[Match] = Set();

  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }

  def InversionSampling(ruleActivities: Map[Int, Double]): Int = {
    var cumulate: Double = 0
    var cdf: Map[Int, Double] = ruleActivities.map(r => {
      cumulate += r._2
      r._1 -> cumulate
    })
    val re: RandomEngine = RandomEngine.makeDefault
    val un: Uniform = new Uniform(0, cumulate, re)
    val r: Double = un.nextDouble()
    var res: Int = 0
    cdf.foreach(f => {
      if (f._2 >= r)
        return f._1
    })
    ruleActivities.keys.toList(0)
  }
}

class StochasticSimulator(b: Bigraph) {
  var v: Vertex = new Vertex(b, null, null);
  var g: Graph = new Graph(v);
  var states: Queue[Tuple2[Double, Vertex]] = Queue();
  var simQueue: TreeMap[Int, Queue[Match]] = TreeMap();
  var reactNodes: Set[String] = Set();

  var steps: Int = 0;
  var checked: Map[Long, Boolean] = Map();

  def simulate: Unit = {

    /**
     * 0. Initialization:
     * Initialize the simulation state
     * t = 0
     * M(a,R) = match(a,R)
     * αR = |M(a,R)|*Pr
     * α=αR1+αR2+...+αRn
     * If α==0, simulation end
     */
    // add the initial agent to the simQueue
    var curv: Vertex = v
    var curb: Bigraph = v.bigraph
    states += ((0, curv))
    var matchMap: Map[Int, Set[Match]] = Map()
    var systemActivity: Double = 0.0
    var ruleActivities: Map[Int, Double] = Map()
    var time: Double = 0.0

    var ruleIndex: Int = 0
    println("rule size:" + b.rules.size)
    b.rules.map(r => {
      ruleIndex += 1
      var matches: Set[Match] = curb.findMatchesOfRR(r)
      matchMap += ruleIndex -> matches

      println("ruleIndex:" + ruleIndex + " matches:" + matches.size)
      //calculate rule activities
      val ruleActivity = r.rate * matches.size
      ruleActivities += ruleIndex -> ruleActivity

      //calculate system activity
      systemActivity += ruleActivity
    })

    while (systemActivity > 0) {
      /**
       * 1. Monte Carlo step:
       * Sample the following random values
       * R=Rand(Rule set, PPS Sampling of ruleActivity/systemActivity)
       * m=Rand(M(a,R), Uniform distribution of 1/M(a,R))
       */
      // sample rule
      val ru: Int = StochasticSimulator.InversionSampling(ruleActivities.map(r => { r._1 -> r._2 / systemActivity }))
      println("which rule:" + ru + " match size:" + matchMap(ru).size)
      // sample match
      val re: RandomEngine = RandomEngine.makeDefault
      val un: Uniform = new Uniform(0, matchMap(ru).size - 1, re)
      val ma: Match = matchMap(ru).toList(un.nextInt())
      val ex: Exponential = new Exponential(systemActivity, re)
      val tIncr: Double = ex.nextDouble()
      println("time Incr:" + tIncr)

      /**
       * 2. Update:
       * Update the simulation state
       * perform reaction
       * time = time + tIncr
       * update set of matches
       * update rule activities
       * update system activity
       */
      var newb: Bigraph = curb.applyMatch(ma)
      var newv: Vertex = null
      if (newb.root == null)
        newb.root = new Nil();
      time += tIncr
      if (curb != null) {
        newv = new Vertex(newb, curv, ma.rule)
        newv.CLK = time.toInt
        newv.globalClock = time
        //newv.parent = curv
        if (g.lut.contains(newv.hash)) {
          newv = g.lut(newv.hash);
          newv.addParents(curv)
        } else {
          g.add(newv);
        }
        curv.addTarget(newv, ma.rule);
        states += (time -> newv)
        if (GlobalCfg.printMode) {
          printf("%s:%s\n", "N_" + Math.abs(newv.hash), newv.bigraph.root.toString);
        }
      }

      // clean all the containers and update
      curv = newv
      curb = newb
      matchMap.empty
      systemActivity = 0.0
      ruleActivities.empty
      ruleIndex = 0
      b.rules.map(r => {
        ruleIndex += 1
        var matches: Set[Match] = curb.findMatchesOfRR(r)
        matchMap += ruleIndex -> matches
        println("ruleIndex:" + ruleIndex + " matches:" + matches.size)

        //calculate rule activities
        val ruleActivity = r.rate * matches.size
        ruleActivities += ruleIndex -> ruleActivity

        //calculate system activity
        systemActivity += ruleActivity
      })
    }

    report(1)
    StochasticSimulator.matchGC;
  }

  def report(step: Int): String = {
    GlobalCfg.node = false
    if (GlobalCfg.pathOutput != "")
      g.dumpPathes
    GlobalCfg.node = true
    dumpDotForward;
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
    out += "   BigSim_Report [shape = parallelogram color = aliceblue style=filled label=\"BigSim\nReport\"];\n"
    out += "BigSim_Report -> N_" + formatHash(g.root.hash) + "[color = aliceblue label = \"" +
      DataModel.getWeightExpr + "=" +
      DataModel.getReport + "\n" +
      DataModel.getValues(",") + "\"];\n";
    out += " N_" + formatHash(g.root.hash) + "\n" + " [shape=circle, color=lightblue2, style=filled];\n";
    g.lut.values.map(x => {
      var rr: String = "root";
      var dc: String = "";

      if (x.terminal) {
        dc = "shape = doublecircle, color=lightblue2, style=filled, ";
      }
      out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\n" +  "\"];\n";

      x.target.map(y => {
        rr = "?";
        if (y._2 != null)
          rr = y._2.name;

        if (y._1 != null) {
          if (GlobalCfg.checkTime)
            rr = rr + "\nSystem Clock: " + y._1.globalClock
          if (GlobalCfg.checkData && y._2.conds.size != 0)
            rr = rr + "\nCond:" + y._2.getConds
          if (GlobalCfg.checkHMM && y._2.hmms.size != 0)
            rr = rr + "\nHMM:" + y._2.getHMM
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
