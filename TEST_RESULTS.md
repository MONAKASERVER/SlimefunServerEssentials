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

### Paper起動スモークテスト

- Paper 26.1.2 stable build 74 / Java 25でローカルサーバーを起動
- Slimefun 4.0-UNOFFICIAL（build 2834）とMonaSlimefunServerEssentialsをロード
- Slimefun本体の555アイテム、1,515レシピを認識
- Mona JEI同期キャッシュを602〜1,089 msで生成（複数回起動）
- 最終試験値: 23ネットワークパケット、585,499 raw bytes、66,309 transferred bytes
- `Done`到達を確認し、初回試験では`stop`で正常終了

初回試験でNBT-API plugin artifactのshade警告を検出したため、配布物は正しい`item-nbt-api` artifactへ修正後に再試験しています。

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

### Paper ↔ Fabric実接続・同期試験

- オフラインモードのローカルPaper 26.1.2 build 74へFabric 26.1.2クライアントを接続
- サーバーのSlimefun 555アイテム・1,515レシピを対象にprotocol v2同期を開始
- 9 type + 2 item + 2 category + 10 display = 23パケットをSHA-256検証・gzip展開
- クライアントログの`Received every expected packet`を確認
- JEIへ555個のSlimefun ItemStackを追加し、Mona専用カテゴリ・レシピを再登録
- REI統合も同時に再読込
- 2回目の接続では永続キャッシュから復元し、全期待データの成立を確認
- 初回試験でNettyスレッド上のItemStack復元による`Rendersystem called from wrong thread`を検出。復元・キャッシュ適用をMinecraftクライアントスレッドへ移し、再試験で解消

### アドオン統合試験

- Paper 26.1.2 build 74 / Slimefun build 2834へSupreme 1.21.4、InfinityExpansion 1.21.11-2617、MonaIndustry 3.0.1を追加して起動
- 分離した試験環境でMonaIndustryを有効化するため、試験専用の最小Vault Economy providerを使用（本番用JAR・MonaIndustryソースは未変更）
- MonaIndustryの90アイテム、Wireless Energy Networkの16アイテム、6×6レシピ34件の登録を確認
- Slimefun本体と全アドオンを合わせた1,301アイテムを同期
- 9 type + 5 item + 8 category + 10 display = 32パケットをクライアントが受信し、`Received every expected packet`を確認
- JEI runtimeへ1,301個のSlimefun ItemStackを追加
- `monajei export`で`infinityexpansion.json`、`supreme.json`、`monaindustry.json`および`grid_4x4.json`〜`grid_9x9.json`を生成し、`grid_6x6.json`を含むことを確認
- 接続中の再エクスポートでキャッシュ済みパケットを二重計上する競合を検出。チャンク単位のSHA-256をクライアントスレッド上で照合するよう修正し、再同期で超過警告が出ないことを確認

開発ランチャーの未認証ユーザーによるRealms認証警告と、JEIの任意Amecsクラス不在警告は出ましたが、起動停止要因ではありません。

## 未実施

- JEI一覧、R、U、6×6画面の目視操作
- Mod未導入・Geyser/Floodgateクライアントの接続回帰
- 異なる2サーバー間を切り替える実ネットワーク試験（同一サーバー再接続は実施済み）
- 5,000 / 10,000件の処理時間、メモリ、転送時間ベンチマーク
- 破損パケットを実ネットワーク経由で注入する統合試験

したがって、ビルド成功を実サーバー動作確認済みとは扱いません。本番投入前にステージング環境で上記を実施してください。

## 既知の警告

- MavenはローカルSlimefun JARの`systemPath`利用を警告します。運用サーバーと同一APIへ固定する意図的な構成です。
- サーバーのレジストリ読み取り・シリアライズは現在メインスレッドで実行されます。毎tick処理ではなく登録完了時・管理コマンド時だけですが、大規模データでの非同期分離と計測は未完了です。
