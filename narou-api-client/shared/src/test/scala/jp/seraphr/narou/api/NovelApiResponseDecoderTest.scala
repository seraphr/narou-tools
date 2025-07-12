package jp.seraphr.narou.api

import jp.seraphr.narou.api.model.{ BigGenre, Genre, NovelApiResponse, NovelType }
import jp.seraphr.narou.api.model.given

import io.circe.parser._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class NovelApiResponseDecoderTest extends AnyFreeSpec with Matchers {

  "NovelApiResponseのJSONデコード" - {

    "実際のAPIレスポンスをデコードできること" in {
      // https://api.syosetu.com/novelapi/api/?out=json&lim=10 からの実際のレスポンス( 2025/07/07 )
      val tApiResponseJson =
        """[{"allcount":1103504},{"title":"昂燃機⪕ダルイダー_(:eD∠.","ncode":"N6866JA","userid":1014960,"writer":"gaction9969","story":"地球の皆さんこんにちはッ！　アストラの皆さんこんにちはッ！　不々惑なるこのワイがッ！　此度も不要不急不毛の界隈を燃やし尽くすのやでぇッ！！\n\n更年の、更年による、更年のためのワーカホリックロボテ","biggenre":2,"genre":201,"gensaku":"","keyword":"ロボット ガンダム 逆行転生 残酷な描写 アンチヘイト TS 男の娘 アンドロイド サイボーグ 人造人間","general_firstup":"2024-12-29 02:14:20","general_lastup":"2025-01-06 23:33:19","novel_type":2,"end":0,"general_all_no":10,"length":119652,"time":598,"isstop":0,"isr15":0,"isbl":0,"isgl":0,"iszankoku":1,"istensei":1,"istenni":0,"global_point":0,"daily_point":0,"weekly_point":0,"monthly_point":0,"quarter_point":0,"yearly_point":0,"fav_novel_cnt":0,"impression_cnt":0,"review_cnt":0,"all_point":0,"all_hyoka_cnt":0,"sasie_cnt":0,"kaiwaritu":53,"novelupdated_at":"2025-01-06 23:33:19","updated_at":"2025-01-07 01:46:36"},{"title":"魔法少女リリカルなのは　～恋風伝～","ncode":"N5533JA","userid":1015116,"writer":"千羽　優一","story":"　恋風を名乗る元魔法少女が再び立ち上がる。愛のために。\n　恋風ユイナは、十数年前に引退した元魔法少女である。\n　彼女は昔、闇の書事件にて、リインフォースⅡと一緒に闇の書の暴走を止めた功労者の一人でもあった。\n　時が経ち、三十歳になった彼女に再び危機が迫る。\n　愛する家族のために、再び恋風として立ち上がる元魔法少女の物語。","biggenre":2,"genre":202,"gensaku":"魔法少女リリカルなのは","keyword":"魔法少女リリカルなのは 二次創作 恋風 ユイナ オリジナル魔法少女 愛 家族 感動 戦闘 友情 成長","general_firstup":"2024-12-29 11:31:00","general_lastup":"2025-01-06 21:00:00","novel_type":2,"end":0,"general_all_no":9,"length":77508,"time":387,"isstop":0,"isr15":0,"isbl":0,"isgl":0,"iszankoku":0,"istensei":0,"istenni":0,"global_point":3,"daily_point":0,"weekly_point":0,"monthly_point":0,"quarter_point":3,"yearly_point":3,"fav_novel_cnt":1,"impression_cnt":0,"review_cnt":0,"all_point":0,"all_hyoka_cnt":0,"sasie_cnt":0,"kaiwaritu":55,"novelupdated_at":"2025-01-06 21:00:00","updated_at":"2025-01-07 01:46:36"}]"""

      val tResult = decode[NovelApiResponse](tApiResponseJson)

      tResult.isRight should be(true)
      val tResponse = tResult.getOrElse(fail("JSONのデコードに失敗しました"))

      // レスポンス構造の検証
      tResponse.allcount should be(1103504)
      tResponse.novels should have size 2

      // 1件目のデータ検証
      val tFirstNovel = tResponse.novels.head
      tFirstNovel.title should be("昂燃機⪕ダルイダー_(:eD∠.")
      tFirstNovel.ncode should be("N6866JA")
      tFirstNovel.userid should be(1014960)
      tFirstNovel.writer should be("gaction9969")
      tFirstNovel.biggenre should be(BigGenre.Fantasy)
      tFirstNovel.genre should be(Genre.HighFantasy)
      tFirstNovel.novel_type should be(NovelType.Serial)
      tFirstNovel.end should be(false)
      tFirstNovel.length should be(119652)
      tFirstNovel.iszankoku should be(true)
      tFirstNovel.istensei should be(true)

      // 2件目のデータ検証
      val tSecondNovel = tResponse.novels(1)
      tSecondNovel.title should be("魔法少女リリカルなのは　～恋風伝～")
      tSecondNovel.ncode should be("N5533JA")
      tSecondNovel.userid should be(1015116)
      tSecondNovel.writer should be("千羽　優一")
      tSecondNovel.biggenre should be(BigGenre.Fantasy)
      tSecondNovel.genre should be(Genre.LowFantasy)
      tSecondNovel.gensaku should be("魔法少女リリカルなのは")
      tSecondNovel.global_point should be(3)
      tSecondNovel.fav_novel_cnt should be(1)
    }

    "空のレスポンスをデコードできること" in {
      val tEmptyResponseJson = """[{"allcount":0}]"""

      val tResult = decode[NovelApiResponse](tEmptyResponseJson)

      tResult.isRight should be(true)
      val tResponse = tResult.getOrElse(fail("JSONのデコードに失敗しました"))

      tResponse.allcount should be(0)
      tResponse.novels should be(empty)
    }

    "不正なJSONでデコードが失敗すること" in {
      val tInvalidJson = """{"invalid": "json"}"""

      val tResult = decode[NovelApiResponse](tInvalidJson)

      tResult.isLeft should be(true)
    }

    "allcountのみのJSONでも処理できること" in {
      val tOnlyAllcountJson = """[{"allcount":12345}]"""

      val tResult = decode[NovelApiResponse](tOnlyAllcountJson)

      tResult.isRight should be(true)
      val tResponse = tResult.getOrElse(fail("JSONのデコードに失敗しました"))

      tResponse.allcount should be(12345)
      tResponse.novels should be(empty)
    }
  }
}
