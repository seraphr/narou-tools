package jp.seraphr.command

import scala.util.Try

/**
 */
trait Command { self =>
  val name: String
  val description: String
  val version: String
  def run(aArgs: Seq[String]): Try[Unit]

  protected abstract class CommandArgParser[ArgType] extends scopt.OptionParser[ArgType](self.name) {
    override def errorOnUnknownArgument: Boolean = true
    override def showUsageOnError: Boolean = true
    head(self.name, self.version)

    help("help").abbr("h")
  }
}