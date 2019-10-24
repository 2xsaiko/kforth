package therealfarfetchd.kforth.light.debugger

import therealfarfetchd.kforth.light.Memory
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class MemoryTableModel(val mem: Memory) : TableModel {

  override fun addTableModelListener(l: TableModelListener) {

  }

  override fun getRowCount(): Int {
    return mem.size
  }

  override fun getColumnName(columnIndex: Int): String {
    return when (columnIndex) {
      0 -> "Address"
      1 -> "Value"
      2 -> "Hex Value"
      else -> error("")
    }
  }

  override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
    return rowIndex != 0
  }

  override fun getColumnClass(columnIndex: Int): Class<*> {
    return when (columnIndex) {
      0 -> String::class.java
      1, 2 -> Int::class.javaObjectType
      else -> error("")
    }
  }

  override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    val radix = when (rowIndex) {
      1 -> 10
      2 -> 16
      else -> error("")
    }
    val value = (aValue as? String)?.toIntOrNull(radix) ?: return
    mem[rowIndex] = value.toByte()
  }

  override fun getColumnCount(): Int {
    return 3
  }

  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
    return when (columnIndex) {
      0 -> "$${rowIndex.toString(16).padStart(8, '0')}"
      1 -> mem[rowIndex].toInt() and 0xFF
      2 -> (mem[rowIndex].toInt() and 0xFF).toString(16).padStart(2, '0')
      else -> error("")
    }
  }

  override fun removeTableModelListener(l: TableModelListener?) {
  }
}