package org.bigraph.bigsim.utils

import java.util.Properties
import java.io.FileInputStream

object GlobalCfg {

  val prop: Properties = new Properties

  try {
    prop.load(new FileInputStream("config.properties"))
  } catch {
    case e: Exception =>
      e.printStackTrace
      sys.exit(1)
  }

  /**
   * For condition configurations
   */
  val condPrefStr: String = prop.getProperty("condPrefStr")
  val exprPrefStr: String = prop.getProperty("exprPrefStr")
  val sysClkPrefStr: String = prop.getProperty("sysClkPrefStr")
  val randomPrefStr: String = prop.getProperty("randomPrefStr")
  val wExprPrefStr: String = prop.getProperty("wExprPrefStr")
  val hmmPrefStr: String = prop.getProperty("hmmPrefStr")
  val ratePrefStr: String = prop.getProperty("ratePrefStr")
  val reversePrefStr: String = prop.getProperty("reversePrefStr")
  val reactPrefStr: String = prop.getProperty("reactPrefStr")
  val minProbability: Double = prop.getProperty("minProbability").toDouble

  var DEBUG: Boolean = prop.getProperty("DEBUG").toBoolean
  var checkLocal: Boolean = prop.getProperty("checkLocal").toBoolean
  var maxSteps: Long = prop.getProperty("maxSteps").toLong
  var reportInterval: Long = prop.getProperty("reportInterval").toLong
  var printMode: Boolean = prop.getProperty("printMode").toBoolean
  var ranNameIndex: Int = prop.getProperty("ranNameIndex").toInt

  // set output path and graph
  var outputPath: Boolean = prop.getProperty("outputPath").toBoolean
  var pathOutput: String = prop.getProperty("pathFile")
  var outputGraph: Boolean = prop.getProperty("outputGraph").toBoolean
  var graphOutput: String = prop.getProperty("graphOutput")
  var outputData: Boolean = prop.getProperty("outputData").toBoolean
  var dataOutput: String = prop.getProperty("dataOutput")

  // system clock init
  var SysClk: Double = prop.getProperty("initSysClk").toDouble
  // system clock increaser
  var SysClkIncr: Double = prop.getProperty("sysClkIncr").toDouble
  // the max system clock
  var maxSysClk: Double = prop.getProperty("maxSysClk").toDouble

  def getRanNameIndex: Int = {
    ranNameIndex += 1
    ranNameIndex
  }

  // whether contain time simulation
  var checkTime: Boolean = true;

  // how many times of simulation
  var simLoop: Int = 10;
  var curLoop: Int = 0;
  var append: Boolean = false
  // whether sim data
  var checkData: Boolean = true
  // whether check HMM
  var checkHMM: Boolean = false

  var allDefs: Boolean = false
  var allUses: Boolean = false
  var patterns: Boolean = false

  var patternFile: String = ""
  var defPathMapFile: String = ""

  var node: Boolean = true
  var sortFileName: String = null;
  var verbose: Boolean = false
  var printDiscovered: Boolean = false
  var localCheck: Boolean = false
  var reportFrequency: Int = 500
  var filename: String = ""
  var stochastic: Boolean = false
}
