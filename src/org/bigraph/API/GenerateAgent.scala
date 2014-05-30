package org.bigraph.API

object GenerateAgent {

  def main(args: Array[String]): Unit = {

    /*        println("""#Airplane
ap.boardingTime	Double	35	mins
ap.flightNo	String	CZ3265	unit""") */

    for (i <- 1 to 1) {
      print("p" + i + ":Passenger[isAdult:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage | d" + i + ":Danger) | ");
    }

    for (i <- 2 to 2) {
      print("p" + i + ":Passenger[isAdult:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage | d" + i + ":Danger) | ");
    }

    for (i <- 3 to 20) {
      print("p" + i + ":Passenger[isAdult:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 21 to 21) {
      print("p" + i + ":Passenger[isAdult:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | d" + i + ":Danger) | ");
    }

    for (i <- 22 to 53) {
      print("p" + i + ":Passenger[isAdult:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 54 to 54) {
      print("p" + i + ":Passenger[isAdult:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | d" + i + ":Danger) | ");
    }

    for (i <- 55 to 60) {
      print("p" + i + ":Passenger[isAdult:edge,isEconomy:edge].(t" + i + ":Ticket[idle]) | ");
    }

    for (i <- 61 to 76) {
      print("p" + i + ":Passenger[isAdult:edge,isEconomy:edge].(t" + i + ":Ticket[idle]) | ");
    }

    for (i <- 77 to 77) {
      print("p" + i + ":Passenger[isAdult:edge,isBusiness:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 78 to 78) {
      print("p" + i + ":Passenger[isAdult:edge,isBusiness:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 79 to 79) {
      print("p" + i + ":Passenger[isAdult:edge,isBusiness:edge].(t" + i + ":Ticket[idle]) | ");
    }

    for (i <- 80 to 80) {
      print("p" + i + ":Passenger[isAdult:edge,isBusiness:edge].(t" + i + ":Ticket[idle]) | ");
    }

    for (i <- 81 to 81) {
      print("p" + i + ":Passenger[isDisable:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 82 to 82) {
      print("p" + i + ":Passenger[isChild:edge,isEconomy:edge].(t" + i + ":Ticket[idle]) | ");
    }

    for (i <- 83 to 83) {
      print("p" + i + ":Passenger[isChild:edge,isBusiness:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 84 to 84) {
      print("p" + i + ":Passenger[isAdultWithChild:edge,isBusiness:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 85 to 85) {
      print("p" + i + ":Passenger[isAdultChild:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 86 to 86) {
      print("p" + i + ":Passenger[isOld:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage | d" + i + ":Danger) | ");
    }

    for (i <- 87 to 90) {
      print("p" + i + ":Passenger[isOld:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 91 to 95) {
      print("p" + i + ":Passenger[isOld:edge,isBusiness:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }

    for (i <- 96 to 100) {
      print("p" + i + ":Passenger[isOld:edge,isEconomy:edge].(t" + i + ":Ticket[idle] | c" + i + ":CheckinLuggage) | ");
    }
  }

}