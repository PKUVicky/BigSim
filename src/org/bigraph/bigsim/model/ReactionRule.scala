package org.bigraph.bigsim.model

import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.data.DataSpliter
import org.bigraph.bigsim.parser.BooleanExprParser
import org.bigraph.bigsim.parser.HMM
import org.bigraph.bigsim.parser.TermParser
import org.bigraph.bigsim.utils.GlobalCfg
import scala.collection.mutable.Set
import cern.jet.random.Poisson
import cern.jet.random.engine.RandomEngine
import org.bigraph.bigsim.BRS.Match
import scala.collection.mutable.Queue

/**
 * @author zhaoxin
 * version 0.1
 *
 * @author liangwei
 * version 0.2
 */

class ReactionRule(n: String, red: Term, react: Term, exp: String) {
  /** Basic element of RR */
  var name: String = n
  var redex: Term = red
  var reactum: Term = react

  /** If a reaction rule contains data model, calculate it and split it */
  DataSpliter.preOrderData(redex, "root", this)
  DataSpliter.preOrderData(reactum, "root", this)

  /** redexCtrl <--> reactumCtrl */
  var nodeMap: Map[Node, Node] = calcNodeMap

  var data: Set[Prefix] = Set()
  var causation: Set[ReactionRule] = null

  /** If this is a stochastic bigraph, a rate constant will be assigned. */
  var rate: Double = 0
  var dataCalcs: Queue[Tuple2[String, String]] = Queue()
  var conds: Set[String] = Set()
  var random: Boolean = false
  var sysClkIncr: Int = 0
  var hmms: Set[String] = Set()
  var reverse: Boolean = false
  var reactNodes: Set[String] = Set()
  initExprs

  /** Possion Distribution */
  var poTimeSpend: Double = getContiRRIncr

  /** init data calculate exprs and conds in a RR */
  def initExprs {
    exp.split("\t").foreach(f => {
      if (f.startsWith(GlobalCfg.sysClkPrefStr)) {
        sysClkIncr = f.substring(GlobalCfg.sysClkPrefStr.length).trim.toInt
      } else if (f.startsWith(GlobalCfg.condPrefStr)) {
        f.substring(GlobalCfg.condPrefStr.length).split(",").foreach(c => conds += c)
      } else if (f.startsWith(GlobalCfg.exprPrefStr)) {
        f.substring(GlobalCfg.exprPrefStr.length).split(",").foreach(e => {
          val kv = e.split("=")
          if (kv.length == 2)
            dataCalcs.enqueue(new Tuple2(kv(0), kv(1)))
        })
      } else if (f.startsWith(GlobalCfg.randomPrefStr)) {
        random = f.substring(GlobalCfg.randomPrefStr.length).toBoolean
      } else if (f.startsWith(GlobalCfg.hmmPrefStr)) {
        f.substring(GlobalCfg.hmmPrefStr.length).split(",").foreach(h => {
          hmms += h
        })
      } else if (f.startsWith(GlobalCfg.ratePrefStr)) {
        rate = f.substring(GlobalCfg.ratePrefStr.length).toDouble
      } else if (f.startsWith(GlobalCfg.reversePrefStr)) {
        reverse = f.substring(GlobalCfg.reversePrefStr.length).toBoolean
      } else if (f.startsWith(GlobalCfg.reactPrefStr)) {
        f.substring(GlobalCfg.reactPrefStr.length).split(",").foreach(reactNodes.add(_))
      }
    })
  }

  /**
   * Get the time by Possion distribution if the reaction rule is random
   */
  def getRRIncr: Int = {
    if (random) {
      val re: RandomEngine = RandomEngine.makeDefault
      val po: Poisson = new Poisson(sysClkIncr, re)
      val incr = po.nextInt()
      if (sysClkIncr != 0 && incr < GlobalCfg.SysClkIncr.toInt) {
        GlobalCfg.SysClkIncr.toInt
      } else {
        incr / GlobalCfg.SysClkIncr.toInt * GlobalCfg.SysClkIncr.toInt
      }
    } else
      sysClkIncr
  }

