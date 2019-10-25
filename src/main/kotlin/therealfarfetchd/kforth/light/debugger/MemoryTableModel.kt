package therealfarfetchd.kforth.light.debugger

import therealfarfetchd.kforth.light.Memory
import therealfarfetchd.kforth.light.asBytes
import therealfarfetchd.kforth.light.c
import therealfarfetchd.kforth.light.i
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class MemoryTableModel(val mem: Memory, val mode: () -> AddressMode) : TableModel {

  override fun addTableModelListener(l: TableModelListener) {

  }

  override fun getRowCount(): Int {
    return mem.size / mode().grouping
  }

  override fun getColumnName(columnIndex: Int): String {
    return when (columnIndex) {
      0 -> "Address"
      1 -> "Value"
      2 -> "Hex Value"
      3 -> "Text"
      else -> error("")
    }
  }

  override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
    return rowIndex != 0
  }

  override fun getColumnClass(columnIndex: Int): Class<*> {
    return String::class.java
  }

  override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    if (columnIndex in 1..2) {
      val radix = when (columnIndex) {
        1 -> 10
        2 -> 16
        else -> error("")
      }
      val value = (aValue as? String)?.toIntOrNull(radix) ?: return
      val x = mode().grouping
      for ((i, b) in value.asBytes().withIndex()) {
        mem[rowIndex * x + i] = b
      }
    }
  }

  override fun getColumnCount(): Int {
    return 4
  }

  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
    val mode = mode()
    val start = rowIndex * mode.grouping
    val v = (0 until mode.grouping).map { offset -> mem[start + offset].i shl (8 * offset) }.sum()
    return when (columnIndex) {
      0 -> "$${start.toString(16).padStart(8, '0')}"
      1 -> v
      2 -> v.toString(16).padStart(2 * mode.grouping, '0')
      3 -> String(v.asBytes().map { b -> if (b in 0x1f..0x7f) b.c else '.' }.toCharArray())
      else -> error("")
    }
  }

  override fun removeTableModelListener(l: TableModelListener?) {
  }
}

enum class AddressMode(val grouping: Int) {
  I8(1), I16(2), I32(4)
}