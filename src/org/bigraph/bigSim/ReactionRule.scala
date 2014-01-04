package org.bigraph.bigsim

import org.bigraph.bigsim.datamodel.DataModel
import org.bigraph.bigsim.datamodel.DataSpliter

/**
 * @author zhaoxin
 * version 0.1
 *
 * @author liangwei
 * version 0.2
 */

class ReactionRule(n: String, red: Term, react: Term, exp: String) {
  var name: String = n
  var causation: Set[ReactionRule] = null
  var redex: Term = red
  var data: Set[Prefix] = Set()
  // if a reaction rule contains data model, we need to calculate it and split it
  DataSpliter.preOrderData(redex, "root", this)
  var reactum: Term = react
  DataSpliter.preOrderData(reactum, "root", this)
  var dataCalcs: Map[String, String] = Map()
  var conds: Set[String] = Set()
  var sysClkIncr: Int = 0
  var random: Boolean = false
  initExprs

  /**
   * init data calculate exprs and conds in a RR
   */
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
            dataCalcs += kv(0) -> kv(1)
        })
      } else if (f.startsWith(GlobalCfg.randomPrefStr)) {
        if (f.substring(GlobalCfg.randomPrefStr.length).equals("true"))
          random = true
      }
    })
   // if (!dataCalcs.contains("time"))
     // dataCalcs += "time" -> "time+0"
  }

  /**
   * Check if there is any condition not meet
   * @return true/false
   */
  def check: Boolean = {
    conds.foreach(f => {
      val q = QueryParser.parse(f)
      if (!q.check(null))
        return false
    })
    true
  }

  /**
   * Update variables once a RR takes place
   */
  def update {
    dataCalcs.foreach(f => {
      DataModel.update(f._1, f._2)
    });
  }

  /**
   * get reaction time
   */
 /* def getReactionTime: Double = {
    dataCalcs("time").substring(4).trim().toDouble
  }*/

  /**
   * get expressions
   */
  def getExps: String = {
    var exps: String = ""
    dataCalcs.map(f => {
      if (exps.equals(""))
        exps = f._1 + "=" + DataModel.getValue(f._1)
      else
        exps += "," + f._1 + "=" + DataModel.getValue(f._1)
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
   * for data flow
   */
  //暂时先考虑，规则只有一个def的情况。cuse以及puse虽然用set，但是暂时考虑单个情况
  var cuses: scala.collection.mutable.Set[Term] = scala.collection.mutable.Set()
  var puses: scala.collection.mutable.Set[Term] = scala.collection.mutable.Set()
  var defTerm: scala.collection.mutable.Set[Term] = scala.collection.mutable.Set()
  var cuseRules: scala.collection.mutable.Set[String] = scala.collection.mutable.Set()
  var puseRules: scala.collection.mutable.Set[String] = scala.collection.mutable.Set()
  var defRules: scala.collection.mutable.Set[String] = scala.collection.mutable.Set() //其它有相同def的规则

  // redexCtrl -- reactumCtrl
  var ctrlMap: Map[Control, Control] = calcCtrlMap;

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

  def calcCtrlMap: Map[Control, Control] = {
    var res: Map[Control, Control] = Map();
    var redexCtrls = getTermCtrls(redex);
    var reactumCtrls = getTermCtrls(reactum);

    // redex、reactum中Control数量不一致修正
    var redexCtrlsName = redexCtrls.map(_.name);
    var reactumCtrlsName = reactumCtrls.map(_.name);
    redexCtrls = redexCtrls.filter(x => reactumCtrlsName.contains(x.name));
    reactumCtrls = reactumCtrls.filter(x => {
      var isIn = redexCtrlsName.contains(x.name);
      if (!isIn) res += (x -> x);
      isIn;
    });
    var index = 0;
    if (redexCtrls.size == reactumCtrls.size) {
      reactumCtrls.map(x => {
        res += (x -> redexCtrls(index));
        index += 1;
      });
    }
    res;
  }

  // 计算Term中所有的Control
  def getTermCtrls(t: Term): List[Control] = {
    var ctrls: List[Control] = List();
    if (t.isInstanceOf[Prefix]) {
      var pre: Prefix = t.asInstanceOf[Prefix];
      ctrls = ctrls.:+(pre.ctrl);
      ctrls = getTermCtrls(pre.suffix) ::: ctrls;
    } else if (t.isInstanceOf[Paraller]) {
      var par: Paraller = t.asInstanceOf[Paraller];
      ctrls = getTermCtrls(par.leftTerm) ::: ctrls;
      ctrls = getTermCtrls(par.rightTerm) ::: ctrls;
    } else if (t.isInstanceOf[Regions]) {
      var reg: Regions = t.asInstanceOf[Regions];
      ctrls = getTermCtrls(reg.leftTerm) ::: ctrls;
      ctrls = getTermCtrls(reg.rightTerm) ::: ctrls;
    }
    ctrls = ctrls.sort((c1: Control, c2: Control) => { c1.name < c2.name });
    ctrls;
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
    var r = new ReactionRule("r_28", redex, reactum, "time:5 cond:power +=5%;fee+=3");

    //println("r.ctrlMap:" + r.ctrlMap);

    // ChargingRoom.Computer[connected:edge] | ConsultingRoom.(Patient[patient_prescription:edge,idle] | Computer[connected:edge].Prescription[patient_prescription:edge]) | $0 -> 
    // ChargingRoom.Computer[connected:edge].Bill[idle] | ConsultingRoom.(Patient[patient_prescription:edge,idle] | Computer[connected:edge].Prescription[patient_prescription:edge]) | $0;    
    var redexS2 = "ChargingRoom.Computer[connected:edge] | ConsultingRoom.(Patient[patient_prescription:edge,idle] | Computer[connected:edge].Prescription[patient_prescription:edge]) | $0";
    var reactums = "ChargingRoom.Computer[connected:edge].Bill[idle] | ConsultingRoom.(Patient[patient_prescription:edge,idle] | Computer[connected:edge].Prescription[patient_prescription:edge]) | $0";

  }
}