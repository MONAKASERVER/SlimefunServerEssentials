# 通信プロトコル

## チャンネル

- `slimefun_server_essentials:loading_state`
- `slimefun_server_essentials:component_types`
- `slimefun_server_essentials:items`
- `slimefun_server_essentials:recipe_categories`
- `slimefun_server_essentials:recipe_displays`
- `slimefun_server_essentials:cache_status`（client → server）

## Loading State v2

既存4件のネットワークパケット数に続き、プロトコル番号、サーバーUUID、各データセットのSHA-256マニフェストを送ります。旧サーバーの4整数だけの形式もクライアントで受理します。

## 分割エンベロープ v2

各Plugin Messageは最大32,766 bytesです。

| フィールド | 型 |
|---|---|
| magic `MSE2` | int32 |
| protocol `2` | int16 |
| flags（bit 0 = gzip） | byte |
| message id | int32 |
| chunk index | int32 |
| chunk count | int32 |
| raw length | int32 |
| encoded length | int32 |
| SHA-256（非圧縮本文） | 32 bytes |
| data | 残り |

クライアントは非圧縮64 MiB、圧縮32 MiB、4,096チャンク、同時組立64件、30秒タイムアウトを上限とします。長さ、メタデータ、gzip展開後サイズ、SHA-256が一致しない本文はデコードせず破棄します。

旧12-byte分割ヘッダーも受信互換として残しています。新サーバーからの送信はv2だけです。

## キャッシュ

クライアントは`SHA-256(接続アドレス + NUL + server-id)`ごとに本文を保存します。全ハッシュが一致したチャンネルだけを復元し、`cache_status`のビットマスクでサーバーへ通知します。サーバーは一致したチャンネルの全量送信を省略します。

差分レコード転送、再送要求、再送回数制御は未実装です。破損・欠損時はキャッシュ不一致として全量を再受信します。
