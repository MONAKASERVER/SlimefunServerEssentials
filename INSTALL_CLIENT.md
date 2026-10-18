# クライアント導入

## 必要環境

- Minecraft 26.1.2
- Java 25
- Fabric Loader 0.19.4以降
- Fabric API 0.155.2+26.1.2
- JEI 29.47.0.111（26.1.2 Fabric版）
- `MonaSlimefunEssentials-26.1.2-fabric.jar`

Mod Menu、Cloth Config、Jade、REIは既存互換機能用の任意依存です。今回の完成対象はJEIです。

## 導入手順

1. Fabric 26.1.2のプロファイルを作成します。
2. 上記のModをクライアントの`mods`へ入れます。
3. MonaSlimefunServerEssentials導入済みサーバーへ接続します。
4. 同期完了トーストの後、JEIでSlimefunアイテムを検索します。
5. アイテムへカーソルを合わせ、Rで製作方法、Uで使用用途を確認します。

同期キャッシュは`config/slimefun_essentials/cache`へサーバー単位で保存されます。表示が古い場合は一度切断し、該当キャッシュを削除して再接続してください。

## ソースからのビルド

```powershell
.\gradlew.bat clean test build --no-daemon
```

出力は`build/libs/MonaSlimefunEssentials-26.1.2-fabric.jar`です。
