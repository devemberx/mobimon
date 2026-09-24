#!/usr/bin/env python3
"""Pack original RGBA sources losslessly. Requires Pillow; never trims or resizes."""
from pathlib import Path
import argparse
from PIL import Image


def build(source: Path, target: Path):
    paths = sorted(source.glob('mobi_idle_breath_*.png'))
    expected = [source / f'mobi_idle_breath_{i:02d}.png' for i in range(1, 25)]
    if paths != expected:
        raise ValueError('Expected exactly frames 01 through 24')
    frames = [Image.open(path) for path in paths]
    if any(frame.mode != 'RGBA' or frame.size != (1254, 1254) for frame in frames):
        raise ValueError('All original frames must be 1254 x 1254 RGBA')
    sheet = Image.new('RGBA', (1254 * 6, 1254 * 4))
    for index, frame in enumerate(frames):
        # No alpha mask: copy all RGBA channels, including transparent-pixel RGB.
        sheet.paste(frame, ((index % 6) * 1254, (index // 6) * 1254))
    target.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(target, compress_level=6)
    with Image.open(target) as result:
        for index, frame in enumerate(frames):
            x, y = (index % 6) * 1254, (index // 6) * 1254
            assert result.crop((x, y, x + 1254, y + 1254)).tobytes() == frame.tobytes()
    print(f'PASS: 24 lossless RGBA cells, 7524 x 5016: {target}')


if __name__ == '__main__':
    root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source', type=Path, default=root / 'docs/art/characters/mobi/idle_breath')
    parser.add_argument('--output', type=Path, default=root / 'core/core-ui/src/main/assets/characters/mobi/idle_breath/mobi_idle_breath_sprite.png')
    args = parser.parse_args()
    build(args.source, args.output)
