package jp.seraphr.narou.json

import io.circe.Codec
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.Configuration
import jp.seraphr.narou.model.{ Genre, NarouNovel, NovelType, UploadType }

object NarouNovelFormats {
  implicit val customConfig: Configuration =
    Configuration.default
      .withDiscriminator("__type")

  implicit val GenreCodec: Codec[Genre] = deriveConfiguredCodec
  implicit val NovelTypeCodec: Codec[NovelType] = deriveConfiguredCodec
  implicit val UploadTypeCodec: Codec[UploadType] = deriveConfiguredCodec
  implicit val ModelCodec: Codec[NarouNovel] = deriveConfiguredCodec
}
