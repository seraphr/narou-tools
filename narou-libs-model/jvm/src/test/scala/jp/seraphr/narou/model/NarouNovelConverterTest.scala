package jp.seraphr.narou.model

import jp.seraphr.narou.api.model.{ Genre => ApiGenre }

import org.scalatest.freespec.AnyFreeSpec

class NarouNovelConverterTest extends AnyFreeSpec {

  "Genre変換" - {
    "全てのAPIジャンルがドメインモデルジャンルに変換できること" in {
      // APIモデルの全てのGenre値を取得
      val allApiGenres = ApiGenre.values.toList

      // 全てのAPIジャンルについて変換が成功することを確認
      allApiGenres.foreach { apiGenre =>
        // 変換実行（例外が発生しないことを確認）
        val domainGenre = Genre.fromApiGenre(apiGenre)

        // 変換結果が適切であることを確認
        assert(domainGenre != null, s"変換結果がnullです: ${apiGenre}")

        // ログ出力（デバッグ用）
        println(s"${apiGenre.getClass.getSimpleName} (ID: ${apiGenre
            .id}) -> ${domainGenre.getClass.getSimpleName} (ID: ${domainGenre.id})")
      }
    }

    "個別のジャンル変換が正しく行われること" - {
      "Unselected ジャンルが適切に変換されること" in {
        val result = Genre.fromApiGenre(ApiGenre.Unselected)
        assert(result == Genre.Unselected, s"Unselectedの変換結果が期待値と異なります: ${result}")
        assert(result.id == 0, s"UnselectedのIDが0ではありません: ${result.id}")
      }

      "Romance系ジャンルが正しく変換されること" in {
        assert(Genre.fromApiGenre(ApiGenre.RomanceIsekai) == Genre.AnotherWorldRomance)
        assert(Genre.fromApiGenre(ApiGenre.RomanceReality) == Genre.Romance)
      }

      "Fantasy系ジャンルが正しく変換されること" in {
        assert(Genre.fromApiGenre(ApiGenre.HighFantasy) == Genre.HighFantasy)
        assert(Genre.fromApiGenre(ApiGenre.LowFantasy) == Genre.LowFantasy)
      }

      "Literature系ジャンルが正しく変換されること" in {
        assert(Genre.fromApiGenre(ApiGenre.PureLiterature) == Genre.PureLiterature)
        assert(Genre.fromApiGenre(ApiGenre.HumanDrama) == Genre.HumanDrama)
        assert(Genre.fromApiGenre(ApiGenre.History) == Genre.History)
        assert(Genre.fromApiGenre(ApiGenre.Mystery) == Genre.Detective)
        assert(Genre.fromApiGenre(ApiGenre.Horror) == Genre.Horror)
        assert(Genre.fromApiGenre(ApiGenre.Action) == Genre.Action)
        assert(Genre.fromApiGenre(ApiGenre.Comedy) == Genre.Comedy)
      }

      "SF系ジャンルが正しく変換されること" in {
        assert(Genre.fromApiGenre(ApiGenre.VRGame) == Genre.VRGame)
        assert(Genre.fromApiGenre(ApiGenre.Space) == Genre.Space)
        assert(Genre.fromApiGenre(ApiGenre.Science) == Genre.SF)
        assert(Genre.fromApiGenre(ApiGenre.Panic) == Genre.Panic)
      }

      "Other系ジャンルが正しく変換されること" in {
        assert(Genre.fromApiGenre(ApiGenre.Fairy) == Genre.FairyTale)
        assert(Genre.fromApiGenre(ApiGenre.Poetry) == Genre.Poem)
        assert(Genre.fromApiGenre(ApiGenre.Essay) == Genre.Essay)
        assert(Genre.fromApiGenre(ApiGenre.Replay) == Genre.Replay)
        assert(Genre.fromApiGenre(ApiGenre.OtherMisc) == Genre.Other)
      }

      "NonGenre系ジャンルが正しく変換されること" in {
        assert(Genre.fromApiGenre(ApiGenre.NonGenreDetail) == Genre.NonGenre)
      }
    }

    "IDの整合性が保たれること" in {
      val apiGenresToCheck = List(
        (ApiGenre.Unselected, Genre.Unselected),
        (ApiGenre.RomanceIsekai, Genre.AnotherWorldRomance),
        (ApiGenre.RomanceReality, Genre.Romance),
        (ApiGenre.HighFantasy, Genre.HighFantasy),
        (ApiGenre.LowFantasy, Genre.LowFantasy),
        (ApiGenre.PureLiterature, Genre.PureLiterature),
        (ApiGenre.HumanDrama, Genre.HumanDrama),
        (ApiGenre.History, Genre.History),
        (ApiGenre.Mystery, Genre.Detective),
        (ApiGenre.Horror, Genre.Horror),
        (ApiGenre.Action, Genre.Action),
        (ApiGenre.Comedy, Genre.Comedy),
        (ApiGenre.VRGame, Genre.VRGame),
        (ApiGenre.Space, Genre.Space),
        (ApiGenre.Science, Genre.SF),
        (ApiGenre.Panic, Genre.Panic),
        (ApiGenre.Fairy, Genre.FairyTale),
        (ApiGenre.Poetry, Genre.Poem),
        (ApiGenre.Essay, Genre.Essay),
        (ApiGenre.Replay, Genre.Replay),
        (ApiGenre.OtherMisc, Genre.Other),
        (ApiGenre.NonGenreDetail, Genre.NonGenre)
      )

      apiGenresToCheck.foreach { case (apiGenre, expectedDomainGenre) =>
        val actualDomainGenre = Genre.fromApiGenre(apiGenre)
        assert(
          actualDomainGenre == expectedDomainGenre,
          s"変換結果が期待値と異なります: ${apiGenre} -> ${actualDomainGenre}, 期待値: ${expectedDomainGenre}"
        )
        assert(apiGenre.id == actualDomainGenre.id, s"IDが一致しません: ${apiGenre.id} != ${actualDomainGenre.id}")
      }
    }
  }
}
