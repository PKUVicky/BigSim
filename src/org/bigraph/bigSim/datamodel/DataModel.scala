package org.bigraph.bigsim.datamodel

import org.bigraph.bigsim._
import java.io.File
import scala.io.Source
import org.bigraph.bigsim.ReactionRule

class Data(n: String, dt: String, v: String, u: String) {
  val name = n
  val dataType = dt
  val unit = u
  var value = v
  val ratio = getRatio(unit)
  val percentage = getPercentage(unit)

  def getRatio(unit: String): Boolean = {
    unit.contains("/")
  }

  def getPercentage(unit: String): Boolean = {
    unit.equals("%")
  }
}

class WeightExpr(expr: String) {
  val expression = expr

  def getReport(): Double = {
    Calculator.apply(expression)
  }
}

object DataModel {
  var data: Map[String, Data] = Map()
  var wExpr: WeightExpr = null

  def addWeightExpr(expr: String) {
    wExpr = new WeightExpr(expr)
  }

  def getWeightExpr: String = {
    if (wExpr == null)
      ""
    else
      return "wExpr=" + wExpr.expression
  }

  def getValues: String = {
    var res: String = ""
    data.map(d => {
      res += d._1 + "=" + d._2.value+","
    })
    res
  }

  def getReport(): Double = {
    if (wExpr == null)
      0
    else
      wExpr.getReport
  }

  def parseData(path: String) {
    for (line <- Source.fromFile(path).getLines) {
      if (!line.startsWith("#")) {
        val items = line.split("\t");
        if (items.length != 4) {
          println("initial data format error!")
        } else {
          data += items(0) -> new Data(items(0), items(1), items(2), items(3))
        }
      }
    }
  }

  def getValue(name: String): Double = {
    if (!data.contains(name) || data(name).dataType.equals("String")) 0
    else data(name).value.toDouble
  }

  def update(name: String, exp: String) {
    if (data.contains(name))
      data(name).value = Calculator.apply(exp).toString
    else
      println("data update error!")
  }
}

object DataSpliter {

  /**
   * pre order data structure, and split data info.
   */
  def preOrderData(t: Term, position: String, rr: ReactionRule) {
    if (t == null) return ;
    else t.termType match {
      case TermType.TPREF => preOrderData(t.asInstanceOf[Prefix], position, rr);
      case TermType.TPAR => preOrderData(t.asInstanceOf[Paraller], position, rr);
      case TermType.TREGION => preOrderData(t.asInstanceOf[Regions], position, rr);
      case _ => return ;
    }
  }

  def preOrderData(t: Prefix, position: String, rr: ReactionRule) {
    updatePorts(t)
    if (Data.relations.contains(t.ctrl.name)) {
      rr.data += t
      updateParent(t, position)
    } else if (Data.dataCtrls.contains(t.ctrl.name))
      updateParent(t, position)
    else
      preOrderData(t.suffix, "suffix", rr)
  }

  def preOrderData(t: Paraller, position: String, rr: ReactionRule) {
    var rightTerm = t.rightTerm
    var grandfather = t.parent
    preOrderData(t.leftTerm, "leftTerm", rr)

    if (rightTerm.parent == grandfather) {
      updateSub(t.parent, rightTerm, position)
      preOrderData(rightTerm, position, rr)
    } else {
      preOrderData(t.rightTerm, "rightTerm", rr)
    }

  }

  def preOrderData(t: Regions, position: String, rr: ReactionRule) {
    var rightTerm = t.rightTerm
    var grandfather = t.parent
    preOrderData(t.leftTerm, "leftTerm", rr)
    if (rightTerm.parent == grandfather) {
      updateSub(t.parent, rightTerm, position)
      preOrderData(rightTerm, position, rr)
    } else
      preOrderData(t.rightTerm, "rightTerm", rr)
  }

  /**
   * update ports who contains data link
   */
  def updatePorts(t: Prefix) {
    var ports: List[Name] = List[Name]()
    ports.map(p => {
      if (Data.dataPorts.contains(p.name)) {
        p.name = "idle"
        p.nameType = "idle"
      }
    })
  }

  /**
   * update parent node
   */
  def updateParent(t: Term, position: String) {
    val positions = List("root", "leftTerm", "rightTerm",
      "suffix")

    position match {
      case "root" => println("bad format")
      case "suffix" => {
        t.parent.asInstanceOf[Prefix].suffix = new Nil()
      }
      case "leftTerm" => {
        if (t.parent.isInstanceOf[Paraller])
          t.parent.asInstanceOf[Paraller].rightTerm.parent = t.parent.parent
        else if (t.parent.isInstanceOf[Regions])
          t.parent.asInstanceOf[Regions].rightTerm.parent = t.parent.parent
      }
      case "rightTerm" => {
        if (t.parent.isInstanceOf[Paraller])
          t.parent.asInstanceOf[Paraller].leftTerm.parent = t.parent.parent
        else if (t.parent.isInstanceOf[Regions])
          t.parent.asInstanceOf[Regions].leftTerm.parent = t.parent.parent
      }
    }
  }

  /**
   * Parameters: parent, child, position: the position of child in its parent
   */
  def updateSub(parent: Term, child: Term, position: String) {
    parent.termType match {
      case TermType.TREGION => {
        if (position == "leftTerm" && parent != null)
          parent.asInstanceOf[Regions].leftTerm = child
        else
          parent.asInstanceOf[Regions].rightTerm = child
      }
      case TermType.TPAR => {
        if (position == "leftTerm" && parent != null)
          parent.asInstanceOf[Paraller].leftTerm = child
        else
          parent.asInstanceOf[Paraller].rightTerm = child
      }
      case TermType.TPREF => {
        parent.asInstanceOf[Prefix].suffix = child
      }
      case _ => return

    }
  }
}

object testDataModel {
  def main(args: Array[String]) {
    DataModel.parseData("MobileCloud/data/hotel.txt")
    DataModel.data.foreach(f => println(f._2.name + " " + f._2.ratio + " " + f._2.percentage))
    DataModel.addWeightExpr("(fee+energy*15+(0.5*5))")
    println(DataModel.getReport)

  }
}