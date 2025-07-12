package jp.seraphr.narou.reactive

import monix.reactive.Observable

object ObservableUtils {

  implicit class ObservableOps[A](aObs: Observable[A]) {

    /**
     * Observableを指定された数でグループ化します
     * @note 実装上、aObsが終了するまで各グループのObservableは終了しない。
     *       groupByと似た実装をすれば、groupedも実装できることはわかっているが、groupByの重要部分がprivateで構成されているため、コードのコピーが要る
     * @param aN グループサイズ
     * @return [[Long]]は n個ごとに値を分割した時の何個目のグループかを表す整数値（0-origin）
     */
    def grouped(aN: Int): Observable[(Long, Observable[A])] = {
      if (aN <= 0) throw new IllegalArgumentException(s"nは1以上でないといけないが、${aN}だった")
      aObs
        .zipWithIndex
        .groupBy { case (_, tIndex) =>
          tIndex / aN
        }
        .map(tGrouped => (tGrouped.key, tGrouped.map(_._1)))
    }

  }
}
