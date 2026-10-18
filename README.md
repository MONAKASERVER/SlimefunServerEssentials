# Mona Slimefun Essentials

Minecraft 26.1.2 / Java 25向けの、SlimefunレシピをクライアントのJEIへ同期するサーバー・クライアント統合プロジェクトです。

- サーバー: `MonaSlimefunServerEssentials-26.1.2.jar`
- Fabricクライアント: `MonaSlimefunEssentials-26.1.2-fabric.jar`
- フォーク元: [SlimefunServerEssentials](https://github.com/JustAHuman-xD/SlimefunServerEssentials) / [SlimefunEssentials](https://github.com/JustAHuman-xD/SlimefunEssentials)
- ライセンス: MIT（元の著作権表示と履歴を保持）

## 実装概要

- Slimefun Registryからアイテム、レシピカテゴリー、表示定義を取得
- Slimefun IDを表示用ItemStackへ保持し、同じベースアイテムでもJEI上で区別
- JEIのアイテム一覧、レシピ検索（R）、用途検索（U）、触媒表示を既存実装から26.1.2へ移植
- 3×3から9×9までの可変グリッド表示と`width` / `height`付きデータ形式
- MonaIndustry Expert Crafterの6×6レシピを、プラグインが存在する場合だけ反射APIで読み込むアダプター
- バージョン2通信、gzip、SHA-256、分割送信、サイズ制限、送信レート制限、破損拒否
- 接続先アドレスとサーバーUUIDを組み合わせたクライアントキャッシュ
- `/monajei`管理コマンド

## 現在の検証範囲

サーバーとクライアントのクリーンビルドおよび自動テストは成功しています。PaperサーバーとFabricクライアントを実際に接続したJEI表示試験は未実施です。このため、現段階は「配布候補」であり、本番導入済み・実機動作確認済みとは扱いません。詳細は [TEST_RESULTS.md](TEST_RESULTS.md) を参照してください。

## 文書

- [サーバー導入](INSTALL_SERVER.md)
- [クライアント導入](INSTALL_CLIENT.md)
- [設定](CONFIGURATION.md)
- [通信仕様](PROTOCOL.md)
- [対応レシピ](SUPPORTED_RECIPES.md)
- [テスト結果](TEST_RESULTS.md)
- [変更履歴](CHANGELOG.md)
- [既存コード調査](INVESTIGATION.md)

公開サーバーへ自動デプロイする処理は含めていません。
