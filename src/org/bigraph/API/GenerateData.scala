package org.bigraph.API

object GenerateData {

  def main(args: Array[String]): Unit = {

    /*        println("""#Airplane
ap.boardingTime	Double	35	mins
ap.flightNo	String	CZ3265	unit""") */

    for (i <- 1 to 1) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	true	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 2 to 2) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	true	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 3 to 20) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 21 to 21) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	false	boolean
p""" + i + """.hasDanger	String	true	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 22 to 53) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 54 to 54) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	false	boolean
p""" + i + """.hasDanger	String	true	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 55 to 60) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	false	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 61 to 76) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	false	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 77 to 77) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 78 to 78) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 79 to 79) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	false	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 80 to 80) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	false	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 81 to 81) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 82 to 82) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	false	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 83 to 83) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 84 to 84) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 85 to 85) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 86 to 86) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	true	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 87 to 90) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	false	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 91 to 95) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }

    for (i <- 96 to 100) {
      var light = scala.util.Random.nextInt(11)
      if (light > 10)
        light = 10
      else if (light <= 0)
        light = 1

      print("""#Passenger p""" + i + """
p""" + i + """.traffic	String	true	boolean
p""" + i + """.flightNo	String	CZ3265	unit
p""" + i + """.fee	Double	0	yuan
p""" + i + """.light	Double	""" + light + """	degree
p""" + i + """.hasCheckinLuggage	String	true	boolean
p""" + i + """.hasDanger	String	false	boolean
p""" + i + """.updateLight	String	false	boolean
p""" + i + """.updateBillboard	String	false	boolean
""")
    }
  }

}