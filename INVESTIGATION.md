# 既存コード調査レポート

## 調査対象

- `JustAHuman-xD/SlimefunServerEssentials`
- `JustAHuman-xD/SlimefunEssentials`

両方とも既存Git履歴とMIT LICENSEを保持し、`upstream` remoteを登録したフォーク上で作業しました。

## サーバー側

既存実装はSlimefunのRegistryをアドオン単位で列挙し、次の4データセットを生成していました。

1. 表示コンポーネント型
2. Slimefun ItemStack
3. レシピカテゴリーとレシピ
4. レシピ画面レイアウト

JSON出力とPlugin Messaging用バイナリ生成は同じBuilderを共有しています。レシピはRecipeTypeから入力を取得し、登録SlimefunItem自身を出力として扱います。標準で表せないカテゴリーにはExporterと表示Builderの登録点がありました。

旧通信は`slimefun_server_essentials:*`チャンネルを使い、各ペイロードを32,766 bytes以下へ12-byteヘッダーで分割していました。接続時に4種類の予定パケット数を先に送る方式でしたが、圧縮、ハッシュ、上限検証、永続キャッシュはありませんでした。

## クライアント側

既存実装は受信した4データセットから共通レシピモデルを組み立て、JEI / REI / EMI互換層へ渡していました。JEIではItemStackのSubtype InterpreterへSlimefun IDを加えることで、同じMinecraft Itemを基材とする別SFアイテムを区別します。レシピと用途の索引はJEI標準登録へ渡すため、R/U操作を再利用できます。

データ受信後のレシピビューアー再読込処理も既存実装にありましたが、接続先ごとの永続分離や破損検証はありませんでした。

## 26.1.2で必要だった変更

- Yarn系名からMojang公式マッピングへ移行
- `fabric-loom`から非難読化版`net.fabricmc.fabric-loom`へ移行
- `GuiGraphics`から`GuiGraphicsExtractor`など、26.1.2の描画APIへ更新
- Fabric Networkingのclientbound/serverbound Payload Registry名へ更新
- KeyBindingからKeyMappingへ更新
- Item Components / DataComponentPatchを現行APIへ更新
- Paper API、Java release、Fabric Loader/API、JEI/REI/Jade/Mod Menu/Cloth Configを26.1.2対応版へ更新
- `fabric.mod.json`のFabric API依存IDを`fabric-api`へ修正

## 採用した拡張方針

全面再実装は行わず、既存のBuilder、表示定義、JEI登録、Plugin Messagingを維持しました。その上で通信v2、キャッシュ、管理コマンド、可変グリッド、RecipeTypeAdapterを追加しています。

## 残る技術課題

- Bukkit APIを安全に読む同期スナップショット段階と、圧縮・シリアライズの非同期段階の完全分離
- 研究・管理者限定レシピの公開フィルター
- 汎用の複数出力、確率出力、非矩形レイアウト
- 実サーバー接続によるJEI R/Uと全アドオンの検証
- 10,000件ベンチマークと再送要求プロトコル
