# Changelog

## 26.1.2-1.0.0 - 2026-10-18

- Paper / Minecraft / Fabric 26.1.2とJava 25へ移植
- クライアントをMojang公式マッピングとFabric API 26.1系へ更新
- JEI 29.47.0.111へ更新し、既存JEI/REI統合を維持
- 通信プロトコルv2、gzip、SHA-256、境界検証、タイムアウトを追加
- サーバー単位の永続キャッシュとハッシュ一致時の送信省略を追加
- `/monajei status|reload|sync|syncall|export|debug`を追加
- 3×3〜9×9可変グリッドとレシピ`width` / `height`を追加
- `RecipeTypeAdapter`拡張APIとMonaIndustry 6×6アダプターを追加
- 日本語UI翻訳を追加
- JSON出力を通常起動時は無効化し、明示的なexportに分離
- 元のMITライセンス、履歴、旧分割通信の受信互換を保持
