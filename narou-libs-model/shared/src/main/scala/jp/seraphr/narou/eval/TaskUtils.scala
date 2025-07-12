package jp.seraphr.narou.eval

import monix.eval.Task

object TaskUtils {
  implicit class TaskOps[A](private val t: Task[A]) extends AnyVal {

    /** 指数バックオフによるリトライを実行します */
    def retryBackoff(aMaxRetry: Int = 5, aInitSleepMillis: Int = 400): Task[A] = {
      t.onErrorRecoverWith {
        case _ if 0 < aMaxRetry =>
          import scala.concurrent.duration._
          val tSleep = (aInitSleepMillis / 2) + (math.random() * aInitSleepMillis / 2).toInt
          Task
            .sleep(tSleep.millis)
            .flatMap { _ =>
              t.retryBackoff(aMaxRetry - 1, aInitSleepMillis * 2)
            }
        case e                  => Task.raiseError(e)
      }
    }

  }

  implicit class TaskObjOps(private val t: Task.type) extends AnyVal {

    /** 非同期述語でコレクションをフィルタリングします */
    def filter[A](aCol: Seq[A])(aPredicate: A => Task[Boolean]): Task[Seq[A]] = {
      Task
        .parTraverse(aCol)(a => aPredicate(a).map(_ -> a))
        .map {
          _.collect { case (true, a) =>
            a
          }
        }
    }

  }
}
