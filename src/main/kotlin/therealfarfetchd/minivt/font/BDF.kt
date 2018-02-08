/*
 * Copyright (c) 2018 Marco Rebhan (the_real_farfetchd)
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package therealfarfetchd.minivt.font

import java.util.*

/**
 * An implementation of the BDF (Glyph Bitmap Distribution Format).
 */
internal data class BDF(
  val fontVer: String = "2.1",
  val glyphs: Map<Char, IGlyph>,
  val defaultGlyph: Char,
  val width: Int,
  val height: Int
) {
  interface IGlyph {
    val name: String
    val codePoint: Char
    val width: Int
    val height: Int
    val xOff: Int
    val yOff: Int
    val dwidthX: Int
    val dwidthY: Int
    val bits: BitSet
  }
}