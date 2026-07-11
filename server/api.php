<?php
/**
 * 三角洲卡牌收集 — 后端 API（单文件，无需数据库，JSON 文件存储）
 * 支持动作：register / login / sync / board
 * 用法：把本文件与 dashboard.php 放到同一目录，App 的“服务器地址”指向本文件的完整 URL。
 */

header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, X-Token");
if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(204);
    exit;
}

define("DATA_FILE", __DIR__ . "/data/users.json");
define("TOTAL", 54);

function load(): array {
    if (!file_exists(DATA_FILE)) return ["users" => []];
    $txt = @file_get_contents(DATA_FILE);
    if ($txt === false) return ["users" => []];
    $d = json_decode($txt, true);
    return is_array($d) ? $d : ["users" => []];
}

function save(array $d): void {
    if (!is_dir(dirname(DATA_FILE))) @mkdir(dirname(DATA_FILE), 0700, true);
    $h = @fopen(DATA_FILE, "c+");
    if (!$h) { http_response_code(500); echo json_encode(["error" => "存储不可用，请检查 data/ 目录写权限"]); exit; }
    flock($h, LOCK_EX);
    ftruncate($h, 0);
    fwrite($h, json_encode($d, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
    fflush($h);
    flock($h, LOCK_UN);
    fclose($h);
}

function fail(string $msg, int $code = 400): void {
    http_response_code($code);
    echo json_encode(["error" => $msg]);
    exit;
}

function getBody(): array {
    $raw = file_get_contents("php://input");
    $j = json_decode($raw, true);
    return is_array($j) ? $j : [];
}

function getToken(): ?string {
    if (isset($_SERVER["HTTP_X_TOKEN"])) return $_SERVER["HTTP_X_TOKEN"];
    if (isset($_GET["token"])) return $_GET["token"];
    return null;
}

function genToken(): string {
    return bin2hex(random_bytes(16));
}

function userByToken(array $d, string $tok): ?string {
    foreach ($d["users"] as $un => $u) {
        if (($u["token"] ?? "") === $tok) return $un;
    }
    return null;
}

$action = $_GET["action"] ?? "";
$d = load();

switch ($action) {

    case "register":
        $b = getBody();
        $user = trim($b["user"] ?? "");
        $pass = $b["pass"] ?? "";
        if ($user === "" || $pass === "") fail("用户名和密码不能为空");
        if (mb_strlen($user) > 32) fail("用户名过长（最多 32 字）");
        if (preg_match('/[^\p{L}\p{N}_]/u', $user)) fail("用户名只能含字母、数字、下划线");
        if (isset($d["users"][$user])) fail("用户名已存在", 409);
        $tok = genToken();
        $d["users"][$user] = [
            "hash" => password_hash($pass, PASSWORD_DEFAULT),
            "token" => $tok,
            "progress" => []
        ];
        save($d);
        echo json_encode(["token" => $tok, "user" => $user, "progress" => []]);
        break;

    case "login":
        $b = getBody();
        $user = trim($b["user"] ?? "");
        $pass = $b["pass"] ?? "";
        if (!isset($d["users"][$user]) || !password_verify($pass, $d["users"][$user]["hash"])) {
            fail("用户名或密码错误", 401);
        }
        $tok = genToken();
        $d["users"][$user]["token"] = $tok;
        save($d);
        echo json_encode(["token" => $tok, "user" => $user, "progress" => $d["users"][$user]["progress"] ?? []]);
        break;

    case "sync":
        $tok = getToken();
        $user = userByToken($d, $tok);
        if ($user === null) fail("未登录或登录已失效，请重新登录", 401);
        if ($_SERVER["REQUEST_METHOD"] === "POST") {
            $b = getBody();
            $prog = $b["progress"] ?? [];
            if (!is_array($prog)) fail("progress 格式错误");
            $clean = [];
            foreach ($prog as $k) { if (is_string($k)) $clean[] = $k; }
            $d["users"][$user]["progress"] = $clean;
            save($d);
            echo json_encode(["ok" => true]);
        } else {
            echo json_encode(["progress" => $d["users"][$user]["progress"] ?? []]);
        }
        break;

    case "board":
        $rows = [];
        foreach ($d["users"] as $un => $u) {
            $p = $u["progress"] ?? [];
            $rows[] = ["user" => $un, "count" => count($p)];
        }
        usort($rows, fn($a, $b) => $b["count"] - $a["count"]);
        echo json_encode(["total" => TOTAL, "board" => $rows]);
        break;

    default:
        fail("未知操作", 404);
}
