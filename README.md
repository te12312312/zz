# VirtualX - ルート不要のAndroid仮想ゲームパッド

Accessibility Service のみを使用した仮想パッドです。  
**Android Studio なし**でも GitHub Actions で APK を作れます。

## 機能

- 他のアプリの上にフローティング仮想パッド表示
- 左 / 右アナログスティック
- D-Pad（十字キー）
- A / B / X / Y ボタン
- 透明度調整・ハプティックフィードバック
- ルート権限不要（Accessibility のみ）

---

## Android Studio なしで APK を作る方法（GitHub Actions）

### 事前準備
- [GitHub](https://github.com) の無料アカウント

### 手順

#### 1. このフォルダを GitHub に上げる

1. GitHub にログイン
2. 右上 **+** → **New repository**
3. Repository name に `VirtualX` など任意の名前
4. **Public** を選択（Private でも可）
5. **Create repository** をクリック
6. **uploading an existing file** から、この ZIP を解凍した **中身** をドラッグ＆ドロップして Commit

#### 2. APK をビルドする

1. リポジトリページで **Actions** タブを開く
2. 左の **Build VirtualX APK** をクリック
3. 右の **Run workflow** → **Run workflow** を実行
4. 数分待つ（緑のチェックが付けば成功）
5. 成功したジョブを開き、下部の **Artifacts** から **VirtualX-APK** をダウンロード
6. ZIP を解凍すると `app-debug.apk` が出てきます

#### 3. スマホに入れる

1. APK をスマホに転送（メール・Googleドライブ・USB など）
2. 「提供元不明のアプリ」のインストールを許可
3. APK をタップしてインストール

#### 4. 使い方

1. アプリを開く
2. **オーバーレイ権限** を許可
3. **アクセシビリティ** で VirtualX をオンにする
4. 「仮想パッドを開始」を押す
5. ゲームを起動して操作

---

## 注意

- Accessibility 方式のため、一部ゲームでは入力がブロックされることがあります
- スティック・ボタンの座標は `GamepadOverlay.kt` で調整可能です
- デバッグ用 APK のため、Play ストア公開には別途署名が必要です

## ライセンス

個人利用・学習用に自由に使ってください。
