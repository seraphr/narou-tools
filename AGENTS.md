# AGENTS.md

## プロジェクト概要

Scala 3 と SBT を利用した、JVM / Scala.js クロスプラットフォームのマルチモジュールプロジェクトです。

### モジュール構成

- `narou-api-client`: なろう API クライアント。JVM / Scala.js 共通のモデルとクライアントを提供する
- `narou-libs-model`: JVM / Scala.js で共有するドメインモデルとデータアクセス抽象を提供する
- `narou-libs`: 小説の収集・加工を行う JVM 向けのコアロジックを提供する
- `narou-tools`: コマンドラインインターフェースを提供する JVM アプリケーション
- `narou-webui`: scalajs-react、Recharts、Ant Design を利用した Scala.js 製 Web UI

依存ライブラリとそのバージョンは `build.sbt` と `project/Dependencies.scala` を正とします。

## 主要コマンド

```bash
# CLI のビルドとパッケージング
./sbt narou-tools/pack

# 全テスト
./sbt test

# モジュール単位のテスト
./sbt narou-libs-modelJS/test

# テストクラス単位の実行
./sbt "narou-libs-modelJVM/testOnly jp.seraphr.narou.model.NovelConditionParserTest"

# フォーマット
./sbt reformatAll
./sbt reformatCheck

# Web UI のローカルプレビュー（http://localhost:4000/）
./previewSite

# パッケージング後の CLI 実行例
./narou-tools/target/pack/bin/narou collect --help
./narou-tools/target/pack/bin/narou collect --novelsPerFile 30000
```

GitHub Pages への公開は、明示的に依頼された場合だけ次のコマンドで行います。

```bash
./sbt clean makeSite ghpagesPushSite
```

## アーキテクチャ

- 収集処理は CLI から `narou-api-client` を通じてなろう API を呼び出し、JSON ファイルをローカルまたは Dropbox に保存する
- 分析・表示処理はローカルまたは Dropbox のデータを読み込み、加工して Web UI に表示する
- `NovelDataReader`、`NovelDataWriter`、`NovelDataAccessor` でデータソースを抽象化し、プラットフォーム固有の実装を JVM / Scala.js の各ソースディレクトリに置く
- 非同期処理には主に Monix の `Task` と `Observable`、不変データの操作には Monocle を利用する
- CLI コマンドは `Command` trait を基底とする
- Web UI の状態は `AppState` と `StoreProvider` で管理する。ローカル開発ではダミーデータ、本番では Dropbox のデータを利用する

### 主要ドメインモデル

- `NarouNovel`: 小説のメタデータを表す中心的なドメインモデル
- `NovelCondition`: 小説を絞り込むためのドメイン固有クエリ
- `Genre`、`NovelType`、`UploadType`: ドメイン上の意味を持つ列挙型

## 実装上の制約

### クロスプラットフォーム

- 共有モデルは JVM と Scala.js の両方で動作するように実装する
- プラットフォーム固有の実装は `jvm/` または `js/` のソースディレクトリに置く
- Web UI の CSS とアセットは既存の webpack 設定に従って扱う

### 外部 API と Dropbox

- なろう API のレート制限を守り、連続アクセスには `IntervalAdjuster` を利用する
- なろう API クライアントは STTP を利用し、JVM 固有の HTML 処理には jsoup を利用する
- CLI 用 Dropbox の secret と refresh token は環境変数から読み込む
- Web UI には公開データへアクセスするための読み取り専用 Dropbox 資格情報を意図的に埋め込んでいる
- エラー処理には `Either` や `Task` を利用し、副作用を局所化する

## コーディング規約

### 命名規則

変数名には次のプレフィックスを利用します。

- `t`: 一時的・ローカル変数（例: `tConditions`、`tNovelPredicate`、`tConfig`）
- `a`: メソッド引数（例: `aArgs`、`aBuilder`、`aMinLength`、`aSkip`）
- `m`: private フィールド（例: `mLimit`、`mMaxSkip`、`mParser`）

コンビネータに渡す関数リテラルの引数は、意味上メソッド引数に相当しても `t` を付けます。

```scala
array.foreach { tElement =>
  println(tElement)
}
```

変数名には名詞句または動詞句を使います。基本的に関数名には動詞句、それ以外には名詞句を使い、名前に記号は使いません。class、trait、object のメンバーにも同じ規則を適用します。

次の場合は例外とします。

- 関数を第一級関数の値として扱う場合、その変数名に名詞句を使ってよい

  ```scala
  def search(aHumanFilter: Human => Boolean): Seq[Human]
  ```

- コレクションの添字や `for` のループ変数には `i`、`j`、`k` などの一文字名を使ってよい

  ```scala
  array.zipWithIndex.foreach((tElement, i) => println(s"${i}: ${tElement}"))
  ```

- 十分に狭いスコープでは、意味が明確な略語や先頭一文字を使ってよい

  ```scala
  conditions.foreach(c => c.apply(obj))
  numbers.reduceLeft(0)((tAcc, tNum) => tAcc + tNum)
  ```

### テスト

- ScalaTest と ScalaCheck を利用する
- テストクラスには `AsyncFreeSpec` または `AnyFreeSpec` を利用する
- テスト名は日本語で、`〇〇である時、□□をすると、～であること` のように検証する性質が分かる名前にする
- コードを追加・変更した場合は、対応するユニットテストも追加・変更する

### コメントと型注釈

- コメントは日本語で記述する
- public メンバーにはコメントを記述し、型情報だけでは分からない値にはフォーマットや単位を明記する
- public / protected メンバーには型注釈を付ける

### 設計方針

- 純粋関数と不変データ構造を優先する
- 副作用を分離・局所化する
- 型安全性を確保する

## その他

- Git のコミットメッセージは基本的に日本語で記述する
- 機能の追加や修正を行い編集が完了した場合、CIで行っているチェックが通るかどうかを必ず確認し、通らない場合は通るように修正すること
    - `./sbt reformatCheck test makeSite`