package org.bigraph.API

object Main {

  def main(args: Array[String]): Unit = {

    /*        println("""#Airplane
ap.boardingTime	Double	35	mins
ap.flightNo	String	CZ3265	unit""") */

    for (i <- 61 to 76) {
      //print("p"+i+":Passenger[isDisable:edge,isEconomy:edge].(t"+i+":Ticket[idle] | c"+i+":CheckinLuggage | d"+i+":Danger) | ");

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	4	degree
p""" + i + """.hasCheckinLuggage	String	false	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
""")
    }

  }

}