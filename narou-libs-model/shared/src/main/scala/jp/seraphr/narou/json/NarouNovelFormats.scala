package jp.seraphr.narou.json

import java.util.Date

import jp.seraphr.narou.model.{ ExtractedNarouNovelsMeta, Genre, NarouNovel, NarouNovelsMeta, NovelType, UploadType }

import io.circe.{ Codec, HCursor, Json }
import io.circe.Decoder.Result
import io.circe.derivation.{ Configuration, ConfiguredCodec }

object NarouNovelFormats {
  implicit val customConfig: Configuration =
    Configuration.default.withDiscriminator("__type")

  implicit val GenreCodec: Codec[Genre]           = ConfiguredCodec.derived
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
