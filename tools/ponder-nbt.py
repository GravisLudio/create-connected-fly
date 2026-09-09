#!/usr/bin/env python3
"""Read, inspect and rewrite the gzipped NBT structures under
assets/create_connected/ponder/, with no third-party dependencies.

These files came from upstream and are 1.20.1/1.21.1 vintage. Ponder does not
merely read them: PonderLevel.createBackup re-serialises every block entity in a
scene through the *current* Create Fly code every time a scene is compiled, so a
payload the new codecs cannot handle surfaces at runtime -- usually as a silent
"Serialization errors:" line in the log, once as a client crash. See PORTING.md,
"A ponder structure carried an Owner UUID".

Usage:
    python tools/ponder-nbt.py check  <dir>     round-trip every .nbt, byte-exact
    python tools/ponder-nbt.py dump   <file>    print one structure as a tree
    python tools/ponder-nbt.py blocks <dir>     list every block entity and its keys
    python tools/ponder-nbt.py legacy <dir>     flag payloads 26.2 cannot decode

`check` is the one that matters: it proves the writer reproduces the reader's
input exactly, and nothing here should be trusted to edit a structure until it
passes on every file.

Values are (code, payload) pairs so a tag's exact type survives a round-trip;
CODE maps the code back to its NBT type id.
"""

import gzip
import os
import struct
import sys

END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BARR, STR, LIST, COMP, IARR, LARR = range(13)

CODE = {'b': BYTE, 's': SHORT, 'i': INT, 'l': LONG, 'f': FLOAT, 'd': DOUBLE,
        'B': BARR, 'S': STR, 'L': LIST, 'C': COMP, 'I': IARR, 'Q': LARR}


class Reader:
    def __init__(self, buf):
        self.buf = buf
        self.i = 0

    def num(self, fmt, n):
        v = struct.unpack_from(fmt, self.buf, self.i)
        self.i += n
        return v[0]

    def u8(self):
        return self.num('>B', 1)

    def string(self):
        n = self.num('>H', 2)
        s = self.buf[self.i:self.i + n].decode('utf-8', 'replace')
        self.i += n
        return s


def read_payload(r, tag_type):
    if tag_type == BYTE:
        return ('b', r.num('>b', 1))
    if tag_type == SHORT:
        return ('s', r.num('>h', 2))
    if tag_type == INT:
        return ('i', r.num('>i', 4))
    if tag_type == LONG:
        return ('l', r.num('>q', 8))
    if tag_type == FLOAT:
        return ('f', r.num('>f', 4))
    if tag_type == DOUBLE:
        return ('d', r.num('>d', 8))
    if tag_type == BARR:
        n = r.num('>i', 4)
        v = list(r.buf[r.i:r.i + n])
        r.i += n
        return ('B', v)
    if tag_type == STR:
        return ('S', r.string())
    if tag_type == LIST:
        elem = r.u8()
        n = r.num('>i', 4)
        return ('L', (elem, [read_payload(r, elem) for _ in range(n)]))
    if tag_type == COMP:
        out = {}
        while True:
            t = r.u8()
            if t == END:
                break
            key = r.string()          # read before the payload; order matters here
            out[key] = read_payload(r, t)
        return ('C', out)
    if tag_type == IARR:
        n = r.num('>i', 4)
        return ('I', [r.num('>i', 4) for _ in range(n)])
    if tag_type == LARR:
        n = r.num('>i', 4)
        return ('Q', [r.num('>q', 8) for _ in range(n)])
    raise ValueError('unknown tag type %d at offset %d' % (tag_type, r.i))


def write_payload(out, value):
    code, x = value
    if code == 'b':
        out += struct.pack('>b', x)
    elif code == 's':
        out += struct.pack('>h', x)
    elif code == 'i':
        out += struct.pack('>i', x)
    elif code == 'l':
        out += struct.pack('>q', x)
    elif code == 'f':
        out += struct.pack('>f', x)
    elif code == 'd':
        out += struct.pack('>d', x)
    elif code == 'B':
        out += struct.pack('>i', len(x)) + bytes(x)
    elif code == 'S':
        e = x.encode('utf-8')
        out += struct.pack('>H', len(e)) + e
    elif code == 'L':
        elem, items = x
        out += struct.pack('>B', elem) + struct.pack('>i', len(items))
        for it in items:
            write_payload(out, it)
    elif code == 'C':
        for k, v in x.items():
            e = k.encode('utf-8')
            out += struct.pack('>B', CODE[v[0]]) + struct.pack('>H', len(e)) + e
            write_payload(out, v)
        out += b'\x00'
    elif code == 'I':
        out += struct.pack('>i', len(x))
        for n in x:
            out += struct.pack('>i', n)
    elif code == 'Q':
        out += struct.pack('>i', len(x))
        for n in x:
            out += struct.pack('>q', n)
    else:
        raise ValueError('unknown code %r' % code)


