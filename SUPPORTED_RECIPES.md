# 対応レシピ

## 共通取得

Slimefun Registryに登録された全SlimefunItemをアドオン単位で走査し、SlimefunのRecipeType、入力、出力を既存のカテゴリー変換へ渡します。そのため、本体と通常のSlimefun APIを使うアドオンは、個別アドオン名をハードコードせず候補になります。

既存の標準表示には強化クラフト台、粉砕・精錬・圧縮・洗浄・魔法加工・電動機械などが含まれます。サーバーAPIから得られない処理時間、消費電力、確率は推測しません。

## 可変グリッド

データ形式v2はレシピごとの`width` / `height`を保持します。表示定義は3×3、4×4、5×5、6×6、7×7、8×8、9×9を生成できます。

## MonaIndustry

`MonaIndustryExpertRecipeAdapter`はMonaIndustryが導入され、公開されているExpert Recipe Registryを検出できた場合だけ次を登録します。

- カテゴリー: `mona_industry_expert_crafting`
- サイズ: 6×6（36入力）
- 触媒: `MONA_EXPERT_CRAFTER`
- 出力: レジストリで定義された実出力

対象プラグインが無い場合は何も登録せず、安全に続行します。

## 拡張API

独自設備は`RecipeTypeAdapter`を`SlimefunEssentialsRegisterEvent`で登録できます。標準APIで表せない構造だけをアダプターで共通形式へ変換してください。

## 未検証・未対応

- Supreme / Infinity Expansionを同梱した実サーバーでの全レシピ目視確認
- 複数出力・確率出力の汎用表示
- 非矩形専用レイアウトの自動推定
- 研究未解放・管理者限定レシピの送信フィルター
- CraftEngine外見（必要リソースパックがクライアントにある場合のみ描画可能）