  /**
   * Get the time by Possion distribution if the reaction rule is random
   */
  def getContiRRIncr: Double = {
    if (random) {
      val re: RandomEngine = RandomEngine.makeDefault
      val po: Poisson = new Poisson(sysClkIncr, re)
      poTimeSpend = po.nextDouble()
      poTimeSpend
    } else
      sysClkIncr
  }

  /**
   * Check if there is any condition not meet
   * Also Check HMM
   * @return true/false
   */
  def check: Boolean = {
    if (GlobalCfg.checkData)
      conds.foreach(m => {
        val q = BooleanExprParser.parse(m)
        if (!q.check())
          return false
      })

    if (GlobalCfg.checkHMM)
      hmms.foreach(h => {
        if (HMM.hmms(h).getObsProbability < GlobalCfg.minProbability)
          return false
      })
    true
  }

  /**
   * Check if there is any condition not meet
   *
   * Control.variable can be mapped to node.variable based on
   * reactNodes and match.
   *
   * Also Check HMM
   * @return true/false
   */
  def check(m: Match): Boolean = {

    if (GlobalCfg.checkData) {
      conds.foreach(c => {
        var cond = c
        reactNodes.foreach(rn => {
          if (cond.contains(rn)) {
            var rns = m.reactNodesMap.getOrElse(rn, Set())
            if (rns.size > 0) {
              cond = cond.replaceAll(rn, rns.head)
            }
          }
        })

        val q = BooleanExprParser.parse(cond)
        if (GlobalCfg.DEBUG)
          println("check:" + cond + "\tresult:" + q.check)
        if (!q.check()) {
          return false
        }
      })
    }

    if (GlobalCfg.checkHMM)
      hmms.foreach(h => {
        if (HMM.hmms(h).getObsProbability < GlobalCfg.minProbability)
          return false
      })
    true
  }

  /**
   * Update variables once a RR takes place
   */
  def update {
    Data.data("SysClk").value = GlobalCfg.SysClk.toString
    dataCalcs.foreach(f => {
      Data.update(f._1, f._2)
    });
  }

  /**
   * Update variables once a RR takes place
   */
  def update(m: Match) {
    Data.data("SysClk").value = GlobalCfg.SysClk.toString
    dataCalcs.foreach(dc => {

      var leftTerm = dc._1
      var rightTerm = dc._2

      reactNodes.foreach(rn => {
        var rns = m.reactNodesMap.getOrElse(rn, Set())
        if (leftTerm.contains(rn)) {
          if (rns.size > 0) {
            leftTerm = leftTerm.replaceAll(rn, rns.head)
          }
        }
        if (rightTerm.contains(rn)) {
          if (rns.size > 0) {
            rightTerm = rightTerm.replaceAll(rn, rns.head)
          }
        }
      })

      Data.update(leftTerm, rightTerm)
    });
  }

  /**
   * get expressions
   */
  def getExps: String = {
    var exps: String = ""
    dataCalcs.map(f => {
      if (exps.equals(""))
        exps = f._1 + "=" + Data.getValue(f._1)
      else
        exps += "," + f._1 + "=" + Data.getValue(f._1)
    })
    exps
  }

  /**
   * get conditions
   */
  def getConds: String = {
    var exps: String = ""
    conds.map(f => {
      if (exps.equals(""))
        exps = f
      else
        exps += "," + f
    })
    exps
  }

  /**
   * Get HMM
   */
  def getHMM: String = {
    hmms.map(h => { h + "=" + (HMM.hmms(h).getObsProbability * 100).formatted("%.2f") + "%" }).toList.mkString(",")
  }

  /**
   * for data flow
   */
  //暂时先考虑，规则只有一个def的情况。cuse以及puse虽然用set，但是暂时考虑单个情况
  var cuses: Set[Term] = Set()
  var puses: Set[Term] = Set()
  var defTerm: Set[Term] = Set()
  var cuseRules: Set[String] = Set()
  var puseRules: Set[String] = Set()
  var defRules: Set[String] = Set() //其它有相同def的规则

  def this(red: Term, react: Term) = this(null, red, react, "")
  def this(n: String, red: Term, react: Term) = this(n, red, react, "")

