package jp.seraphr.narou.reactive

import monix.execution.Scheduler
import monix.reactive.Observable
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class ObservableUtilsTest extends AsyncFreeSpec with Matchers {

  import ObservableUtils._
  implicit override def executionContext: Scheduler = Scheduler.global

  "ObservableOps" - {
    "grouped" - {
      "n個ずつの要素をもつObservableになること" in {
        val tBaseList = List.iterate(0, 10)(_ + 1)
        val tObs      = Observable.fromIterable(tBaseList).map { i => println(s"gen: ${i}"); i }

        val tActualFuture = tObs.grouped(3).mapEval(_._2.toListL).toListL.runToFuture
        tActualFuture.map { tActual =>
          tActual shouldBe tBaseList.grouped(3).toList
        }
      }
    }
  }
}
