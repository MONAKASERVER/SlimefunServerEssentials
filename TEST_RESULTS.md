# テスト結果

実施日: 2026-10-18

## 成功

### サーバー

```powershell
mvn clean verify
```

- Java 25コンパイル成功（43 main sources）
- JUnit: 3 tests / failures 0 / errors 0
- gzipを含む複数チャンクの復元、SHA-256、最大サイズ拒否、末尾ゼロ埋めなしを検証
- 出力: `target/MonaSlimefunServerEssentials-26.1.2.jar`

### クライアント

```powershell
.\gradlew.bat clean test build --no-daemon
```

- Minecraft 26.1.2公式マッピング / Java 25コンパイル成功
- JEI 29.47.0.111依存解決成功
- JUnit: CacheIdentityTest成功
- 接続アドレスまたはサーバーUUIDが違う場合にキャッシュキーが分離されることを検証
- 出力: `build/libs/MonaSlimefunEssentials-26.1.2-fabric.jar`

### Fabric起動スモークテスト

```powershell
.\gradlew.bat runClient --no-daemon
```

- Fabric Loader 0.19.4でMinecraft 26.1.2を起動
- Fabric API、JEI 29.47.0.111、REI、Jade、Mona Slimefun Essentialsを含む60 Modを解決
- Mona Slimefun EssentialsのREI/Jadeエントリーポイントを登録
- リソース再読込、OpenGL、OpenAL、各テクスチャアトラスの生成まで例外なく到達
- 確認後に手動終了

開発ランチャーの未認証ユーザーによるRealms認証警告と、JEIの任意Amecsクラス不在警告は出ましたが、起動停止要因ではありません。

## 未実施

- Paper 26.1.2 + Slimefun4 +各アドオンのローカル起動
- FabricクライアントからPaperサーバーへの実接続
- JEI一覧、R、U、6×6画面の目視操作
- Mod未導入・Geyser/Floodgateクライアントの接続回帰
- 再接続・サーバー切替の実ネットワーク試験
- 5,000 / 10,000件の処理時間、メモリ、転送時間ベンチマーク
- 破損パケットを実ネットワーク経由で注入する統合試験

したがって、ビルド成功を実サーバー動作確認済みとは扱いません。本番投入前にステージング環境で上記を実施してください。

## 既知の警告

- MavenはローカルSlimefun JARの`systemPath`利用を警告します。運用サーバーと同一APIへ固定する意図的な構成です。
- Shade時にNBT-APIの`plugin.yml`と重複警告が出ます。完成JAR内では本プラグインの`plugin.yml`を確認します。
- サーバーのレジストリ読み取り・シリアライズは現在メインスレッドで実行されます。毎tick処理ではなく登録完了時・管理コマンド時だけですが、大規模データでの非同期分離と計測は未完了です。
