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

  val ALL: Int = -1;
  val STAR: Int = -2;
  val PLUS: Int = -3;

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
  val minProbability: Double = prop.getProperty("minProbability").toDouble

  var DEBUG: Boolean = prop.getProperty("DEBUG").toBoolean
  var checkLocal: Boolean = prop.getProperty("checkLocal").toBoolean
  var maxSteps: Long = prop.getProperty("maxSteps").toLong
  var reportInterval: Long = prop.getProperty("reportInterval").toLong
  var printMode: Boolean = prop.getProperty("printMode").toBoolean
  var graphOutput: String = prop.getProperty("graphOutput")
  var ranNameIndex: Int = prop.getProperty("ranNameIndex").toInt

  def getRanNameIndex: Int = {
    ranNameIndex += 1
    ranNameIndex
  }

  // whether contain time simulation
  var checkTime: Boolean = true;
  // system clock init is 0
  var SysClk: Int = 0
  // system clock increaser is 1
  var SysClkIncr: Int = 1
  // how many times of simulation
  var simLoop: Int = 1;
  // whether sim data
  var checkData: Boolean = true
  // whether check HMM
  var checkHMM: Boolean = true

  var allDefs: Boolean = false
  var allUses: Boolean = false
  var patterns: Boolean = false

  var patternFile: String = ""
  var pathOutput: String = ""
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
