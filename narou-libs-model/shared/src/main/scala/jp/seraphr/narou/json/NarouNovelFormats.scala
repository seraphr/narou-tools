package jp.seraphr.narou.json

import java.util.Date

import io.circe.Decoder.Result
import io.circe.{ Codec, HCursor, Json }
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.Configuration
import jp.seraphr.narou.model.{ Genre, MultiNarouNovelsMeta, NarouNovel, NarouNovelsMeta, NovelType, UploadType }

object NarouNovelFormats {
  implicit val customConfig: Configuration =
    Configuration.default
      .withDiscriminator("__type")

  implicit val GenreCodec: Codec[Genre] = deriveConfiguredCodec
  implicit val NovelTypeCodec: Codec[NovelType] = deriveConfiguredCodec
  implicit val UploadTypeCodec: Codec[UploadType] = deriveConfiguredCodec
  implicit val ModelCodec: Codec[NarouNovel] = deriveConfiguredCodec

  implicit val dateCodec: Codec[Date] = new Codec[Date] {
    import io.circe.syntax._
    override def apply(c: HCursor): Result[Date] = c.as[Long].map(new Date(_))

    override def apply(a: Date): Json = a.getTime.asJson
  }
  implicit val MetaCodec: Codec[NarouNovelsMeta] = deriveConfiguredCodec
  implicit val MultiMetaCodec: Codec[MultiNarouNovelsMeta] = deriveConfiguredCodec
}
