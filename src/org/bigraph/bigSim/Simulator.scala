package org.bigraph.bigSim

import scala.collection.immutable.TreeMap
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.mutable.Stack

object Simulator {
  var matchDiscard: Set[Match] = Set();
  
  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }
}

class Simulator(b: Bigraph) {

  var v: Vertex = new Vertex(b, null, null);
  var g: Graph = new Graph(v);
  var workStack: Stack[Vertex] = Stack();
  workStack.push(v);

  var reactNodes: Set[String] = Set();

  var steps: Int = 0;
  var checked: Map[Long, Boolean] = Map();

  def simulate: Unit = {
    if (b == null || b.root == null) {
      println("mc::check(): null");
      return ;
    } else {
      // 一直检查直到遇到反例
      while (step()) {
        //println("workStack size : " + workStack.size +" : agent : "+workStack.top.bigraph.root.toString)
      };
      report(1)
      Simulator.matchGC;
    }
  }

  def report(step: Int): String = {
    GlobalCfg.instance = false
    if (GlobalCfg.pathOutput != "")
      g.dumpPathes
    GlobalCfg.instance = true
    g.dumpDotForward;
  }

  def step(): Boolean = {
    // 是否达到最大检测步数
    if (steps >= GlobalCfg.maxSteps) {
      println("mc::step Interrupted!  Reached maximum steps: " + GlobalCfg.maxSteps);
      report(steps);
      return false;
    }
    // 待检测模型队列为空
    if (workStack.size == 0) {
      println("sim::step Complete!");
      report(steps);
      Simulator.matchGC;
      return false;
    }

    // 取出栈顶模型
    var v: Vertex = workStack.top;
    //if (checked.contains(v.hash)) { //若模型已检查过则跳过
    //return false;
    //}

    steps += 1;
    var step: Int = steps;
    var b: Bigraph = v.bigraph;

    // 模型使用规则进行匹配
    var matches: Set[Match] = b.findMatches;
    println("matches size: " + matches.size)
    println("here---")
    matches.foreach(f => { f.reactNodes.foreach(println)})
     println("---here")
    checked(v.hash) = true;
    if (matches.size == 0) { //没有可以匹配的规则，该节点为终结节点
      v.terminal = true;
      return false;
    }

    var simRRMap: TreeMap[String, Match] = TreeMap();
    var isFirst = true

    while (matches.size > 0) {
      // delete conflict react node
      matches.map(it => {
        var conflict = false
        it.reactNodes.map(rn => {
          if (reactNodes.contains(rn)) {
            println(rn)
            conflict = true
          }
        })
        if (conflict) {
          matches -= it
        } else if (!it.reactionRule.random) {
          var key = (GlobalCfg.SysClk + it.reactionRule.sysClkIncr).toString + "_" + scala.util.Random.nextInt(100000)
          while (simRRMap.contains(key)) {
            key = (GlobalCfg.SysClk + it.reactionRule.sysClkIncr).toString + "_" + scala.util.Random.nextInt(100000)
          }
          simRRMap += key -> it
          reactNodes ++ it.reactNodes
          println("not random match but must act: " + it.reactionRule.name)
          matches -= it
        }
      });

      // find one rule meets the condition
      if (matches.size > 0) {
        var randIndex = scala.util.Random.nextInt(matches.size)
        var curMatch = matches.toList(randIndex)

        var rr: ReactionRule = curMatch.rule;
        var key = (GlobalCfg.SysClk + rr.sysClkIncr).toString + "_" + scala.util.Random.nextInt(100000)
        while (simRRMap.contains(key)) {
          key = (GlobalCfg.SysClk + rr.sysClkIncr).toString + "_" + scala.util.Random.nextInt(100000)
        }

        print("curMatch react nodes :")
        curMatch.reactNodes.foreach(print)
        println

        if (isFirst || !curMatch.reactionRule.random) {
          simRRMap += key -> curMatch
          reactNodes ++ curMatch.reactNodes
          println("random match: " + curMatch.reactionRule.name)
        } else if (scala.util.Random.nextInt(2) == 1) {
          simRRMap += key -> curMatch
          reactNodes ++ curMatch.reactNodes
          println("random match: " + curMatch.reactionRule.name)
        }
        matches -= curMatch
      }
      isFirst = false
    }

    var curBigraph = v.bigraph
    var curRR: ReactionRule = null
    simRRMap.foreach(tm => {
      if (!GlobalCfg.checkData || tm._2.reactionRule.check) {
        var nb: Bigraph = curBigraph.applyMatch(tm._2);
        tm._2.reactionRule.update
        if (nb.root == null)
          nb.root = new Nil();
        //var nv: Vertex = new Vertex(nb, v, tm._2.reactionRule);
        //nv.CLK = tm._1.split("_")(0).toDouble
        GlobalCfg.SysClk = tm._1.split("_")(0).toInt
        //println("System Clock:" + GlobalCfg.SysClk)
       // println("middle result match RR " + tm._2.reactionRule.name + " : " + nb.root.toString)
        curBigraph = nb
        curRR = tm._2.reactionRule
      }
    })

    if (curBigraph != null && curRR != null) {
      var nv = new Vertex(curBigraph, v, curRR)
      nv.CLK = GlobalCfg.SysClk
      if (g.lut.contains(nv.hash)) {
        nv = g.lut(nv.hash);
        nv.addParents(v)
      } else {
        // 若新模型尚未检测过，加入待检测队列。
        //workQueue.enqueue(v2);
        g.add(nv);
      }
      v.addTarget(nv, curRR);
      workStack.push(nv)
      if (GlobalCfg.printMode) {
        printf("%s:%s\n", "N_" + Math.abs(nv.hash), nv.bigraph.root.toString);
      }
    }
    matches.clear();
    Simulator.matchGC;
    true;
  }
}