  override def toString = {
    if (redex != null && reactum != null) {
      name + ":" + redex.toString + " -> " + reactum.toString;
    } else {
      if (redex != null)
        name + ":" + redex.toString() + " -> NULL";
      else if (reactum != null)
        name + ":" + "NULL -> " + reactum.toString();
      else
        "<error printing rule>";
    }
  }

  def calcNodeMap: Map[Node, Node] = {
    var res: Map[Node, Node] = Map();
    var redexNodes = getTermNodes(redex);
    var reactumNodes = getTermNodes(reactum);

    // redex、reactum中Node/Control数量不一致修正
    /**
     * Match both the node or control both in redex and reactum
     */
    var redexNodesName = redexNodes.map((_.name));
    var reactumNodesName = reactumNodes.map(_.name);

    /**
     * If the reactum creates a new node, than it maps to itself.
     * Note that the link for a node may change during the reaction.
     * If not change link, than it is a full match, we take it as first place.
     * If change link, than just do as the node name and ctrl name.
     */
    reactumNodes.map(rn => {
      var fullMatch: Boolean = false
      var mNode: Node = null
      var hasMatch: Boolean = false
      var maxMatch: Int = 0
      for (i <- 0 to redexNodes.length - 1 if !fullMatch) {
        if (redexNodes(i).getNodeStr.equals(rn.getNodeStr)) {
          /**
           * If it is the first time match, record it and the maxMatch name list.
           * If it is not the first time match, compare the maxMatch to find more
           * suitable match.
           */
          if (!hasMatch) {
            hasMatch = true
            mNode = redexNodes(i)
            maxMatch = rn.getMatchPortsCount(redexNodes(i).ports)
          } else if (rn.getMatchPortsCount(redexNodes(i).ports) > maxMatch) {
            mNode = redexNodes(i)
            maxMatch = rn.getMatchPortsCount(redexNodes(i).ports)
          }

        }
        if (redexNodes(i).getNodeStr.equals(rn.getNodeStr)
          && redexNodes(i).getNodePortsStr.equals(rn.getNodePortsStr)) {
          fullMatch = true
          mNode = redexNodes(i)
        }
      }
      if (hasMatch) {
        res += (rn -> mNode)
        redexNodes.filter(_ != mNode)
      } else {
        /**
         *   if the node is a new node, assign the new node with new node name
         */
        //rn.name = rn.ctrl.name.substring(0, 1) + "_" + GlobalCfg.ranNameIndex.toString

        res += (rn -> rn)
      }
    })

    /**
     * If reactum contails the redex node, keep it in the redexNodes.
     * So now, redexNodes is small than reactumNodes.
     *
     * If reactum node not in the redex, add itself
     * If in redex, keep it. So now, redexNodes and reactumNodes are the same.
     */

    /*
    redexNodes = redexNodes.filter(x => reactumNodesName.contains(x.name));
    reactumNodes = reactumNodes.filter(x => {
      var isIn = redexNodesName.contains(x.name);
      if (!isIn) res += (x -> x);
      isIn;
    });

    var index = 0;
    /**
     * The get node function return a sorted list.
     */
    if (redexNodes.size == reactumNodes.size) {
      reactumNodes.map(x => {
        res += (x -> redexNodes(index));
        index += 1;
      });
    } */
    res;
  }

  // 计算Term中所有的Node
  def getTermNodes(t: Term): List[Node] = {
    var nodes: List[Node] = List();
    if (t.isInstanceOf[Prefix]) {
      var pre: Prefix = t.asInstanceOf[Prefix];
      nodes = nodes.:+(pre.node);
      nodes = getTermNodes(pre.suffix) ::: nodes;
    } else if (t.isInstanceOf[Paraller]) {
      var par: Paraller = t.asInstanceOf[Paraller];
      nodes = getTermNodes(par.leftTerm) ::: nodes;
      nodes = getTermNodes(par.rightTerm) ::: nodes;
    } else if (t.isInstanceOf[Regions]) {
      var reg: Regions = t.asInstanceOf[Regions];
      nodes = getTermNodes(reg.leftTerm) ::: nodes;
      nodes = getTermNodes(reg.rightTerm) ::: nodes;
    }
    nodes = nodes.sortWith((n1: Node, n2: Node) => { n1.name < n2.name });
    nodes;
  }

