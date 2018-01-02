package jp.seraphr.narou

import narou4j.Narou
import narou4j.enums.{NovelBigGenre, NovelGenre, OutputOrder}

/**
  */
case class NarouClientBuilder(build: Narou => Narou) {
  def n[U](f: Narou => U): NarouClientBuilder = {
    val g: Narou => Narou = _.tap(f)
    this.copy(build = build andThen g)
  }

  def opt[A](f: NarouClientBuilder => A => NarouClientBuilder)(aOptA: Option[A]): NarouClientBuilder = aOptA.fold(this)(f(this))

  def order(aOrder: OutputOrder) = this.n(_.setOrder(aOrder))
  def skipLim(aSkip: Int, aLimit: Int) = {
    val tSkipped = if(aSkip == 0) this else this.n(_.setSt(aSkip))
    tSkipped.n(_.setLim(aLimit))
  }

  def genre(aGenre: NovelGenre) = this.n(_.setGenre(aGenre))
  def bigGenre(aGenre: NovelBigGenre) = this.n(_.setBigGenre(aGenre))
  def length(aMin: Option[Int], aMax: Option[Int]) = {
    val tMin = aMin.getOrElse(0)
    val tMax = aMax.getOrElse(Int.MaxValue)

    this.n(_.setCharacterLength(tMin, tMax))
  }
}

object NarouClientBuilder {
  val init = NarouClientBuilder(identity)
}