package jp.seraphr.narou.reactive

import monix.reactive.Observable

object ObservableUtils {

  implicit class ObservableOps[A](aObs: Observable[A]) {

    /**
     * @note 実装上、aObsが終了するまで各グループのObservableは終了しない。
     *       groupByと似た実装をすれば、groupedも実装できることはわかっているが、groupByの重要部分がprivateで構成されているため、コードのコピーが要る
     * @param n
     * @return [[Long]]は n個ごとに値を分割した時の何個目のグループかを表す整数値（0-origin）
     */
    def grouped(n: Int): Observable[(Long, Observable[A])] = {
      if (n <= 0) throw new IllegalArgumentException(s"nは1以上でないといけないが、${n}だった")
      aObs
        .zipWithIndex
        .groupBy { case (_, tIndex) =>
          tIndex / n
        }
        .map(tGrouped => (tGrouped.key, tGrouped.map(_._1)))
    }

  }
}
