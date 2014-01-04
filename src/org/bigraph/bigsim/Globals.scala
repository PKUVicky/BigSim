package org.bigraph.bigsim

object GlobalCfg {

  val ALL: Int = -1;
  val STAR: Int = -2;
  val PLUS: Int = -3;

  /**
   * For condition configurations
   */
  val condPrefStr: String = "Cond:"
  val exprPrefStr: String = "Expr:"
  val sysClkPrefStr: String = "SysClk:"
  val randomPrefStr: String = "Rand:"

  var DEBUG: Boolean = false;
  var checkLocal: Boolean = false;
  var maxSteps: Long = 50;
  var reportInterval: Long = 0;
  var printMode: Boolean = true;
  var graphOutput: String = "";

  // whether contain time simulation
  var checkTime: Boolean = true;
  // system clock init is 0
  var SysClk: Int = 0
  // system clock increaser is 1
  var SysClkIncr: Int = 1
  // how many times of simulation
  var simLoop: Int = 1;
  // whether sim data
  var checkData: Boolean = true;

  var allDefs: Boolean = false
  var allUses: Boolean = true
  var patterns: Boolean = false

  var patternFile: String = ""
  var pathOutput: String = ""
  var defPathMapFile: String = ""

  var instance: Boolean = true
}

object BigSimOpts {
  var sortFileName: String = null;
  var graphOutput: String = ""
  var maxSteps: Int = 1000
  var printDiscovered: Boolean = false
  var localCheck: Boolean = false
  var reportFrequency: Int = 500
  var verbose: Boolean = false
  var filename: String = ""
  var stochastic: Boolean = false
}
