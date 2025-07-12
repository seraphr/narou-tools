package jp.seraphr.narou

import java.io.{ File, FileOutputStream, OutputStreamWriter }
import java.nio.charset.StandardCharsets

import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.{ Genre, NovelInfo, OrderType, SearchParams }
import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.narou.model.NarouNovelConverter._
import monix.eval.Task
import monix.execution.Scheduler

import io.circe.syntax._
import io.circe.generic.auto._

/**
 * 小説情報収集器
 */
object NovelCollector {
  implicit val scheduler: Scheduler = Scheduler.global

  def collect(client: NarouApiClient): Task[Iterator[NovelInfo]] = {
    val tSkips    = List(0, 500, 1000, 1500, 2000)
    val tOrders   = List(None, Some(OrderType.New), Some(OrderType.FavNovelCnt), Some(OrderType.ReviewCnt))
    val tGenres   = Genre.values.toList.filter(_ != Genre.Unselected)
    case class Setting(genre: Genre, order: Option[OrderType], skip: Int)
    
    // 1秒に1回しかアクセスさせない
    val tAdjuster = new IntervalAdjuster(1000)

    def collectOne(aSetting: Setting): Task[Vector[NovelInfo]] = {
      println(aSetting)
      tAdjuster.adjust()

      val params = SearchParams(
        genre = Seq(aSetting.genre),
        order = aSetting.order,
        st = Some(aSetting.skip),
        lim = Some(500)
      )
      
      client.search(params).map(_.novels.toVector)
    }

    def collectBySettings(aRemainSettings: List[Setting]): Task[Iterator[NovelInfo]] = {
      aRemainSettings match {
        case Nil => Task.pure(Iterator.empty)
        case h :: t =>
          collectOne(h).flatMap { tHead =>
            if (tHead.size < 500) {
              def otherGenre(s: Setting)     = h.genre != s.genre
              def sameOrderGenre(s: Setting) = h.genre == s.genre && h.order == s.order && h.skip >= s.skip
              val tNewRemain                 = t.filter(s => otherGenre(s) || sameOrderGenre(s))
              collectBySettings(tNewRemain).map(tHead.iterator ++ _)
            } else {
              collectBySettings(t).map(tHead.iterator ++ _)
            }
          }
      }
    }

    val tSettings =
      for {
        tGenre <- tGenres
        tOrder <- tOrders
        tSkip  <- tSkips
      } yield Setting(tGenre, tOrder, tSkip)

    collectBySettings(tSettings)
  }
}

object NovelCollectorMain extends App {
  import monix.execution.Scheduler.Implicits.global
  
  val tFile = new File("./novellist2")
  if (tFile.exists()) tFile.delete()

  val client = NarouApiClient().runSyncUnsafe()
  val task = NovelCollector
    .collect(client)
    .map(_.take(10000).foldLeft(Map.empty[String, NarouNovel]) { (m, n) =>
      m.updated(n.ncode, n.asScala)
    })
    .map { tNovelMap =>
      println(s"取得ノベル数 ${tNovelMap.size}")
      val tWriter = new OutputStreamWriter(new FileOutputStream(tFile), StandardCharsets.UTF_8)

      tNovelMap
        .values
        .foreach { tNovel =>
          tWriter.write(tNovel.asJson.noSpaces)
          tWriter.write("\n")
        }

      tWriter.close()
    }

  task.runSyncUnsafe()
}
