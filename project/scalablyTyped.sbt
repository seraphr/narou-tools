// このファイルをgithub actionsのcacheで監視することで、バージョンごと保存するキャッシュを分離する
// こうしておかないとScalablyTypedのキャッシュが無限に大きくなる
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta37")