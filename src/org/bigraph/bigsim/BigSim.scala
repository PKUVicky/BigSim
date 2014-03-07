package org.bigraph.bigsim

import java.io.File
import org.bigraph.bigsim.datamodel.DataModel
import org.bigraph.bigsim.simulator._
import org.bigraph.bigsim.strategy.HMM
import org.bigraph.bigsim.utils._

object BigSim extends App {
  def usage = System.err.println("""    
Usage: bigsim [options] <filename>

  Options:
    -G file     Output the reaction graph to a dot file.
    -h --help   Display this help and exit.
    -l          Local check mode - do not build the reaction graph.
    -m x        Specify x maximum steps of graph unfolding (default: 1000)
    -p          Print new states as they are discovered.
    -r x        Output statistics and graphs every x steps (default: 500)
    -S          Enable stochastic simulation
    -v          Print version information and exit.
    -V          Print verbose output while running.
    -sf file    Specify the sorting file.
    -P file		specify the path output file
    -D st path dpath  	Specify the data flow analysis strategy and output path,and defs mapping pathes 
    -PF file path   	Specify the patterns file and the output path 	
               """)

  def version = println("BigSim v0.1\n2013")

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

  /**
   * registerPredicates
   * five predicates: empty, size, matches, terminal, equal
   */
  def registerPredicates: Unit = {
    Predicate.registerPredicate("empty", new PredEmpty());
    Predicate.registerPredicate("size", new PredSize());
    Predicate.registerPredicate("matches", new PredMatches());
    Predicate.registerPredicate("terminal", new PredTerminal());
    Predicate.registerPredicate("equal", new PredEqual());
  }

  override def main(args: Array[String]) = {
    var start = System.currentTimeMillis();

    if (args.length == 0) {
      usage
      System.exit(1)
    } else {
      parseOpts(args.toList)

      registerPredicates;

      /**
       * init sorting file if exits
       */
      //BigSimOpts.sortFileName = "sortingFile/decoction.xml"
      if (GlobalCfg.sortFileName != null)
        Bigraph.sorting.init(GlobalCfg.sortFileName)

      var filename = "earthquake";
      filename = "checker";
      //filename = "EconomNormal";

      // GlobalCfg.patterns = true
      //GlobalCfg.patternFile = "resources/Patterns.xml"
      GlobalCfg.pathOutput = "MobileCloud/paths/" + filename + ".txt";

      GlobalCfg.filename = "MobileCloud/models/" + filename + ".bgm";

      // graphviz图形化字符串
      GlobalCfg.graphOutput = "MobileCloud/results/" + filename + ".dot";

      // 解析BGM
      val p = BGMParser.parse(new File(GlobalCfg.filename));
      // println(p)
      val b: Bigraph = BGMTerm.toBigraph(p);
      //println(b);

      /**
       * init Data if needed
       */
      if (GlobalCfg.checkData)
        DataModel.parseData("MobileCloud/data/" + filename + ".txt")
      
      if (GlobalCfg.checkHMM)
        HMM.parseHMM("MobileCloud/hmm/" + filename + ".hmm")  

      val m = new MC(b)
      //m.check;

      val sim = new StochasticSimulator(b)
      sim.simulate

      for (i <- 1 to GlobalCfg.simLoop) {
        GlobalCfg.SysClk = 0
        println("<--------------------------- Sim " + i + " ------------------------------------>")
        //var sim = new Simulator(b)
        //sim.simulate;
        println("<---------------------------- End ------------------------------------->")

      }
      val rc = new ReachChecker(io.Source.fromFile(new File(GlobalCfg.filename)).mkString)
      //println(rc.check)

      var end = System.currentTimeMillis();
      println("start:" + start + ", end:" + end + ", used:" + (end - start));
    }
  }
}