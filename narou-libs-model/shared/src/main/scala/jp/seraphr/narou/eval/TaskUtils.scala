package jp.seraphr.narou.eval

import monix.eval.Task

object TaskUtils {
  implicit class TaskOps[A](private val t: Task[A]) extends AnyVal {
    def retryBackoff(maxRetry: Int = 5, initSleepMillis: Int = 400): Task[A] = {
      t.onErrorRecoverWith {
        case _ if 0 < maxRetry =>
          import scala.concurrent.duration._
          val tSleep = (initSleepMillis / 2) + (math.random() * initSleepMillis / 2).toInt
          Task.sleep(tSleep.millis).flatMap { _ =>
            t.retryBackoff(maxRetry - 1, initSleepMillis * 2) 
          }
        case e => Task.raiseError(e)
      }
    }
  }
}
