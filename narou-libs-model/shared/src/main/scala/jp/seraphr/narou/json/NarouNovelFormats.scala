package jp.seraphr.narou.json

import java.util.Date

import jp.seraphr.narou.model.{ ExtractedNarouNovelsMeta, Genre, NarouNovel, NarouNovelsMeta, NovelType, UploadType }

import io.circe.{ Codec, HCursor, Json }
import io.circe.Decoder.Result
import io.circe.derivation.{ Configuration, ConfiguredCodec }

/**
 * Json構造に保存するためのGenreの一時構造
 *
 * @param id
 * @param text ジャンルの人間可読な名前。 これを保存する意味は無い（変換時に捨てられる）が、データ構造の互換性のために存在している
 */
case class GenreForJson(id: Int, text: String) {
  def toGenre: Either[String, Genre] = Genre.fromId(id)
}

object GenreForJson {
  def fromGenre(g: Genre): GenreForJson = GenreForJson(g.id, g.text)
}

object NarouNovelFormats {
  implicit val customConfig: Configuration =
    Configuration.default.withDiscriminator("__type")

  implicit val GenreCodec: Codec[Genre]           = ConfiguredCodec.derived[GenreForJson].iemap(_.toGenre)(GenreForJson.fromGenre)
  implicit val NovelTypeCodec: Codec[NovelType]   = ConfiguredCodec.derived
  implicit val UploadTypeCodec: Codec[UploadType] = ConfiguredCodec.derived
  implicit val ModelCodec: Codec[NarouNovel]      = ConfiguredCodec.derived

  implicit val dateCodec: Codec[Date] = new Codec[Date] {
    import io.circe.syntax._
    override def apply(c: HCursor): Result[Date] = c.as[Long].map(new Date(_))

    override def apply(a: Date): Json = a.getTime.asJson
  }

  implicit val MetaCodec: Codec[NarouNovelsMeta]                   = ConfiguredCodec.derived
  implicit val ExtractedMetaCodec: Codec[ExtractedNarouNovelsMeta] = ConfiguredCodec.derived
}
