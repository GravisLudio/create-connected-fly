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
    # Match on the shape, not the key name. `Source` is a BlockPos on the kinetic
    # block entities and an unrelated {Label, Id} on create:display_link, and a
    # name-only check reports that second one as broken when it is correct.
    for key, value in tag.items():
        if _is_xyz_compound(value):
            out.append('%s is a compound {X,Y,Z}; 26.2 wants a list of three ints' % key)
    return out


# --- migration ---------------------------------------------------------------
# Each rule below is grounded in a real sample from Create Fly's own ponder
# structures, found by unzipping its jar and reading the same block entity. None
# of them is inferred from what a codec "probably" wants, and each one refuses
# rather than guesses when it meets input it was not shown. Rules that would have
# to invent a shape are deliberately absent -- see PORTING.md for those.

# 3D data values, which is the order Direction.values() has always had.
DIRECTIONS = ['down', 'up', 'north', 'south', 'west', 'east']

# (block entity id, key) whose value is an enum constant. 26.2 serialises these
# lowercase; the committed files still hold the SCREAMING_CASE that Java's
# name() produced. Reference: blaze_burner.nbt has State='waiting', Mode='use',
# Phase='search_inputs'; basin.nbt has Casing='none'.
ENUM_KEYS = {
    ('create:belt', 'Casing'),
    ('create:deployer', 'State'),
    ('create:deployer', 'Mode'),
    ('create:mechanical_arm', 'Phase'),
}

# Inventories that used to be a compound wrapping an Items list plus some
# processing bookkeeping, and are a bare list now. Reference: depot.nbt and
# item_vault entries, both ('L', (0, [])). The extra keys are the ones we are
# willing to drop -- anything else and the rule refuses.
FLATTEN_INVENTORY = {
    'create:saw': {'AppliedRecipe', 'ProcessingTime', 'RecipeTime', 'Size'},
    'create:item_vault': {'Size'},
}


def _blockpos_list(x, y, z):
    return ('L', (INT, [('i', x), ('i', y), ('i', z)]))


def _is_xyz_compound(value):
    """A BlockPos written the old way. Shape-guarded on purpose: `Source` also
    names an unrelated compound on create:display_link ({Label, Id}), and that
    one must not be touched."""
    if value[0] != 'C':
        return False
    d = value[1]
    return set(d) == {'X', 'Y', 'Z'} and all(v[0] == 'i' for v in d.values())


def _unpack_block_pos(packed):
    """BlockPos.asLong packing: 26 bits x, 26 bits z, 12 bits y."""
    def signed(v, bits):
        v &= (1 << bits) - 1
        return v - (1 << bits) if v >> (bits - 1) else v
    return signed(packed >> 38, 26), signed(packed, 12), signed(packed >> 12, 26)


