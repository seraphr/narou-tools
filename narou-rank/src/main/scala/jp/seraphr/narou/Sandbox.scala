package jp.seraphr.narou

import java.io.{ BufferedWriter, File, FileOutputStream, OutputStreamWriter }
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.{ Date, Locale }

/**
 */
object Sandbox extends App {
  val tRankGenerator = new NarouRankGenerator()
  val tFavList       = List(100, 500, 1000)
  val tSettingsList  = tFavList.map { tFav =>
    NarouRankSettings()
      .limit(100)
      //      .n(_.setPickup(true))
      .n(_.setCharacterLength(100000, Int.MaxValue)).n(_.setSearchWord("溺愛")).addFilter(tFav <= _.getFavCount)
    //      .addFilter(_.getIsNovelContinue == 1)
  }

  val tFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.JAPAN)

  (tSettingsList zip tFavList).foreach { case (tSettings, tFav) =>
    println(s"start fav${tFav}")
    val tRankResult = tRankGenerator.generateRank(tSettings)
    val tResultFile = new File(s"./fav${tFav}.txt")
    if (tResultFile.exists()) tResultFile.delete()
    val tWriter     = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tResultFile), StandardCharsets.UTF_8))
    try {
      tWriter.write(s"${tFormat.format(new Date())}\n")
      tWriter.write(s"ブックマーク数${tFav}以上\n")
      //        tWriter.write(s"連載中\n")
      tWriter.write(s"文字数10万文字以上\n\n")

      tWriter.write(s"all novel count = ${tRankResult.allNovelCount}\n")
      tWriter.write(s"unique novel count = ${tRankResult.uniqueNovelCount}\n")
      tWriter.write(s"filtered novel count = ${tRankResult.filteredCount}\n")
      tWriter.write("\n")

      tWriter.write("順位\tURL\t文字数\tポイント評価\tブックマーク数\t(ポイント評価 / ブックマーク数)\ttitle\tkeyword\n")

      tRankResult
        .novels
        .zipWithIndex
        .foreach { case (NovelAndRate(tNovel, tRate), tIndex) =>
          val tUrl      = s"http://ncode.syosetu.com/${tNovel.getNcode}/"
          val tKeywords = s"[${tNovel.getKeyword}]"
          val tRateStr  = "%.4f".format(tRate)
          val tLine     =
            s"${tIndex + 1}\t${tUrl}\t${tNovel.getNumberOfChar}\t${tNovel.getAllPoint}\t${tNovel.getFavCount}\t${tRateStr}\t${tNovel.getTitle.replaceAll("[\r\n]", "")}\t${tKeywords}\n"

          tWriter.write(tLine)
        }

    } finally {
      tWriter.close()
    }
  }
}
