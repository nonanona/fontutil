package com.nona.fontutil.core.otparser

internal val ROBOTO_COVERAGE = setOf(
    0x0000, 0x0002, 0x000d, 0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x0027, 0x0028,
    0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034,
    0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f, 0x0040,
    0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c,
    0x004d, 0x004e, 0x004f, 0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058,
    0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f, 0x0060, 0x0061, 0x0062, 0x0063, 0x0064,
    0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f, 0x0070,
    0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a, 0x007b, 0x007c,
    0x007d, 0x007e, 0x00a0, 0x00a1, 0x00a2, 0x00a3, 0x00a4, 0x00a5, 0x00a6, 0x00a7, 0x00a8, 0x00a9,
    0x00aa, 0x00ab, 0x00ac, 0x00ad, 0x00ae, 0x00af, 0x00b0, 0x00b1, 0x00b2, 0x00b3, 0x00b4, 0x00b5,
    0x00b6, 0x00b7, 0x00b8, 0x00b9, 0x00ba, 0x00bb, 0x00bc, 0x00bd, 0x00be, 0x00bf, 0x00c0, 0x00c1,
    0x00c2, 0x00c3, 0x00c4, 0x00c5, 0x00c6, 0x00c7, 0x00c8, 0x00c9, 0x00ca, 0x00cb, 0x00cc, 0x00cd,
    0x00ce, 0x00cf, 0x00d0, 0x00d1, 0x00d2, 0x00d3, 0x00d4, 0x00d5, 0x00d6, 0x00d7, 0x00d8, 0x00d9,
    0x00da, 0x00db, 0x00dc, 0x00dd, 0x00de, 0x00df, 0x00e0, 0x00e1, 0x00e2, 0x00e3, 0x00e4, 0x00e5,
    0x00e6, 0x00e7, 0x00e8, 0x00e9, 0x00ea, 0x00eb, 0x00ec, 0x00ed, 0x00ee, 0x00ef, 0x00f0, 0x00f1,
    0x00f2, 0x00f3, 0x00f4, 0x00f5, 0x00f6, 0x00f7, 0x00f8, 0x00f9, 0x00fa, 0x00fb, 0x00fc, 0x00fd,
    0x00fe, 0x00ff, 0x0100, 0x0101, 0x0102, 0x0103, 0x0104, 0x0105, 0x0106, 0x0107, 0x0108, 0x0109,
    0x010a, 0x010b, 0x010c, 0x010d, 0x010e, 0x010f, 0x0110, 0x0111, 0x0112, 0x0113, 0x0114, 0x0115,
    0x0116, 0x0117, 0x0118, 0x0119, 0x011a, 0x011b, 0x011c, 0x011d, 0x011e, 0x011f, 0x0120, 0x0121,
    0x0122, 0x0123, 0x0124, 0x0125, 0x0126, 0x0127, 0x0128, 0x0129, 0x012a, 0x012b, 0x012c, 0x012d,
    0x012e, 0x012f, 0x0130, 0x0131, 0x0132, 0x0133, 0x0134, 0x0135, 0x0136, 0x0137, 0x0138, 0x0139,
    0x013a, 0x013b, 0x013c, 0x013d, 0x013e, 0x013f, 0x0140, 0x0141, 0x0142, 0x0143, 0x0144, 0x0145,
    0x0146, 0x0147, 0x0148, 0x0149, 0x014a, 0x014b, 0x014c, 0x014d, 0x014e, 0x014f, 0x0150, 0x0151,
    0x0152, 0x0153, 0x0154, 0x0155, 0x0156, 0x0157, 0x0158, 0x0159, 0x015a, 0x015b, 0x015c, 0x015d,
    0x015e, 0x015f, 0x0160, 0x0161, 0x0162, 0x0163, 0x0164, 0x0165, 0x0166, 0x0167, 0x0168, 0x0169,
    0x016a, 0x016b, 0x016c, 0x016d, 0x016e, 0x016f, 0x0170, 0x0171, 0x0172, 0x0173, 0x0174, 0x0175,
    0x0176, 0x0177, 0x0178, 0x0179, 0x017a, 0x017b, 0x017c, 0x017d, 0x017e, 0x017f, 0x018f, 0x0192,
    0x01a0, 0x01a1, 0x01af, 0x01b0, 0x01f0, 0x01fa, 0x01fb, 0x01fc, 0x01fd, 0x01fe, 0x01ff, 0x0218,
    0x0219, 0x021a, 0x021b, 0x0237, 0x0259, 0x02bc, 0x02c6, 0x02c7, 0x02c9, 0x02d8, 0x02d9, 0x02da,
    0x02db, 0x02dc, 0x02dd, 0x02f3, 0x0300, 0x0301, 0x0303, 0x0309, 0x030f, 0x0323, 0x0384, 0x0385,
    0x0386, 0x0387, 0x0388, 0x0389, 0x038a, 0x038c, 0x038e, 0x038f, 0x0390, 0x0391, 0x0392, 0x0393,
    0x0394, 0x0395, 0x0396, 0x0397, 0x0398, 0x0399, 0x039a, 0x039b, 0x039c, 0x039d, 0x039e, 0x039f,
    0x03a0, 0x03a1, 0x03a3, 0x03a4, 0x03a5, 0x03a6, 0x03a7, 0x03a8, 0x03a9, 0x03aa, 0x03ab, 0x03ac,
    0x03ad, 0x03ae, 0x03af, 0x03b0, 0x03b1, 0x03b2, 0x03b3, 0x03b4, 0x03b5, 0x03b6, 0x03b7, 0x03b8,
    0x03b9, 0x03ba, 0x03bb, 0x03bc, 0x03bd, 0x03be, 0x03bf, 0x03c0, 0x03c1, 0x03c2, 0x03c3, 0x03c4,
    0x03c5, 0x03c6, 0x03c7, 0x03c8, 0x03c9, 0x03ca, 0x03cb, 0x03cc, 0x03cd, 0x03ce, 0x03d1, 0x03d2,
    0x03d6, 0x0400, 0x0401, 0x0402, 0x0403, 0x0404, 0x0405, 0x0406, 0x0407, 0x0408, 0x0409, 0x040a,
    0x040b, 0x040c, 0x040d, 0x040e, 0x040f, 0x0410, 0x0411, 0x0412, 0x0413, 0x0414, 0x0415, 0x0416,
    0x0417, 0x0418, 0x0419, 0x041a, 0x041b, 0x041c, 0x041d, 0x041e, 0x041f, 0x0420, 0x0421, 0x0422,
    0x0423, 0x0424, 0x0425, 0x0426, 0x0427, 0x0428, 0x0429, 0x042a, 0x042b, 0x042c, 0x042d, 0x042e,
    0x042f, 0x0430, 0x0431, 0x0432, 0x0433, 0x0434, 0x0435, 0x0436, 0x0437, 0x0438, 0x0439, 0x043a,
    0x043b, 0x043c, 0x043d, 0x043e, 0x043f, 0x0440, 0x0441, 0x0442, 0x0443, 0x0444, 0x0445, 0x0446,
    0x0447, 0x0448, 0x0449, 0x044a, 0x044b, 0x044c, 0x044d, 0x044e, 0x044f, 0x0450, 0x0451, 0x0452,
    0x0453, 0x0454, 0x0455, 0x0456, 0x0457, 0x0458, 0x0459, 0x045a, 0x045b, 0x045c, 0x045d, 0x045e,
    0x045f, 0x0460, 0x0461, 0x0462, 0x0463, 0x0464, 0x0465, 0x0466, 0x0467, 0x0468, 0x0469, 0x046a,
    0x046b, 0x046c, 0x046d, 0x046e, 0x046f, 0x0470, 0x0471, 0x0472, 0x0473, 0x0474, 0x0475, 0x0476,
    0x0477, 0x0478, 0x0479, 0x047a, 0x047b, 0x047c, 0x047d, 0x047e, 0x047f, 0x0480, 0x0481, 0x0482,
    0x0483, 0x0484, 0x0485, 0x0486, 0x0488, 0x0489, 0x048a, 0x048b, 0x048c, 0x048d, 0x048e, 0x048f,
    0x0490, 0x0491, 0x0492, 0x0493, 0x0494, 0x0495, 0x0496, 0x0497, 0x0498, 0x0499, 0x049a, 0x049b,
    0x049c, 0x049d, 0x049e, 0x049f, 0x04a0, 0x04a1, 0x04a2, 0x04a3, 0x04a4, 0x04a5, 0x04a6, 0x04a7,
    0x04a8, 0x04a9, 0x04aa, 0x04ab, 0x04ac, 0x04ad, 0x04ae, 0x04af, 0x04b0, 0x04b1, 0x04b2, 0x04b3,
    0x04b4, 0x04b5, 0x04b6, 0x04b7, 0x04b8, 0x04b9, 0x04ba, 0x04bb, 0x04bc, 0x04bd, 0x04be, 0x04bf,
    0x04c0, 0x04c1, 0x04c2, 0x04c3, 0x04c4, 0x04c5, 0x04c6, 0x04c7, 0x04c8, 0x04c9, 0x04ca, 0x04cb,
    0x04cc, 0x04cd, 0x04ce, 0x04cf, 0x04d0, 0x04d1, 0x04d2, 0x04d3, 0x04d4, 0x04d5, 0x04d6, 0x04d7,
    0x04d8, 0x04d9, 0x04da, 0x04db, 0x04dc, 0x04dd, 0x04de, 0x04df, 0x04e0, 0x04e1, 0x04e2, 0x04e3,
    0x04e4, 0x04e5, 0x04e6, 0x04e7, 0x04e8, 0x04e9, 0x04ea, 0x04eb, 0x04ec, 0x04ed, 0x04ee, 0x04ef,
    0x04f0, 0x04f1, 0x04f2, 0x04f3, 0x04f4, 0x04f5, 0x04f6, 0x04f7, 0x04f8, 0x04f9, 0x04fa, 0x04fb,
    0x04fc, 0x04fd, 0x04fe, 0x04ff, 0x0500, 0x0501, 0x0502, 0x0503, 0x0504, 0x0505, 0x0506, 0x0507,
    0x0508, 0x0509, 0x050a, 0x050b, 0x050c, 0x050d, 0x050e, 0x050f, 0x0510, 0x0511, 0x0512, 0x0513,
    0x1e00, 0x1e01, 0x1e3e, 0x1e3f, 0x1e80, 0x1e81, 0x1e82, 0x1e83, 0x1e84, 0x1e85, 0x1ea0, 0x1ea1,
    0x1ea2, 0x1ea3, 0x1ea4, 0x1ea5, 0x1ea6, 0x1ea7, 0x1ea8, 0x1ea9, 0x1eaa, 0x1eab, 0x1eac, 0x1ead,
    0x1eae, 0x1eaf, 0x1eb0, 0x1eb1, 0x1eb2, 0x1eb3, 0x1eb4, 0x1eb5, 0x1eb6, 0x1eb7, 0x1eb8, 0x1eb9,
    0x1eba, 0x1ebb, 0x1ebc, 0x1ebd, 0x1ebe, 0x1ebf, 0x1ec0, 0x1ec1, 0x1ec2, 0x1ec3, 0x1ec4, 0x1ec5,
    0x1ec6, 0x1ec7, 0x1ec8, 0x1ec9, 0x1eca, 0x1ecb, 0x1ecc, 0x1ecd, 0x1ece, 0x1ecf, 0x1ed0, 0x1ed1,
    0x1ed2, 0x1ed3, 0x1ed4, 0x1ed5, 0x1ed6, 0x1ed7, 0x1ed8, 0x1ed9, 0x1eda, 0x1edb, 0x1edc, 0x1edd,
    0x1ede, 0x1edf, 0x1ee0, 0x1ee1, 0x1ee2, 0x1ee3, 0x1ee4, 0x1ee5, 0x1ee6, 0x1ee7, 0x1ee8, 0x1ee9,
    0x1eea, 0x1eeb, 0x1eec, 0x1eed, 0x1eee, 0x1eef, 0x1ef0, 0x1ef1, 0x1ef2, 0x1ef3, 0x1ef4, 0x1ef5,
    0x1ef6, 0x1ef7, 0x1ef8, 0x1ef9, 0x1f4d, 0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005, 0x2006,
    0x2007, 0x2008, 0x2009, 0x200a, 0x200b, 0x2010, 0x2011, 0x2013, 0x2014, 0x2015, 0x2017, 0x2018,
    0x2019, 0x201a, 0x201b, 0x201c, 0x201d, 0x201e, 0x2020, 0x2021, 0x2022, 0x2025, 0x2026, 0x2027,
    0x2030, 0x2032, 0x2033, 0x2039, 0x203a, 0x203c, 0x2044, 0x2074, 0x207f, 0x20a3, 0x20a4, 0x20a6,
    0x20a7, 0x20a8, 0x20a9, 0x20aa, 0x20ab, 0x20ac, 0x20b1, 0x20b9, 0x20ba, 0x20bc, 0x20bd, 0x2105,
    0x2113, 0x2116, 0x2122, 0x2126, 0x212e, 0x215b, 0x215c, 0x215d, 0x215e, 0x2202, 0x2206, 0x220f,
    0x2211, 0x2212, 0x221a, 0x221e, 0x222b, 0x2248, 0x2260, 0x2264, 0x2265, 0x25ca, 0xee01, 0xee02,
    0xf6c3, 0xfb01, 0xfb02, 0xfb03, 0xfb04, 0xfeff, 0xfffc, 0xfffd
)