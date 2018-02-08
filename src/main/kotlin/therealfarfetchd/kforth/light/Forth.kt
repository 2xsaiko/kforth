package therealfarfetchd.kforth.light

import therealfarfetchd.kforth.light.term.SystemTerminalLight
import java.io.FileOutputStream

val Version = "%VERSION%"

class Forth {
  val stackHeight = 1024
  val zpSize = 128 // unordered data
  val memorySize = 65536 // 64k of memory should be enough for now

  val sp0 = zpSize
  val rp0 = zpSize + stackHeight

  internal var state
    get() = if (dict.STATE_val != 0) mem.read32(dict.STATE_val) else error("STATE is not defined yet!")
    set(value) = if (dict.STATE_val != 0) mem.write32(dict.STATE_val, value) else error("STATE is not defined yet!")

  val mem: Memory = MemoryImpl(memorySize)

  val data: Stack = MemoryStack(this, mem, sp0, stackHeight)
  val ret: Stack = MemoryStack(this, mem, rp0, stackHeight)

  var isHalted = true; private set

  val dict = Dictionary(2 * stackHeight + zpSize, mem, this)

  lateinit var term: Terminal

  // Forth Instruction Pointer
  var fip: i32 = 0

  // Instruction Pointer
  var ip: i32 = dict.COLD_ptr

  fun eval(name: str) {
    TODO("not implemented")
  }

  fun reset() {
    ip = dict.COLD_ptr
  }

  fun mainLoop() {
    isHalted = false
    try {
      while (!isHalted)
        dict.callWord()
    } finally {
      term.detach()
    }
  }

  fun raiseException() {
    TODO("not implemented")
  }

  fun stop() {
    isHalted = true
  }
}

fun main(args: Array<str>) {
//  SystemTerminal.init()
  val f = Forth()
  dumpMemory(f)
  f.term = SystemTerminalLight
  f.mainLoop()
//  SystemTerminal.deinit()
}

fun dumpMemory(f: Forth) {
  val ostr = FileOutputStream("mem.bin")
  ostr.write((f.mem as MemoryImpl).mem)
  ostr.close()
}