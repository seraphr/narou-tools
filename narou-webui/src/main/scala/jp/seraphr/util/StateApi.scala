package jp.seraphr.util

import cats.data.OptionT
import monix.eval.Task

trait StateApi[S] {
  type TaskOpt[A] = OptionT[Task, A]
  type TaskOptS   = TaskOpt[S]

  def getCurrentState(): Task[S]
  def modStateTaskOpt(f: S => TaskOpt[S => S]): TaskOpt[S]

  def setState(s: S): Task[Unit]                      = setStateTask(Task.now(s))
  def modState(f: S => S): Task[S]                    = genStateTask(s => Task.now(f(s)))
  def modStateOpt(f: S => Option[S]): TaskOpt[S]      = genStateTaskOpt(s => OptionT(Task.now(f(s))))
  def setStateTask(s: Task[S]): Task[Unit]            = genStateTask(_ => s).map(_ => ())
  def genStateTask(f: S => Task[S]): Task[S]          = modStateTask(s => f(s).map(s => _ => s))
  def genStateTaskOpt(f: S => TaskOpt[S]): TaskOpt[S] = modStateTaskOpt(s => f(s).map(s => _ => s))
  def modStateTask(f: S => Task[S => S]): Task[S]     = modStateTaskOpt(s => OptionT.liftF(f(s))).value.map(_.get)
}

class DefaultStateApi[S](get: () => S, mod: (S => S) => S) extends StateApi[S] {
  override def getCurrentState(): Task[S]                           = Task(get())
  override def modStateTaskOpt(f: S => TaskOpt[S => S]): TaskOpt[S] = for {
    s    <- OptionT.liftF(getCurrentState())
    modF <- f(s)
  } yield mod(modF)

}
