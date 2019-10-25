package therealfarfetchd.kforth.light.debugger

import therealfarfetchd.kforth.light.Forth
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.JScrollPane
import javax.swing.JTable
import kotlin.concurrent.thread

class Debugger(val forth: Forth) : JFrame("KForth Debugger") {

  private val vStackElements = JList<String>()
  private val rStackElements = JList<String>()
  private val memory = JTable(MemoryTableModel(forth.mem, { AddressMode.I32 }))

  init {
    layout = BorderLayout(0, 0)
    setSize(800, 600)
    setLocationRelativeTo(null)
    add(JScrollPane(vStackElements), BorderLayout.WEST)
    add(JScrollPane(rStackElements), BorderLayout.EAST)

    add(JScrollPane(memory), BorderLayout.CENTER)

    thread(isDaemon = true) {
      while (true) {
        val elements = (0 until forth.data.depth()).map { i ->
          val value = forth.data.indexedPeek(i)
          "(0x${value.toString(16).padStart(8, '0')}) $value"
        }.toTypedArray()
        vStackElements.setListData(elements)

        val rElements = (0 until forth.ret.depth()).map { i ->
          val value = forth.ret.indexedPeek(i)
          "(0x${value.toString(16).padStart(8, '0')}) $value"
        }.toTypedArray()
        rStackElements.setListData(rElements)
        Thread.sleep(1L)
      }
    }
  }

}