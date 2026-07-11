# 三角洲卡牌收集 · 后端（PHP，无需数据库）

把 `api.php` 和 `dashboard.php` 放到任意支持 PHP 7.4+ 的服务器目录即可，**不需要数据库**，数据用 JSON 文件存（自动创建 `data/users.json`）。

## 部署步骤
1. 把本目录（`server/`）里的 `api.php`、`dashboard.php` 上传到你的 PHP 空间（如 `http://你的域名/delta/`）。
2. 确保该目录**可写**（首次运行 `api.php` 会自动建 `data/` 子目录存数据）。
3. 浏览器打开 `dashboard.php` 即可看到全服进度榜（每 5 秒自动刷新）。
4. 手机 App 里「服务器地址」填 `api.php` 的完整 URL，例如：
   - `http://你的IP/delta/api.php`
   - 或直接填到目录 `http://你的IP/delta`，App 会自动补 `/api.php`

## API 说明
| 动作 | 方法 | 说明 |
|------|------|------|
| `?action=register` | POST `{user,pass}` | 注册，返回 `{token,user,progress}` |
| `?action=login` | POST `{user,pass}` | 登录，返回 `{token,user,progress}` |
| `?action=sync` | GET（带 `X-Token` 或 `?token=`） | 取自己的进度 |
| `?action=sync` | POST `{progress:[...]}` | 保存自己的进度 |
| `?action=board` | GET | 取全服进度榜（按数量排序） |

## 安全提示
- 账号密码通过 **HTTP 明文** 传输，建议在**内网**或套 **HTTPS** 后使用。
- `data/` 目录建议禁止 Web 访问，避免数据泄露：
  - Apache（2.4）：在 `data/` 下放 `.htaccess`，内容 `Require all denied`
  - Nginx：在配置里 `location ^~ /data/ { deny all; }`
- 密码用 `password_hash`（bcrypt）存储，不会存明文。

## 资源占用
极轻量：纯 JSON 接口，无框架、无数据库，单次请求内存约十几~几十 MB。0.5 核 / 200MB 内存的小服务器跑几人并发毫无压力。