def migrate_tag(tag, log):
    """Rewrites one block entity compound in place. Appends a line to `log` for
    every change, so a dry run reports exactly what a write would do."""
    be_id = tag.get('id', ('S', '?'))[1]

    for key, value in list(tag.items()):
        # A BlockPos as {X,Y,Z}. 26.2 reads it with BlockPos.CODEC, which wants a
        # list -- "Failed to decode ... Not a list" in the log.
        if _is_xyz_compound(value):
            d = value[1]
            tag[key] = _blockpos_list(d['X'][1], d['Y'][1], d['Z'][1])
            log.append('%s / %s: {X,Y,Z} compound -> list of ints' % (be_id, key))
            continue

        if (be_id, key) in ENUM_KEYS and value[0] == 'S' and value[1].isupper():
            tag[key] = ('S', value[1].lower())
            log.append('%s / %s: %r -> %r' % (be_id, key, value[1], value[1].lower()))
            continue

        if be_id in FLATTEN_INVENTORY and key == 'Inventory' and value[0] == 'C':
            inner = value[1]
            extra = set(inner) - {'Items'}
            if 'Items' not in inner or not extra <= FLATTEN_INVENTORY[be_id]:
                raise ValueError('%s / Inventory has unexpected keys %s; refusing to flatten'
                                 % (be_id, sorted(extra)))
            items = inner['Items']
            if items[1][1]:
                raise ValueError('%s / Inventory is not empty; the item shape inside it was never '
                                 'grounded against a reference, so this refuses to convert it' % be_id)
            tag[key] = items
            log.append('%s / Inventory: compound -> list (dropped %s, all defaults)'
                       % (be_id, ', '.join(sorted(extra))))
            continue

        if be_id == 'create:redstone_link' and key == 'LastKnownPosition' and value[0] == 'l':
            x, y, z = _unpack_block_pos(value[1])
            tag[key] = _blockpos_list(x, y, z)
            log.append('%s / %s: packed long -> [%d, %d, %d]' % (be_id, key, x, y, z))
            continue

    # The deployer's inventory is the fake player's, and it moved one level down:
    # DeployerBlockEntity.write builds a TagValueOutput, hands its "Inventory"
    # list to Inventory.save, and stores the whole compound. So the list the old
    # files hold at the top belongs inside a compound under the same name.
    # The element shape is already right -- ItemStackWithSlot.CODEC is a "Slot"
    # unsigned byte plus ItemStack.MAP_CODEC inlined, which is exactly {Slot, id,
    # count}. Create Fly's own deployers only ever ship ('C', {}), an empty one,
    # which confirms the outer type but shows nothing about the contents.
    if be_id == 'create:deployer':
        inv = tag.get('Inventory')
        if inv and inv[0] == 'L':
            for element in inv[1][1]:
                if element[0] != 'C' or not {'Slot', 'id'} <= set(element[1]):
                    raise ValueError('create:deployer Inventory holds %r, which is not an '
                                     'ItemStackWithSlot; refusing to move it' % (element,))
            tag['Inventory'] = ('C', {'Inventory': inv})
            log.append('create:deployer / Inventory: top-level list -> compound wrapping it')

    # A depot's held item stores the face it came in through. That used to be a
    # 3D data value and is a Direction name now, which is the "Not a string"
    # in the log. Reference: display_link_redstone.nbt, InDirection='east'.
    held = tag.get('HeldItem')
    if be_id == 'create:depot' and held and held[0] == 'C':
        d = held[1].get('InDirection')
        if d and d[0] == 'i':
            if not 0 <= d[1] < len(DIRECTIONS):
                raise ValueError('create:depot InDirection %d is not a 3D data value' % d[1])
            held[1]['InDirection'] = ('S', DIRECTIONS[d[1]])
            log.append('create:depot / HeldItem.InDirection: %d -> %r' % (d[1], DIRECTIONS[d[1]]))

    # The arm's interaction points carry their own enum, one level down.
    points = tag.get('InteractionPoints')
    if be_id == 'create:mechanical_arm' and points and points[0] == 'L':
        for point in points[1][1]:
            mode = point[1].get('Mode')
            if mode and mode[0] == 'S' and mode[1].isupper():
                point[1]['Mode'] = ('S', mode[1].lower())
                log.append('create:mechanical_arm / InteractionPoints[].Mode: %r -> %r'
                           % (mode[1], mode[1].lower()))


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

    if cmd == 'migrate':
        write = '--write' in argv
        total = 0
        for p in files:
            name, root = load(p)
            log = []
            for pos, tag in block_entities(root):
                migrate_tag(tag, log)
            if not log:
                continue
            total += len(log)
            print('%s (%d)' % (os.path.basename(p), len(log)))
            for line in sorted(set(log)):
                print('    %-4d %s' % (log.count(line), line))
            if write:
                save(p, name, root)
        print('\n%d changes across %d files%s' % (total, len(files),
                                                 '' if write else ' -- dry run, pass --write to apply'))
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
