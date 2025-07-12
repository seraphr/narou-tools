# Narou API Client

なろう小説APIのScala 3クライアントライブラリです。JVM/JS両対応でcross-platform開発をサポートします。

## 特徴

- **JVM/JS両対応**: Scala.jsを使用したクロスプラットフォーム実装
- **HTTPクライアント**: STTP 3.xを使用
- **JSONデコード**: Circeを使用
- **非同期処理**: Monix Taskによる非同期実行
- **型安全**: Scala 3の型システムを活用

## 使用方法

### 基本的な使用例

```scala
import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.SearchParams
import sttp.client4._
import sttp.client4.httpclient.monix.HttpClientMonixBackend
import monix.execution.Scheduler.Implicits.global

// HTTPバックエンドを作成
val backend = HttpClientMonixBackend().runSyncUnsafe()

// APIクライアントを初期化
val client = NarouApiClient(backend)

// キーワード検索
val result = client.searchByWord("ファンタジー", limit = 10).runSyncUnsafe()
println(s"検索結果: ${result.allcount}件")

// 詳細な検索
val params = SearchParams(
  word = Some("転生"),
  biggenre = Some(1), // ハイファンタジー
  lim = Some(20)
)
val detailedResult = client.search(params).runSyncUnsafe()

// Nコードで特定の小説を取得
val novel = client.getByNcode("N1234AA").runSyncUnsafe()
```

### API パラメータ

`SearchParams`で指定可能な主要パラメータ：

- `word`: 検索キーワード
- `biggenre`: 大ジャンル（1=ハイファンタジー、2=ローファンタジーなど）
- `genre`: 詳細ジャンル
- `lim`: 取得件数（1-500、デフォルト20）
- `order`: ソート順（"hyoka"=評価順、"new"=新着順など）

## 依存関係

- Scala 3.3.6+
- STTP Client 4.x
- Circe（JSON処理）
- Monix（非同期処理）

## API仕様

このクライアントは[なろう小説API](https://dev.syosetu.com/man/api/)を使用しています。

- 1日あたり約80,000リクエストまで
- 最大転送量400MB
- データ更新に5分〜2時間の遅延の可能性

## テスト

### 単体テスト

基本的なパラメータやモデルクラスの動作を確認します：

```bash
./sbt narou-api-clientJVM/testOnly jp.seraphr.narou.api.NarouApiClientTest
```

### 統合テスト

実際のなろう小説APIにアクセスして動作を確認します：

```bash
./sbt narou-api-clientJVM/testOnly jp.seraphr.narou.api.NarouApiClientIntegrationTest
```

**注意**: 統合テストは外部APIにアクセスするため：
- 最小限のリクエスト（1件のみ取得）
- レート制限を考慮した待機時間
- APIの利用制限に配慮した設計

## ライセンス

このプロジェクトのライセンスに従います。