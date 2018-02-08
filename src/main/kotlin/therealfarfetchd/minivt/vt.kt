package therealfarfetchd.minivt

import therealfarfetchd.minivt.font.BDFParser
import java.awt.*
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.Timer

class MiniVT(val name: String = "Terminal", val columns: Int = 120, val rows: Int = 45, val scale: Int = 1) {
  private var fr: Frame? = null

  private var lastDimensions: Rectangle? = null

  fun show() {
    fr?.apply { isVisible = true }
    ?: run { initFrame() }
  }

  @JvmSynthetic
  internal val scwidth = columns * fnt.width * scale

  @JvmSynthetic
  internal val scheight = rows * fnt.height * scale

  @JvmSynthetic
  internal val charW = fnt.width * scale

  @JvmSynthetic
  internal val charH = fnt.height * scale

  fun cursorX() = 0
  fun cursorY() = 0

  private fun initFrame() {
    val f = Frame(name)
    f.isResizable = false
    f.background = Color.BLACK
    f.add(VTPanel(this))
    lastDimensions?.also { f.bounds = lastDimensions }
    ?: run {
      f.setSize(scwidth, scheight)
      f.setLocationRelativeTo(null)
    }
    f.isVisible = true
    fr = f
  }

  fun hide() {
    fr?.apply {
      lastDimensions = bounds
      dispose()
    }
    fr = null
  }

  fun isVisible() = fr?.isVisible ?: false

  fun update() {
    fr?.repaint()
  }
}

private class VTPanel constructor(val vt: MiniVT) : Component() {
  private val img = BufferedImage(vt.scwidth, vt.scheight, ColorSpace.TYPE_RGB)

  private var cursorStatus = false

  init {
    Timer(500) {
      cursorStatus = !cursorStatus
      repaint()
    }.start()

  }

  override fun paint(g: Graphics) {
    g.color = Color.WHITE
    g.drawImage(img, 0, 0, null)
    if (cursorStatus) g.fillRect(vt.cursorX() * vt.charW, (vt.cursorY() + 1) * vt.charH - 2, vt.charW, 2)
  }

  private fun drawBitmap(x: Int, y: Int, width: Int, height: Int, bits: BitSet) {

  }

  private fun clear(x: Int, y: Int, width: Int, height: Int) {

  }

  private fun getGlyph(c: Char) = fnt.glyphs[c] ?: fnt.glyphs[fnt.defaultGlyph]!!
}

private val fnt = BDFParser.read(MiniVT::class.java.classLoader.getResourceAsStream("minivt/term.bdf"))