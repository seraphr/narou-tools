# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System and Commands

This is a multi-module Scala 3 project using SBT with cross-platform compilation (JVM/JS).

### Essential Commands

```bash
# Build and package CLI tools
./sbt narou-tools/pack

# Run tests
./sbt test

# Run tests for specific module
./sbt narou-libs-modelJS/test

# Run tests for specific file in specific module
./sbt "narou-libs-modelJVM/testOnly jp.seraphr.narou.model.NovelConditionParserTest"

# Code formatting
./sbt reformatAll          # Format all code
./sbt reformatCheck        # Check formatting

# Web UI development
./sbt narou-webui/previewSite    # Preview at http://localhost:4000/
./sbt clean makeSite ghpagesPushSite  # Build and deploy to GitHub Pages

# CLI usage after packaging
./narou-tools/target/pack/bin/narou collect --help
./narou-tools/target/pack/bin/narou collect --novelsPerFile 30000
```

## Project Architecture

### Module Structure

- **`narou-libs-model`** - Cross-platform (JVM/JS) shared models and data access abstractions
- **`narou-libs`** - JVM-only core business logic for novel collection and processing
- **`narou-tools`** - CLI application with command-line interface
- **`narou-webui`** - React-based web UI built with Scala.js
- **`narou-rank`** - Ranking and analysis utilities

### Key Dependencies

- **Cross-platform**: Circe (JSON), Monix (reactive), Monocle (optics), ScalaTest
- **JVM-only**: narou4j (API client), Dropbox SDK, Apache Commons IO, Logback
- **JS-only**: ScalaJS React, Recharts, Ant Design, Dropbox JS SDK

### Data Flow Architecture

1. **Collection Pipeline**: CLI → Narou API → JSON files → Dropbox storage
2. **Analysis Pipeline**: Dropbox/Local data → Processing → Web UI visualization
3. **Cross-platform abstractions**: `NovelDataAccessor` trait with platform-specific implementations

### Core Domain Models

- **`NarouNovel`** - Main domain object representing a web novel with metadata
- **`NovelCondition`** - Domain-specific query language for filtering novels
- **`Genre`**, **`NovelType`**, **`UploadType`** - Enumerated types with semantic meaning
- **Platform abstraction**: `NovelDataReader` trait for different data sources (Ajax, File, Dropbox)

### Key Patterns

- **Functional Reactive Programming**: Monix `Task` and `Observable` for async operations
- **Lens-based data manipulation**: Monocle for immutable data transformations
- **Command Pattern**: CLI commands extend base `Command` trait
- **Cross-platform abstractions**: Shared traits with JVM/JS-specific implementations

## Testing

### Tests use ScalaTest with ScalaCheck property-based testing. Key test file:

- `NovelConditionParserTest.scala` - Comprehensive parser tests with property-based testing

### その他

- テストには`AsyncFreeSpec` / `AnyFreeSpec`を利用する

## Development Notes

### Cross-Platform Considerations

- Model classes must work on both JVM and JS platforms
- Platform-specific implementations in separate source folders (`jvm/`, `js/`)
- Webpack configuration for CSS/asset handling in web UI

### API Integration

- Narou API has rate limiting - use `IntervalAdjuster` for throttling
- Dropbox integration uses embedded read-only credentials for public data access
- Error handling with `Either` and `Task` monads

### Web UI Development

- React components built with `scalajs-react`
- State management using custom `AppState` with `StoreProvider`
- Visualization with Recharts for scatter plots and data analysis
- Local development uses dummy data, production connects to Dropbox

## コーディング規約

### 命名規則

#### 変数名プレフィックス

- t - 一時的・ローカル変数：tConditions, tNovelPredicate, tConfig
- a - メソッド引数：aArgs, aBuilder, aMinLength, aSkip
- m - プライベートフィールド：mLimit, mMaxSkip, mParser

ただし、意味的に引数でも

#### 変数名

変数名には名詞句もしくは動詞句を利用する。
基本的には関数名には動詞句を利用し、それ以外には名詞句を利用する。
名前に記号は利用しない。

class、trait、object のメンバーも同様である。

##### 例外

###### 関数が、第一級関数の値として持ち運ばれるとき、その変数を名詞句としても良い

```scala
def search(aHumanFilter: Human => boolean): Seq[Human]
```

###### コレクションの添字や for のループ変数に`i`, `j`, `k`などの１文字の変数を利用しても良い

```scala
array.zipWithIndex.foreach((tElement, i) => println(`${i}: ${element}`))
```

###### 十分にスコープが小さい時、その適切な名前の略語や先頭 1 文字の変数を使っても良い

```scala
conditions.foreach(c => c.apply(object))
numbers.reduceLeft(0)((tAcc, tNum) => tAcc + tNum)
```

### テスト

- テスト名には日本語を使用する
- テスト名は`〇〇である時、□□をすると、～であること`など、どういう性質をテストしているのかが分かりやすい名前にすること
- コードを追加・修正するときは、それに対応するユニットテストを常に追加・修正する

### コメント

- コメントは日本語で記述する
- public なメンバーには必ずコメントを記述する
  - 特に、型情報ではわからない値は、フォーマットや単位を明記する。

### 関数型プログラミングを重視する

- 純粋関数を優先
- 基本的に不変データ構造を利用・定義する
- 副作用の分離と局所化を行う
- 型安全性を確保する

### 型アノテーション

- public / protected なメンバーには、必ず型アノテーションを書く

## その他

- git のコミットコメントは基本的に日本語で記述する
