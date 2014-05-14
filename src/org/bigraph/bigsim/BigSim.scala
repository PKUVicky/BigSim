package org.bigraph.bigsim

import java.io.File

import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.parser.BGMParser
import org.bigraph.bigsim.parser.BGMTerm
import org.bigraph.bigsim.parser.HMM
import org.bigraph.bigsim.simulator.Simulator
import org.bigraph.bigsim.utils.GlobalCfg

object BigSim extends App {
  def usage = System.err.println("""    
Usage: BigSim [options] <filename> 
(Default configuration is set in config.properties)
  Options:
    -h --help				Display this help and exit.
    -s c						Specify c maximum steps of graph unfolding
    -S l						Specify l loops of simulation
    -m t						Specify t is the max simulation time
    -M mode				Specify simulation mode: Enum, SSA(stochastic), TS(time-slicing), DE(dicrete-event)
    -B mode				Specify BRS mode: TR-BRS, GR-BRS
    -T							Simulation with time
    -D						Calculate environment data
    -C						Check conditions of reaction rules
    -v							Print version information and exit.
    -V						Print verbose output while running.
    -P file					Specify the path output file
    -R file					Output the simulation report to a dot file.
               """)

  def version = println("BigSim v0.1\n2014")

  //    -D st path dpath	Specify the data flow analysis strategy and output path,and defs mapping pathes 
  //    -sf file					Specify the sorting file.
  //    -r x						Output statistics and graphs every x steps (default: 500)
  //     -PF file path			Specify the patterns file and the output path 	
  //     -p							Print new states as they are discovered.

  def parseOpts(args: List[String]): Unit = args match {
    case ("-sf" :: f :: t) => {
      GlobalCfg.sortFileName = f
      parseOpts(t)
    }
    case ("-G" :: f :: t) => {
      GlobalCfg.graphOutput = f
      parseOpts(t)
    }
    case ("-P" :: f :: t) => {
      GlobalCfg.pathOutput = f
      parseOpts(t)
    }
    case ("-D" :: st :: path :: dpath :: t) => {
      if (st.equals("alldefs")) {
        GlobalCfg.allDefs = true
      } else if (st.equals("alluses")) {
        GlobalCfg.allUses = true
      }
      GlobalCfg.pathOutput = path
      GlobalCfg.defPathMapFile = dpath

      parseOpts(t)
    }
    case ("-PF" :: file :: path :: t) => {
      GlobalCfg.patterns = true
      GlobalCfg.patternFile = file
      GlobalCfg.pathOutput = path
      parseOpts(t)
    }
    case (("-h" | "--help") :: t) => {
      usage
      System.exit(0)
    }
    case ("-m" :: x :: t) => {
      GlobalCfg.maxSteps = x.toInt
      parseOpts(t)
    }
    case ("-p" :: t) => {
      GlobalCfg.printDiscovered = true
      parseOpts(t)
    }
    case ("-l" :: t) => {
      GlobalCfg.localCheck = true
      parseOpts(t)
    }
    case ("-r" :: x :: t) => {
      GlobalCfg.reportFrequency = x.toInt
      parseOpts(t)
    }
    case ("-S" :: t) => {
      GlobalCfg.stochastic = true
      parseOpts(t)
    }
    case ("-v" :: t) => {
      version
      System.exit(0)
    }
    case ("-V" :: t) => {
      GlobalCfg.verbose = true
      parseOpts(t)
    }
    case n :: Nil => {
      GlobalCfg.filename = n
    }
    case _ => {
      usage
      System.exit(1)
    }
  }

  override def main(args: Array[String]) = {
    var start = System.currentTimeMillis();

    if (args.length != 0) {
      parseOpts(args.toList)
    }

    /**
     * init sorting file if exits
     */
    //BigSimOpts.sortFileName = "sortingFile/decoction.xml"
    if (GlobalCfg.sortFileName != null)
      Bigraph.sorting.init(GlobalCfg.sortFileName)

    var filename = "earthquake";
    filename = "chemistry";
    filename = "checker";
    var folderName = "OldAirport";
    folderName = "Examples/MobileCloud";

    // GlobalCfg.patterns = true
    //GlobalCfg.patternFile = "resources/Patterns.xml"
    GlobalCfg.pathOutput = folderName + "/paths/" + filename + ".txt";
    GlobalCfg.filename = folderName + "/models/" + filename + ".bgm";

    // graphviz图形化字符串
    GlobalCfg.graphOutput = folderName + "/results/" + filename + ".dot";

    // 解析BGM
    val p = BGMParser.parse(new File(GlobalCfg.filename));
    val b: Bigraph = BGMTerm.toBigraph(p);

    /**
     * init Data if needed
     */
    if (GlobalCfg.checkData)
      Data.parseData(folderName + "/data/" + filename + ".txt")

    if (GlobalCfg.checkHMM)
      HMM.parseHMM(folderName + "/hmm/" + filename + ".hmm")

    Simulator.simulate("TimeSlicingSimulator", b)

    for (i <- 1 to GlobalCfg.simLoop) {
      GlobalCfg.SysClk = 0
      println("<--------------------------- Sim " + i + " ------------------------------------>")
      //var sim = new DiscreteEventSimulator(b)
      //sim.simulate;
      println("<---------------------------- End ------------------------------------->")
    }
    // val rc = new ReachChecker(io.Source.fromFile(new File(GlobalCfg.filename)).mkString)
    //println(rc.check)

    var end = System.currentTimeMillis();
    println("start:" + start + ", end:" + end + ", used:" + (end - start));
  }
}