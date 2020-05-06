package jp.seraphr.narou.model

import narou4j.entities.Novel

object NarouNovelConverter {
  implicit class NovelOps(val n: Novel) extends AnyVal {
    private def convertNovelType(aType: Int): NovelType = aType match {
      case 1 => NovelType.ShortStory
      case 2 => NovelType.Serially
      case n => NovelType.Etc(n)
    }

    private def convertUploadType(aType: Int): UploadType = aType match {
      case 1 => UploadType.CellularPhone
      case 2 => UploadType.PC
      case 3 => UploadType.Both
      case n => UploadType.Etc(n)
    }

    def asScala: NarouNovel = {
      NarouNovel(
        n.getTitle,
        n.getNcode,
        n.getUserId,
        n.getWriter,
        n.getStory,
        Genre(n.getGenre.getId, n.getGenre.getText),
        if (n.getGensaku == 0) "" else n.getGensaku.toString,
        n.getKeyword.split(" ").toVector,
        n.getFirstUploadDate,
        n.getLastUploadDate,
        convertNovelType(n.getNovelType),
        n.getIsNovelContinue == 0,
        n.getAllNumberOfNovel,
        n.getNumberOfChar,
        n.getTime,
        n.getIsr15 == 1,
        n.getIsbl == 1,
        n.getIsgl == 1,
        n.getIszankoku == 1,
        n.getIstensei == 1,
        n.getIstenni == 1,
        convertUploadType(n.getUploadType),
        n.getGlobalPoint,
        n.getFavCount,
        n.getReviewCount,
        n.getAllPoint,
        n.getAllHyokaCount,
        n.getSasieCount,
        n.getNobelUpdatedAt,
        n.getUpdatedAt
      )
    }
  }
}
