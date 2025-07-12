package jp.seraphr.narou.model

import jp.seraphr.narou.api.model.NovelInfo

object NarouNovelConverter {
  implicit class NovelInfoOps(val n: NovelInfo) extends AnyVal {
    private def convertUploadType(): UploadType = {
      // narou-api-clientでは投稿手段情報がないため、PCを仮定
      UploadType.PC
    }

    def asScala: NarouNovel = {
      NarouNovel(
        n.title,
        n.ncode,
        n.userid.toString,
        n.writer,
        n.story,
        Genre.fromApiGenre(n.genre),
        n.gensaku,
        n.keyword.split(" ").toVector.filter(_.nonEmpty),
        n.general_firstup,
        n.general_lastup,
        convertNovelType(n.novel_type),
        n.end,
        n.general_all_no,
        n.length,
        n.time,
        n.isr15,
        n.isbl,
        n.isgl,
        n.iszankoku,
        n.istensei,
        n.istenni,
        convertUploadType(),
        n.global_point,
        n.fav_novel_cnt,
        n.review_cnt,
        n.all_point,
        n.all_hyoka_cnt,
        n.sasie_cnt,
        n.novelupdated_at,
        n.updated_at
      )
    }

    private def convertNovelType(aType: jp.seraphr.narou.api.model.NovelType): NovelType = aType match {
      case jp.seraphr.narou.api.model.NovelType.Short  => NovelType.ShortStory
      case jp.seraphr.narou.api.model.NovelType.Serial => NovelType.Serially
    }

  }
}
