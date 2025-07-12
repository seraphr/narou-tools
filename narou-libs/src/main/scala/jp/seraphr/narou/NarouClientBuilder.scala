package jp.seraphr.narou

import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.{ BigGenre, Genre, NovelApiResponse, SearchParams, OrderType, ParamRange, SearchTargetFlag, NovelTypeFilter, BuntaiType, StopStatus, DateRange }

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

  def order(aOrder: OrderType)         = this.n(_.copy(order = Some(aOrder)))
  def skipLim(aSkip: Int, aLimit: Int) = {
    val withSt = if (aSkip == 0) this else this.n(_.copy(st = Some(aSkip)))
    withSt.n(_.copy(lim = Some(aLimit)))
  }

  def pickup(aIsPickup: Boolean)                   = this.n(_.copy(ispickup = Some(aIsPickup)))
  def genre(aGenre: Genre)                         = this.n(params => params.copy(genre = params.genre :+ aGenre))
  def bigGenre(aGenre: BigGenre)                   = this.n(params => params.copy(biggenre = params.biggenre :+ aGenre))
  def length(aMin: Option[Int], aMax: Option[Int]) = {
    this.n(params => params.copy(minlen = aMin, maxlen = aMax))
  }

  def lengthRange(aRange: ParamRange) = this.n(_.copy(length = Some(aRange)))

  def ncodes(aNCodes: Array[String]) = {
    this.n(_.copy(ncode = aNCodes.toSeq))
  }

  def addNcode(aNcode: String) = this.n(params => params.copy(ncode = params.ncode :+ aNcode))

  // 新しいメソッドの追加
  def word(aWord: String) = this.n(_.copy(word = Some(aWord)))
  def notword(aNotword: String) = this.n(_.copy(notword = Some(aNotword)))
  
  def title(aFlag: SearchTargetFlag) = this.n(_.copy(title = Some(aFlag)))
  def ex(aFlag: SearchTargetFlag) = this.n(_.copy(ex = Some(aFlag)))
  def keyword(aFlag: SearchTargetFlag) = this.n(_.copy(keyword = Some(aFlag)))
  def wname(aFlag: SearchTargetFlag) = this.n(_.copy(wname = Some(aFlag)))
  
  def addGenres(aGenres: Seq[Genre]) = this.n(params => params.copy(genre = params.genre ++ aGenres))
  def addBigGenres(aBigGenres: Seq[BigGenre]) = this.n(params => params.copy(biggenre = params.biggenre ++ aBigGenres))
  
  def novelType(aType: NovelTypeFilter) = this.n(_.copy(`type` = Some(aType)))
  def buntai(aBuntai: BuntaiType) = this.n(params => params.copy(buntai = params.buntai :+ aBuntai))
  def stop(aStop: StopStatus) = this.n(_.copy(stop = Some(aStop)))
  
  def timeRange(aRange: ParamRange) = this.n(_.copy(time = Some(aRange)))
  def kaiwarituRange(aRange: ParamRange) = this.n(_.copy(kaiwaritu = Some(aRange)))
  def sasieRange(aRange: ParamRange) = this.n(_.copy(sasie = Some(aRange)))
  
  def lastup(aRange: DateRange) = this.n(_.copy(lastup = Some(aRange)))
  def lastupdate(aRange: DateRange) = this.n(_.copy(lastupdate = Some(aRange)))

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
