package therealfarfetchd.kforth.light

import kotlin.experimental.or

interface Memory {
  val size: i32

  operator fun get(addr: i32): i8
  operator fun set(addr: i32, data: i8)
  fun memcpy(src: Int, dst: Int, len: Int)
}

class MemoryImpl(override val size: i32) : Memory {
  internal val mem = ByteArray(size)

  override fun get(addr: i32): i8 {
    return when (addr) {
      in 0 until size -> mem[addr]
      else            -> 0
    }
  }

  override fun set(addr: i32, data: i8) {
    when (addr) {
      in 0 until size -> mem[addr] = data
      else            -> Unit
    }
  }

  override fun memcpy(src: Int, dst: Int, len: Int) {
    System.arraycopy(mem, src, mem, dst, len)
  }
}

//private class MemoryWindow(private val parent: Memory, private val pos: i32, override val size: i32) : Memory {
//  override fun get(addr: i32): i8 {
//    return when (addr) {
//      in 0 until size -> parent[addr + pos]
//      else            -> 0
//    }
//  }
//
//  override fun set(addr: i32, data: i8) {
//    when (addr) {
//      in 0 until size -> parent[addr + pos] = data
//      else            -> Unit
//    }
//  }
//}

fun Memory.write16(addr: i32, data: i16) {
  data.asBytes().withIndex().forEach { (index, value) -> set(addr + index, value) }
}

fun Memory.write32(addr: i32, data: i32) {
  data.asBytes().withIndex().forEach { (index, value) -> set(addr + index, value) }
}

fun Memory.write64(addr: i32, data: i64) {
  data.asBytes().withIndex().forEach { (index, value) -> set(addr + index, value) }
}

fun Memory.read16(addr: i32): i16 {
  return (1 downTo 0).map { get(addr + it) }.fold(0) { acc: i16, byte: i8 -> acc shl 8 or byte.s }
}

fun Memory.read32(addr: i32): i32 {
  return (3 downTo 0).map { get(addr + it) }.fold(0) { acc: i32, byte: i8 -> acc shl 8 or byte.i }
}

fun Memory.read64(addr: i32): i64 {
  return (7 downTo 0).map { get(addr + it) }.fold(0) { acc: i64, byte: i8 -> acc shl 8 or byte.l }
}

//fun Memory.window(offset: i32, len: i32): Memory = MemoryWindow(this, offset, len)