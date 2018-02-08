package therealfarfetchd.kforth.light.term

import therealfarfetchd.kforth.light.Terminal
import therealfarfetchd.kforth.light.i32
import therealfarfetchd.kforth.light.str
import therealfarfetchd.kforth.light.u16

object SystemTerminalLight : Terminal {
  private val istr = System.`in`.bufferedReader()

  override fun width(): i32 = 160

  override fun height(): i32 = 50

  override fun read(): u16? {
    println("WARNING: KEY? or similar is not reliable for this terminal implementation.")
    return null
  }

  override fun readBlocking(): u16 {
    return readLine(1).firstOrNull() ?: readBlocking()
  }

  override fun readLine(len: i32): str {
    return istr.readLine()?.run { slice(0 until minOf(len, length)) } ?: ""
  }

  override fun write(c: u16) {
    print(c)
    System.out.flush()
  }

  override fun clear() {
    println(" --------------")
  }
}