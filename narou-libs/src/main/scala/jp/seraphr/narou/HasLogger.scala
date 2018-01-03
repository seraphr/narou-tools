package jp.seraphr.narou

import org.slf4j.LoggerFactory

/**
 */
trait HasLogger {
  val logger = LoggerFactory.getLogger(this.getClass)
}
