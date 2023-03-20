# narou-tools

https://seraphr.github.io/narou-tools/

## update ghsite

```
./sbt clean makeSite ghpagesPushSite
```

## preview webui（dummy data）

```
./sbt narou-webui/previewSite
```

http://localhost:4000/

## collect novel data

```
./sbt pack
```

```
$ ./narou-tools/target/pack/bin/narou collect --help
collect 0.1.0
Usage: collect [options]

なろう小説の一覧を収集し、ファイルに保存します
  -h, --help
  -o, --out <local:${path} | dropbox[:${path}]>
                           出力先ファイルパスを指定します。 省略した場合は、dropboxです。
                           dropboxのパスを省略した場合、実行日の日付のディレクトリを使用します。
  -i, --interval <value>   なろう小説APIへのアクセスインターバルをミリ秒単位で指定します。 省略した場合は、1000です
  -l, --limit <value>      取得する小説の最大数を指定します。 省略した場合は、2147483647です
  --novelsPerFile <value>  1ファイルにいくつの小説を格納するかを指定します。。 省略した場合は、10000です
  --overwrite recreate | update | fail
                           出力先ファイルが既にある場合の動作を指定します。 省略した場合は、updateです
  -a, --withAll <value>    指定した場合、全ノベルデータを出力に加えます
```

Dropboxに実行日のディレクトリを作って、新しいデータを収集＆アップロード

```
./narou-tools/target/pack/bin/narou collect --novelsPerFile 30000
```