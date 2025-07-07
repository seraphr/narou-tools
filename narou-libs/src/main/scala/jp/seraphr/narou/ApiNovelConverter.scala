package jp.seraphr.narou

import jp.seraphr.narou.api.model.{ NovelInfo, NovelType => ApiNovelType }
import jp.seraphr.narou.model.{ Genre, NarouNovel, NovelType, UploadType }

object ApiNovelConverter {
  implicit class NovelInfoOps(val n: NovelInfo) extends AnyVal {
    private def convertNovelType(aType: ApiNovelType): NovelType = aType match {
      case ApiNovelType.Short  => NovelType.ShortStory
      case ApiNovelType.Serial => NovelType.Serially
    }

    def asScala: NarouNovel = {
      NarouNovel(
        n.title,
        n.ncode,
        n.userid.toString,
        n.writer,
        n.story,
        Genre.fromId(n.genre.id).fold(s => throw new RuntimeException(s), identity),
        n.gensaku,
        n.keyword.split(" ").toVector,
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
        UploadType.PC, // APIからは取得できないためデフォルト値
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

  }
}
