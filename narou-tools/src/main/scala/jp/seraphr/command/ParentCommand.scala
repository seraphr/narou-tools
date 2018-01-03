package jp.seraphr.command

import jp.seraphr.command.ParentCommand.ParentCommandArg

import scala.util.Try

/**
 */
trait ParentCommand extends Command { self =>
  protected val subCommands: Seq[Command]
  override def run(aArgs: Seq[String]): Try[Unit] = Try {
    val (tHead, tTail) = aArgs.splitAt(1)
    ArgsParser.parse(tHead, ParentCommandArg(None)).flatMap(_.subCommand).foreach(_.run(tTail))
  }

  object ArgsParser extends scopt.OptionParser[ParentCommandArg](self.name) {
    override def errorOnUnknownArgument: Boolean = true
    override def showUsageOnError: Boolean = true
    head(self.name, self.version)
    note("各サブコマンドの詳細は、各々のヘルプを参照してください")

    subCommands.foreach { tSub =>
      cmd(tSub.name)
        .text(tSub.description)
        .action { (_, c) => c.copy(subCommand = Some(tSub)) }
    }

    help("help").abbr("h")
  }
}

object ParentCommand {
  case class ParentCommandArg(subCommand: Option[Command])
}