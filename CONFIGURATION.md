# 設定

## サーバー `config.yml`

| キー | 初期値 | 内容 |
|---|---:|---|
| `sync.enabled` | `true` | 同期機能全体。`false`では登録済みクライアントにも送信しません |
| `sync.automatic` | `true` | キャッシュ再構築後、オンラインプレイヤーへ自動同期 |
| `sync.compression` | `true` | 1 KiB以上でサイズが減る場合にgzipを使用 |
| `sync.packets-per-tick` | `4` | 1プレイヤー・1チャンネルあたりの最大送信数/tick |
| `sync.max-uncompressed-bytes` | `67108864` | 1論理メッセージの非圧縮上限 |
| `cache.enabled` | `true` | クライアントからのキャッシュ一致通知を使用 |
| `server-id` | 自動生成 | キャッシュ名前空間。通常は変更しません |
| `export-json-on-load` | `false` | 起動時JSON出力。通常運用では無効推奨 |
| `publish-locked-research` | `true` | 将来の研究公開フィルター用。現版ではまだ強制されません |
| `debug` | `false` | 同期スキップ理由などの追加ログ |

`server-id`だけではキャッシュを信用せず、クライアントは実際の接続アドレスもキーへ含めます。

## クライアント `slimefun_essentials.json`

| キー | 初期値 | 内容 |
|---|---:|---|
| `block_features` | `true` | Slimefunブロック表示機能 |
| `recipe_features` | `true` | JEI/REIレシピ連携 |
| `replace_item_identifiers` | `true` | Minecraft IDをSlimefun ID表示へ置換 |
| `hide_background_tooltips` | `true` | 装飾用アイテムのツールチップを非表示 |

クライアントキャッシュは常時ハッシュ検証されます。キャッシュ無効化UI、アドオン別フィルター、未知レシピポリシーは未実装です。

## コマンドと権限

| コマンド | 権限 | 内容 |
|---|---|---|
| `/monajei status` | `monajei.status` | 状態・構築時間・転送量 |
| `/monajei reload` | `monajei.reload` | 設定再読込とキャッシュ再構築 |
| `/monajei sync <player>` | `monajei.sync` | 指定プレイヤーを再同期 |
| `/monajei syncall` | `monajei.syncall` | 全オンラインプレイヤーを再同期 |
| `/monajei export` | `monajei.export` | `generated`へJSON出力 |
| `/monajei debug` | `monajei.debug` | デバッグログ切替 |

全権限の初期値はOPです。
