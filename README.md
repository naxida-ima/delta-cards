# 🃏 三角洲卡牌收集（在线版）

安卓 APK + PHP 后端，用来记录「三角洲卡牌收集」任务进度，并支持**多人在线**：
- 全部 54 张扑克牌列出来，捡到一张点一下打勾，没拿的卡片一眼可见
- **账号系统**：注册 / 登录（密码用 bcrypt 存储）
- **进度云端同步**：换手机、重装都能找回自己的进度
- **全服实时进度榜**：谁收集了多少，一眼可见（App 内 + 网页版）
- **自定义服务器地址**：服务器放哪你说了算（内网 / 任意 PHP 空间）

## 目录结构
```
app/      安卓工程（Kotlin，GitHub Actions 自动构建 APK）
server/   PHP 后端（api.php + dashboard.php，无需数据库）
```

## 安卓 App
- 首次打开填「服务器地址 + 账号 + 密码」登录 / 注册
- 点卡片切换已收集 / 未收集，进度自动同步到服务器
- 顶部「全服进度」显示所有人的收集数量（自己高亮）
- 右上「刷新」拉取最新；「切换账号」退出当前账号
- 进度同时存本机，没网也能看、联网后自动同步

## 服务器部署
见 [`server/README.md`](server/README.md)：把 `api.php`、`dashboard.php` 传到任意 PHP 7.4+ 空间即可，
数据自动存为 `data/users.json`（无需数据库）。
浏览器打开 `dashboard.php` 看全服进度榜。

## 构建（GitHub Actions 自动出包）
- 推送代码到 `main` 分支自动触发构建；也可在仓库 **Actions** 页手动 **Run workflow**
- 构建完成后在 **Artifacts** 里下载 `app-release.apk`

## 安全提示
账号密码通过 HTTP 明文传输，建议在内网使用或套 HTTPS。详见 `server/README.md`。

## 自定义牌组
改 `app/src/main/java/com/example/deltacards/MainActivity.kt` 里的 `buildCards()` 换成你实际要收集的牌。