  def causes(rule: ReactionRule) {
    causation += rule
  }

}

object testRR {
  def main(args: Array[String]) {
    var redexS = "Philosopher[left11:edge,right11:edge].(Fork[right12:edge,left11:edge] | Lock) | Fork[right11:edge,left12:edge] | $0";
    var reactumS = "Philosopher[left11:edge,right11:edge].(Fork[right12:edge,left11:edge] | Lock | Fork[right11:edge,left12:edge]) | $0";
    redexS = "ChargingRoom.$0 | Patient[Patient_pill:edge,idle,isDecoction:edge].$2 | Pill[Patient_pill:edge] | $1";
    reactumS = "a:Hospital.(Pill[patient_pill:edge].nil|b:Patient[patient_pill:edge,idle,isDecoction:edge].nil|c:ConsultingRoom.f:Computer[connected:edge].Prescription[patient_prescription:edge].nil|d:ChargingRoom.g:Computer[connected:edge].Bill[patient_bill_payed:edge].nil|e:Pharmacy.h:Computer[connected:edge].nil)";
    redexS = "Pharmacy.(Patient[patient_prescription:edge,patient_bill_payed:edge,isDecoction:edge].IsDecoction[isDecoction:edge,value_is:edge,leftValue:edge].Value[value_is:edge] | Pill[idle] | $0) | Equal[leftValue:edge,rightValue:edge] | False[rightValue:edge] | $1";

    var redex = TermParser.apply(redexS);
    var reactum = TermParser.apply(reactumS);
    println(redex.termType)
    println(reactum.termType)

    redexS = "zone[idle,idle].network[idle,idle,idle,idle].(mobile[idle,idle,idle,idle,hasCAH1:edge,idle].(contextAware[hasCAH1:edge,idle,idle,powerIs1:edge,idle,idle,idle].power[powerIs1:edge] | application[idle,idle,idle,idle]) | mobile[idle,idle,idle,idle,hasCAH2:edge,idle].(contextAware[hasCAH2:edge,idle,idle,powerIs2:edge,idle,idle,idle].power[powerIs2:edge] | application[idle,idle,idle,idle]) | mobile[idle,idle,idle,idle,hasCAH3:edge,idle].(contextAware[hasCAH3:edge,idle,idle,powerIs3:edge,idle,idle,idle].power[powerIs3:edge] | application[idle,idle,idle,idle])) | $0";
    reactumS = "zone[idle,idle].network[idle,idle,idle,idle].(mobile[idle,idle,idle,idle,hasCAH1:edge,idle].(contextAware[hasCAH1:edge,idle,idle,powerIs1:edge,pointIs1:edge,lumIs1:edge,pressureIs1:edge].(power[powerIs1:edge] | pointXY[pointIs1:edge] | luminous[lumIs1:edge] | pressure[pressureIs1:edge]) | application[idle,idle,idle,idle].job1:job[idle]) | mobile[idle,idle,idle,idle,hasCAH2:edge,idle].(contextAware[hasCAH2:edge,idle,idle,powerIs2:edge,pointIs2:edge,lumIs2:edge,pressureIs2:edge].(power[powerIs2:edge] | pointXY[pointIs2:edge] | luminous[lumIs2:edge] | pressure[pressureIs2:edge]) | application[idle,idle,idle,idle].job2:job[idle]) | mobile[idle,idle,idle,idle,hasCAH3:edge,idle].(contextAware[hasCAH3:edge,idle,idle,powerIs3:edge,pointIs3:edge,lumIs3:edge,pressureIs3:edge].(power[powerIs3:edge] | pointXY[pointIs3:edge] | luminous[lumIs3:edge] | pressure[pressureIs3:edge]) | application[idle,idle,idle,idle].job3:job[idle])) | $0";
    redex = TermParser.apply(redexS)
    reactum = TermParser.apply(reactumS)
    var r = new ReactionRule("r_28", redex, reactum, "time:5 cond:power +=5%;fee+=3");
    var a = r.nodeMap
    println(a.toString)

  }
}