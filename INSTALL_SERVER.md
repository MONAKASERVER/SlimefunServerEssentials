# サーバー導入

## 必要環境

- Paper 26.1.2
- Java 25
- 対象サーバーで使用しているSlimefun4

## 導入手順

1. サーバーを停止します。
2. `MonaSlimefunServerEssentials-26.1.2.jar`を`plugins`へ配置します。
3. Slimefun本体と対象アドオンが同じサーバーへ導入済みであることを確認します。
4. サーバーを起動し、`plugins/MonaSlimefunServerEssentials/config.yml`を確認します。
5. `/monajei status`でキャッシュの準備状態、パケット数、データ量を確認します。

Mod未導入のJavaクライアントやGeyser/Floodgate接続には同期パケットを要求しません。統合版にJEIが表示される機能ではありません。

## ソースからのビルド

このフォークは実運用中のSlimefun APIと完全一致させるため、`lib/SlimefunCore4-2834.jar`を`system`依存として参照します。第三者JARはリポジトリへ再配布しません。対象Slimefun JARを配置してからJava 25で次を実行します。

```powershell
mvn clean verify
```

出力は`target/MonaSlimefunServerEssentials-26.1.2.jar`です。

## 更新・ロールバック

更新前に既存JARと設定を退避してください。問題があればサーバーを停止し、以前のJARへ戻します。このプラグインはSlimefunの登録データやワールドを変更しません。
