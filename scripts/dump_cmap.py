import sys
from fontTools import ttLib

ttf = ttLib.TTFont(sys.argv[1])

cmap = ttf['cmap']
for table in cmap.tables:
    res = []
    if table.format == 12:
        for codepoint, glyph_name in table.cmap.iteritems():
            res.append('0x%04x' % codepoint)
    print ', '.join(res)
