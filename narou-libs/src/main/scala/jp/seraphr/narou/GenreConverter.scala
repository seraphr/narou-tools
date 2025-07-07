package jp.seraphr.narou

import jp.seraphr.narou.api.model.{ Genre => NewGenre }

import narou4j.enums.{ NovelGenre => OldGenre }

object GenreConverter {
  def convertToNewGenre(oldGenre: OldGenre): NewGenre = {
    oldGenre.getId match {
      case 101  => NewGenre.RomanceIsekai
      case 102  => NewGenre.RomanceReality
      case 201  => NewGenre.HighFantasy
      case 202  => NewGenre.LowFantasy
      case 301  => NewGenre.PureLiterature
      case 302  => NewGenre.HumanDrama
      case 303  => NewGenre.History
      case 304  => NewGenre.Mystery
      case 305  => NewGenre.Horror
      case 306  => NewGenre.Action
      case 307  => NewGenre.Comedy
      case 401  => NewGenre.VRGame
      case 402  => NewGenre.Space
      case 403  => NewGenre.Science
      case 404  => NewGenre.Panic
      case 9901 => NewGenre.Fairy
      case 9902 => NewGenre.Poetry
      case 9903 => NewGenre.Essay
      case 9904 => NewGenre.Replay
      case 9999 => NewGenre.OtherMisc
      case 9801 => NewGenre.NonGenreDetail
      case _    => NewGenre.OtherMisc // デフォルト
    }
  }

}