def serialise(name, root):
    out = bytearray()
    e = name.encode('utf-8')
    out += struct.pack('>B', CODE[root[0]]) + struct.pack('>H', len(e)) + e
    write_payload(out, root)
    return bytes(out)


def load(path):
    """-> (root name, root value). Accepts gzipped or plain NBT."""
    raw = open(path, 'rb').read()
    if raw[:2] == b'\x1f\x8b':
        raw = gzip.decompress(raw)
    r = Reader(raw)
    t = r.u8()
    return r.string(), read_payload(r, t)


def save(path, name, root):
    """Writes gzipped, which is what the structure loader expects."""
    with open(path, 'wb') as f:
        f.write(gzip.compress(serialise(name, root), 9))


def block_entities(root):
    """-> (position as [x, y, z], block entity compound) for every BE in a structure."""
    blocks = root[1].get('blocks')
    if not blocks:
        return
    for entry in blocks[1][1]:
        b = entry[1]
        if 'nbt' in b:
            pos = [c[1] for c in b['pos'][1][1]] if 'pos' in b else None
            yield pos, b['nbt'][1]


# Every rule below was established by reading Create Fly's own ponder structures,
# never by guessing: unzip its jar and compare the same block entity there.
def legacy_findings(tag):
    out = []
    if tag.get('id', ('S', ''))[1] == 'create:deployer' and 'Owner' in tag and 'OwnerName' not in tag:
        out.append('CRASH: Owner without OwnerName -- DeployerBlockEntity.write NPEs on it')
    for key in ('State', 'Mode', 'Phase'):
        v = tag.get(key)
        if v and v[0] == 'S' and v[1].isupper():
            out.append('enum %s=%r should be lowercase' % (key, v[1]))
    src = tag.get('Source')
    if src and src[0] == 'C':
        out.append('Source is a compound {X,Y,Z}; 26.2 wants a list of three ints')
    return out


def show(v, indent=0, key=None):
    code, x = v
    pad = '  ' * indent
    label = (key + ': ') if key else ''
    if code == 'C':
        print('%s%s{' % (pad, label))
        for k, vv in x.items():
            show(vv, indent + 1, k)
        print('%s}' % pad)
    elif code == 'L':
        elem, items = x
        print('%s%s[ (type %d, n=%d)' % (pad, label, elem, len(items)))
        for it in items:
            show(it, indent + 1)
        print('%s]' % pad)
    else:
        print('%s%s%s:%r' % (pad, label, code, x))


def main(argv):
    if len(argv) < 3:
        print(__doc__)
        return 2
    cmd, target = argv[1], argv[2]

    if cmd == 'dump':
        show(load(target)[1])
        return 0

    files = sorted(os.path.join(target, f) for f in os.listdir(target) if f.endswith('.nbt'))

    if cmd == 'check':
        ok = True
        for p in files:
            raw = open(p, 'rb').read()
            plain = gzip.decompress(raw) if raw[:2] == b'\x1f\x8b' else raw
            name, root = load(p)
            same = serialise(name, root) == plain
            ok &= same
            print('%-4s %s (%d bytes)' % ('OK' if same else 'DIFF', os.path.basename(p), len(plain)))
        print('\nround-trip byte-exact on all %d files: %s' % (len(files), ok))
        return 0 if ok else 1

    if cmd == 'blocks':
        for p in files:
            print('=' * 70)
            print(os.path.basename(p))
            for pos, tag in block_entities(load(p)[1]):
                print('  %-34s @ %s' % (tag.get('id', ('S', '?'))[1], pos))
                print('      %s' % ', '.join(k for k in tag if k != 'id'))
        return 0

    if cmd == 'legacy':
        found = 0
        for p in files:
            for pos, tag in block_entities(load(p)[1]):
                for msg in legacy_findings(tag):
                    found += 1
                    print('%-34s %-30s @ %s\n    %s' % (
                        os.path.basename(p), tag.get('id', ('S', '?'))[1], pos, msg))
        print('\n%d findings' % found)
        return 0

    print('unknown command %r' % cmd)
    return 2


if __name__ == '__main__':
    sys.exit(main(sys.argv))
