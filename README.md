# 🃏 三角洲卡牌收集清单

安卓 APK，用来记录「三角洲卡牌收集」任务进度：全部 54 张扑克牌列出来，捡到一张就点一下打勾，没拿的卡片一眼可见。

## 功能
- 完整 54 张牌（♠♥♣♦ 各 13 张 + 大王小王）
- 点一下卡片切换「已收集 / 未收集」，已收集显示 ✓ 并变绿
- 顶部实时显示：已收集 X / 54，未收集 Y
- 进度自动保存在本机（SharedPreferences），关掉再开还在
- 「清空重置」按钮一键归零

## 构建（GitHub Actions 自动出包）
- 推送代码到 `main` 分支会自动触发构建
- 也可在仓库 **Actions** 页手动 **Run workflow**
- 构建完成后在 **Artifacts** 里下载 `app-release.apk`

## 自定义牌组
改 `app/src/main/java/com/example/deltacards/MainActivity.kt` 里的 `buildCards()` 即可换成你实际要收集的牌。
