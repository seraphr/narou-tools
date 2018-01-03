package jp.seraphr.command

import scala.util.Try

/**
 */
trait Command {
  val name: String
  val description: String
  val version: String
  def run(aArgs: Seq[String]): Try[Unit]
}