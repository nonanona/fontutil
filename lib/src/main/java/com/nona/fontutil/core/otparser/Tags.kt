package com.nona.fontutil.core.otparser

const val SFNT_VERSION_1_0 = 0x0001_0000L
const val SFNT_TAG_OTTO = 0x4F_54_54_4FL

const val TAG_ttcf = 0x74_74_63_66L
const val TAG_OS_2 = 0x4F_53_2F_32L
const val TAG_cmap = 0x63_6D_61_70L
const val TAG_name = 0x6E_61_6D_65L
const val TAG_loca = 0x6C_6F_63_61L
const val TAG_head = 0x68_65_61_64L
const val TAG_maxp = 0x6D_61_78_70L
const val TAG_glyf = 0x67_6C_79_66L
const val TAG_CFF  = 0x43_46_46_20L
const val TAG_hmtx = 0x68_6D_74_78L
const val TAG_hhea = 0x68_68_65_61L

fun Long.toTagName(): String{
    val chars = CharArray(4)
    chars[0] = ((this and 0xFF_00_00_00) shr 24).toChar()
    chars[1] = ((this and 0x00_FF_00_00) shr 16).toChar()
    chars[2] = ((this and 0x00_00_FF_00) shr 8).toChar()
    chars[3] = ((this and 0x00_00_00_FF)).toChar()
    return String(chars)
}