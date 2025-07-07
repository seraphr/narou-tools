package jp.seraphr.narou

import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.{ BigGenre, Genre, NovelApiResponse, SearchParams }

import monix.eval.Task

/**
 */
case class NarouClientBuilder(build: SearchParams => SearchParams) {
  def buildParams: SearchParams = build(SearchParams())

  def n(f: SearchParams => SearchParams): NarouClientBuilder = {
    this.copy(build = build andThen f)
  }

  def opt[A](f: NarouClientBuilder => A => NarouClientBuilder)(aOptA: Option[A]): NarouClientBuilder = {
    aOptA.fold(this)(f(this))
  }

  def seq[A](f: NarouClientBuilder => A => NarouClientBuilder)(aSeq: Seq[A]): NarouClientBuilder = {
    aSeq.foldLeft(this)((builder, a) => f(builder)(a))
  }

  def order(aOrder: String)            = this.n(_.copy(order = Some(aOrder)))
  def skipLim(aSkip: Int, aLimit: Int) = {
    val withSt = if (aSkip == 0) this else this.n(_.copy(st = Some(aSkip)))
    withSt.n(_.copy(lim = Some(aLimit)))
  }

  def pickup(aIsPickup: Boolean)                   = this.n(_.copy(pickup = Some(aIsPickup)))
  def genre(aGenre: Genre)                         = this.n(_.copy(genre = Some(aGenre)))
  def bigGenre(aGenre: BigGenre)                   = this.n(_.copy(biggenre = Some(aGenre)))
  def length(aMin: Option[Int], aMax: Option[Int]) = {
    this.n(params => params.copy(minlen = aMin, maxlen = aMax))
  }

  def ncodes(aNCodes: Array[String]) = {
    this.n(_.copy(ncode = Some(aNCodes.mkString("-"))))
  }

  // 旧APIとの互換性のため
  def setNCode(aNCodes: Array[String]) = ncodes(aNCodes)

  def search(client: NarouApiClient): Task[NovelApiResponse] = {
    client.search(buildParams)
  }

  // 旧APIとの互換性のため（deprecated）
  def buildFromEmpty: CompatWrapper = {
    new CompatWrapper(buildParams)
  }

  def build(narou: narou4j.Narou): CompatWrapper = {
    new CompatWrapper(buildParams)
  }

  class CompatWrapper(aParams: SearchParams) {
    def getNovels: List[narou4j.entities.Novel] = {
      // TODO: aParamsを使用して新しいAPIから旧APIの形式に変換する実装を追加
      throw new NotImplementedError(s"This compatibility method is not yet implemented for params: $aParams")
    }

  }

}

object NarouClientBuilder {
  val init = NarouClientBuilder(identity)
}
