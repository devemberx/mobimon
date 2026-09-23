#!/usr/bin/env python3
"""Repack accepted artwork without resampling; register idle frames to one ground anchor. Requires Pillow/numpy."""
from pathlib import Path
import json
import math
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
COLUMNS, ROWS, LOGICAL = 6, 4, 256


def components(alpha):
    # Run-length connected components preserve silhouettes crossing the authored grid.
    parents, runs, previous = [], [], []
    def find(i):
        while parents[i] != i:
            parents[i] = parents[parents[i]]
            i = parents[i]
        return i
    for y, row in enumerate(alpha):
        edges = np.flatnonzero(np.diff(np.r_[False, row > 0, False]))
        current = []
        for start, end in zip(edges[::2], edges[1::2]):
            label = len(parents)
            parents.append(label)
            for left, right, prior in previous:
                if left > end: break
                if right >= start: parents[find(label)] = find(prior)
            current.append((int(start), int(end), label))
            runs.append((y, int(start), int(end), label))
        previous = current
    groups = {}
    for y, left, right, label in runs:
        groups.setdefault(find(label), []).append((y, left, right))
    return list(groups.values())


def unpack(path):
    pixels = np.array(Image.open(path).convert('RGBA'))
    assert pixels.shape == (1024, 1536, 4)
    groups = components(pixels[:, :, 3])
    large = [g for g in groups if sum(r-l for _, l, r in g) > 10000]
    assert len(large) == 24, f'{path}: expected 24 separate characters, found {len(large)}'
    def center(g):
        return ((min(l for _, l, _ in g)+max(r for _, _, r in g))/2,
                (min(y for y, _, _ in g)+max(y for y, _, _ in g))/2)
    centers = [center(g) for g in large]
    indices = [int(y // LOGICAL)*COLUMNS + int(x // LOGICAL) for x,y in centers]
    assert sorted(indices) == list(range(24))
    frames = [[] for _ in range(24)]
    large_ids = {id(g): i for i,g in enumerate(large)}
    for g in groups:
        owner = large_ids.get(id(g))
        if owner is None:
            x,y = center(g)
            owner = min(range(24), key=lambda i:(centers[i][0]-x)**2+(centers[i][1]-y)**2)
        frame = indices[owner]
        frames[frame].extend(g)
    bounds=[]
    for i,g in enumerate(frames):
        ox,oy=i%6*LOGICAL,i//6*LOGICAL
        bounds.append((min(l for _,l,_ in g)-ox,min(y for y,_,_ in g)-oy,
                       max(r for _,_,r in g)-ox,max(y for y,_,_ in g)+1-oy))
    return pixels,frames,bounds


def ground_anchor(frame):
    y, x = np.indices(frame.shape[:2])
    red, green, blue, alpha = [frame[:, :, i].astype(float) for i in range(4)]
    solid = alpha > 200
    ground = int(y[solid].max())
    # Wheel star is below the head/sprout. Its X coordinate identifies the ground-contact wheel.
    star = solid & (green > red * 1.2) & (blue > red * 1.2) & (green > 120)
    star &= (y > ground - 85) & (y < ground - 15)
    assert star.sum() > 100, 'Cannot identify wheel anchor'
    return int(round(x[star].mean())), ground


def align_idle(sheet, cell):
    frames = [sheet[i//6*cell:(i//6+1)*cell, i%6*cell:(i%6+1)*cell].copy() for i in range(24)]
    canonical = ground_anchor(frames[0])
    offsets = []
    for i, frame in enumerate(frames):
        anchor = ground_anchor(frame)
        dx, dy = canonical[0] - anchor[0], canonical[1] - anchor[1]
        ys, xs = np.where(frame[:, :, 3] > 0)
        assert min(xs.min()+dx, ys.min()+dy) >= 0
        assert max(xs.max()+dx, ys.max()+dy) < cell
        aligned = np.zeros_like(frame)
        aligned[ys+dy, xs+dx] = frame[ys, xs]
        assert ground_anchor(aligned) == canonical
        assert np.array_equal(aligned[ys+dy, xs+dx], frame[ys, xs])
        sheet[i//6*cell:(i//6+1)*cell, i%6*cell:(i%6+1)*cell] = aligned
        offsets.append([dx, dy])
    return {'canonicalWheelXGroundY': canonical, 'integerTranslations': offsets, 'resampled': False}


def build():
    source = ROOT/'docs/art/characters/mobi/unhealthy'
    target = ROOT/'core/core-ui/src/main/assets/characters/mobi/unhealthy'
    data={name:unpack(source/f'{name}-source.png') for name in ('transition','idle')}
    all_bounds=[b for _,_,bounds in data.values() for b in bounds]
    overflow=max(0,max(max(-x,-y,r-LOGICAL,b-LOGICAL) for x,y,r,b in all_bounds))
    padding=math.ceil((overflow+math.ceil(LOGICAL*.05))/4)*4
    cell=LOGICAL+padding*2
    report={'columns':6,'rows':4,'frames':24,'logicalCell':LOGICAL,'cell':cell,'padding':padding,'resampled':False}
    target.mkdir(parents=True,exist_ok=True)
    for name,(pixels,frames,bounds) in data.items():
        sheet=np.zeros((cell*4,cell*6,4),dtype=np.uint8)
        recovered=np.zeros_like(pixels)
        for i,g in enumerate(frames):
            ox,oy=i%6*LOGICAL,i//6*LOGICAL
            dx,dy=i%6*cell+padding,i//6*cell+padding
            for y,l,r in g:
                sheet[dy+y-oy,dx+l-ox:dx+r-ox]=pixels[y,l:r]
                recovered[y,l:r]=pixels[y,l:r]
        assert np.array_equal(recovered[:,:,3],pixels[:,:,3])
        visible=pixels[:,:,3]>0
        assert np.array_equal(recovered[visible],pixels[visible])
        alignment = align_idle(sheet, cell) if name == "idle" else None
        minimum=cell
        for i in range(24):
            frame=sheet[i//6*cell:(i//6+1)*cell,i%6*cell:(i%6+1)*cell]
            ys,xs=np.where(frame[:,:,3]>0)
            assert len(xs)>0
            margin=min(xs.min(),ys.min(),cell-1-xs.max(),cell-1-ys.max())
            assert margin >= 13, (name,i,margin)
            minimum=min(minimum,int(margin))
        output = source/f'{name}-packed-archive.png'
        Image.fromarray(sheet).save(output)
        assert np.array_equal(np.array(Image.open(output)), sheet)
        if name == 'idle':
            Image.fromarray(sheet).save(target / 'mobi_collapsed_sprite.png')
        report[name]={'sourceBounds':bounds,'minimumTransparentMargin':minimum,'visiblePixelsPreserved':True, 'alignment':alignment}
    (source/'packing.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps({k:v for k,v in report.items() if k not in data}))
    print('PASS: all 48 frames preserved; idle ground anchors aligned; mobi_collapsed_sprite.png written')

if __name__ == '__main__':
    build()
