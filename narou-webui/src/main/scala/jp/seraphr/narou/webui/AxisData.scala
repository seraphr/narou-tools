package jp.seraphr.narou.webui

import jp.seraphr.narou.model.NarouNovel

/**
 * 各軸のデータ。 大体 x / y の値を表す
 *
 * @param toValue 小説のデータを軸の値に変換する。 Noneになった場合、その小説を値として利用しない
 * @param name 軸の値の名前
 * @param unit 値の単位
 */
case class AxisData(toValue: NarouNovel => Option[Int], name: String, unit: String = "")
object AxisData {
  implicit class ToOption[A](a: A) {
    def option: Option[A] = Option(a)
  }
  val bookmark = AxisData(_.bookmarkCount.option, "bookmark")
  val evaluationPoint = AxisData(_.evaluationPoint.option, "評価ポイント", "pt")
  val globalPoint = AxisData(_.globalPoint.option, "総合ポイント", "pt")
  val evaluationPerBookmark = AxisData(n => if (n.bookmarkCount == 0) None else (n.evaluationPoint * 1000 / n.bookmarkCount).option, "評価ポイント/ブックマーク")
}