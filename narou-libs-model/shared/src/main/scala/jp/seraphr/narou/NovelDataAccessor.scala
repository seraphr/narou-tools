package jp.seraphr.narou

import monix.eval.Task
import monix.reactive.Observable

trait NovelDataAccessor extends NovelDataReader with NovelDataWriter

/**
 * NarouNovelの文字列表現の読み込みを行う
 * 文字列化した情報の読み込み元の抽象化を行うためのレイヤー
 */
trait NovelDataReader {

  /** このreaderが読もうとしているデータが存在しているかどうかを返します */
  def exists(): Task[Boolean]

  /**
   * [[metadata]]や[[getNovel]]の引数情報を保持する情報を取得する
   * [[jp.seraphr.narou.model.ExtractedNarouNovelsMeta]]の文字列表現を想定している
   */
  val extractedMeta: Task[String]

  /**
   * aDirに存在する小説のメタデータ情報を取得する
   * [[NarouNovelsMeta]]の文字列表現を想定している
   */
  def metadata(aDir: String): Task[String]

  /**
   * aDir/aFileに存在する小説情報を取得する
   * 個別の要素は改行を含まない[[jp.seraphr.narou.model.NarouNovel]]の文字列表現を想定している
   */
  def getNovel(aDir: String, aFile: String): Observable[String]
}

trait NovelDataWriter {

  /**
   * 出力先ディレクトリが存在する場合、suffixを付加してrenameします
   * @return 作成されたバックアップのパス。バックアップが行われなかった場合None。 パスは人間可読な値でありこれを機械的な入力にしてはならない
   */
  def backup(suffix: String): Task[Option[String]]

  /**
   * [[writeMetadata]]や[[writeNovel]]の引数情報を保持する情報を保存する
   * [[jp.seraphr.narou.model.ExtractedNarouNovelsMeta]]の文字列表現を想定している
   *
   * @param aMetaString
   * @return
   */
  def writeExtractedMeta(aMetaString: String): Task[Unit]

  /**
   * aDirに説のメタデータ情報を保存する
   * [[jp.seraphr.narou.model.NarouNovelsMeta]]の文字列表現を想定している
   *
   * @param aDir
   * @param aMetaString
   * @return
   */
  def writeMetadata(aDir: String, aMetaString: String): Task[Unit]

  /**
   * aDir/aFileに小説情報を保存する
   *
   * @param aDir
   * @param aFile
   * @param aNovelString 保存するデータ 個別の要素は改行を含まない[[jp.seraphr.narou.model.NarouNovel]]の文字列表現を想定している
   * @return 書き込んだノベル数
   */
  def writeNovel(aDir: String, aFile: String, aNovelStrings: Observable[String]): Task[Int]
}
