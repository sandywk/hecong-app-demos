#!/usr/bin/env python3
"""把 lucide 图标源码转成 Android VectorDrawable(app/src/main/res/drawable/ic_*.xml)。

为什么用生成而不是手抄路径(.claude/rules/architecture.md §3.0.1「能推导的绝不登记」):
设计稿(design/scripts/build-app-demo.py)用的就是 lucide,手抄 path 必然跟稿漂移。
要加图标:把名字加进 ICONS 再跑 `python3 tools/gen-icons.py`。

lucide 是描边图标(viewBox 24 / stroke-width 2 / round cap+join),所以生成的 vector
用 strokeColor + strokeWidth,颜色一律由调用方 imageTintList 决定(res 里写占位黑;不写 android:tint —— 那是 API 24+)。
"""
import math
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3].parent  # 仓库根
SRC = ROOT / 'apps/studio/node_modules/lucide-react/dist/esm/icons'
OUT = Path(__file__).resolve().parents[1] / 'app/src/main/res/drawable'

ICONS = [
    'chevron-left', 'chevron-right', 'x', 'ellipsis',
    'package', 'map-pin', 'settings', 'message-circle',
    'layout-grid', 'list',
]


def arc(cx, cy, r):
    """圆 → 两段 A 弧(VectorDrawable 支持 A 指令)。"""
    return (f'M{cx - r},{cy} A{r},{r} 0 1 0 {cx + r},{cy} '
            f'A{r},{r} 0 1 0 {cx - r},{cy}')


def rounded_rect(x, y, w, h, rx):
    if rx <= 0:
        return f'M{x},{y} h{w} v{h} h{-w} Z'
    return (f'M{x + rx},{y} h{w - 2 * rx} a{rx},{rx} 0 0 1 {rx},{rx} '
            f'v{h - 2 * rx} a{rx},{rx} 0 0 1 {-rx},{rx} h{-(w - 2 * rx)} '
            f'a{rx},{rx} 0 0 1 {-rx},{-rx} v{-(h - 2 * rx)} a{rx},{rx} 0 0 1 {rx},{-rx} Z')


def num(v):
    return float(v)


def parse(js):
    """从 lucide 的 __iconNode 数组里抽出 [(tag, attrs)],转成 path d 列表。"""
    body = js.split('__iconNode = ', 1)[1].split('\nconst ', 1)[0]
    entries = re.findall(r'\[\s*"(\w+)",\s*\{(.*?)\}\s*\]', body, re.S)
    paths = []
    for tag, attrs in entries:
        a = dict(re.findall(r'(\w+):\s*"([^"]*)"', attrs))
        if tag == 'path':
            paths.append(a['d'])
        elif tag == 'circle':
            paths.append(arc(num(a['cx']), num(a['cy']), num(a['r'])))
        elif tag == 'rect':
            paths.append(rounded_rect(num(a['x']), num(a['y']), num(a['width']),
                                      num(a['height']), num(a.get('rx', 0))))
        elif tag == 'line':
            paths.append(f"M{a['x1']},{a['y1']} L{a['x2']},{a['y2']}")
        elif tag == 'polyline':
            pts = a['points'].replace(',', ' ').split()
            pairs = [f'{pts[i]},{pts[i + 1]}' for i in range(0, len(pts), 2)]
            paths.append('M' + ' L'.join(pairs))
        else:
            raise SystemExit(f'未支持的 lucide 图元:{tag}')
    return paths


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for name in ICONS:
        js = (SRC / f'{name}.js').read_text(encoding='utf-8')
        paths = parse(js)
        lines = ['<?xml version="1.0" encoding="utf-8"?>',
                 '<!-- 由 tools/gen-icons.py 从 lucide 源码生成,勿手改 -->',
                 '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
                 '    android:width="24dp" android:height="24dp"',
                 '    android:viewportWidth="24" android:viewportHeight="24">']
        for d in paths:
            lines += [f'  <path android:pathData="{d}"',
                      '      android:strokeColor="#000000" android:strokeWidth="2"',
                      '      android:strokeLineCap="round" android:strokeLineJoin="round" />']
        lines.append('</vector>')
        (OUT / f'ic_{name.replace("-", "_")}.xml').write_text('\n'.join(lines) + '\n',
                                                              encoding='utf-8')
        print('ic_' + name.replace('-', '_') + '.xml')


if __name__ == '__main__':
    sys.exit(main())
