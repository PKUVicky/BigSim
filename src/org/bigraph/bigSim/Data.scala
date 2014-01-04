package org.bigraph.bigSim

object Data {
  var dataCtrls: List[String] = null
  var dataPorts: List[String] = null
  var relations: List[String] = List("Greater", "Less", "Equal", "NotEqual",
    "LessOrEqual", "GreaterOrEqual")
  var dataValues: Map[String, String] = null

  dataCtrls = List()
  dataPorts = List()
  dataValues = Map("leftValue" -> "10", "rightValue" -> "20",
    "patient_leftValue" -> "20", "patient_rightValue" -> "20",
    "d" -> "20", "g" -> "15", "h" -> "13", "e" -> "16", "age_value_is" -> "14")

  def relationDecision(relation: Prefix): Boolean = {
    if (relation.port.size != 2) {
      println("relation prefix must have 2 ports, not " + relation.toString)
      false
    } else {
      var leftValue = dataValues(relation.port(0).name)
      var rightValue = dataValues(relation.port(1).name)

      if (leftValue != null && rightValue != null) {
        relation.ctrl.name match {
          case "Greater" => leftValue > rightValue
          case "Less" => leftValue < rightValue
          case "Equal" => leftValue == rightValue
          case "NotEqual" => leftValue != rightValue
          case "LessOrEqual" => leftValue <= rightValue
          case "GreaterOrEqual" => leftValue >= rightValue
          case _ => true
        }
      } else {
        true
      }
    }
  }
}

object testData {
  def main(args: Array[String]) {

    println(Data.relationDecision(TermParser.apply("Greater[leftValue, rightValue]").asInstanceOf[Prefix]))
  }
}
  