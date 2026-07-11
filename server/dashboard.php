<?php
/**
 * 全服进度榜（网页版，自动刷新）
 * 与 api.php 放在同一目录，浏览器打开本文件即可看到所有人收集进度。
 */
?>
<!doctype html>
<html lang="zh">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>三角洲卡牌收集 · 全服进度</title>
<style>
  body{font-family:system-ui,-apple-system,"Segoe UI",Roboto,sans-serif;background:#0F172A;color:#E2E8F0;margin:0;padding:20px}
  h1{font-size:20px;margin:0 0 4px}
  .sub{color:#94A3B8;font-size:13px;margin-bottom:16px}
  .row{display:flex;align-items:center;gap:12px;margin:10px 0}
  .name{width:130px;font-weight:600;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
  .bar{flex:1;height:18px;background:#1E293B;border-radius:9px;overflow:hidden}
  .fill{height:100%;background:linear-gradient(90deg,#4F46E5,#7C3AED);border-radius:9px;transition:width .4s}
  .cnt{width:74px;text-align:right;font-variant-numeric:tabular-nums}
</style>
</head>
<body>
<h1>🃏 三角洲卡牌收集 · 全服进度</h1>
<div class="sub">每 5 秒自动刷新</div>
<div id="board"></div>
<script>
async function load(){
  try{
    const r = await fetch('api.php?action=board');
    const j = await r.json();
    const total = j.total || 54;
    const el = document.getElementById('board');
    if(!j.board || !j.board.length){ el.innerHTML = '<p style="color:#94A3B8">还没有人开始收集</p>'; return; }
    el.innerHTML = j.board.map(u=>{
      const pct = Math.round(u.count/total*100);
      const bar = '<div class="bar"><div class="fill" style="width:'+pct+'%"></div></div>';
      return '<div class="row"><div class="name">'+esc(u.user)+'</div>'+bar+'<div class="cnt">'+u.count+'/'+total+'</div></div>';
    }).join('');
  }catch(e){ document.getElementById('board').innerHTML='<p style="color:#F87171">加载失败，请确认 api.php 可访问</p>'; }
}
function esc(s){return (s+'').replace(/[&<>]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;'}[c]));}
load(); setInterval(load, 5000);
</script>
</body></html>
