package therealfarfetchd.kforth.light

interface Stack {
  fun push(i: i32)
  fun pop(): i32
  fun peek(): i32
  fun depth(): i32
  fun indexedPeek(i: i32): i32
  fun indexedPop(i: i32): i32
  fun clear()
}

class MemoryStack(val f: Forth, val m: Memory, val addr: i32, val maxDepth: i32) : Stack {
  var pointer = addr

  override fun push(i: i32) {
    m.write32(pointer, i)
    pointer += 4
  }

  override fun pop(): i32 {
    pointer -= 4
    return m.read32(pointer)
  }

  override fun peek(): i32 {
    return m.read32(pointer - 4)
  }

  override fun depth(): i32 {
    return (pointer - addr) / 4
  }

  override fun indexedPeek(i: i32): i32 {
    return m.read32(pointer - 4 * (i + 1))
  }

  override fun indexedPop(i: i32): i32 {
    val v = indexedPeek(i)
    m.memcpy(pointer - 4 * i, pointer - 4 * (i + 1), 4 * i)
    pointer -= 4
    return v
  }

  override fun clear() {
    pointer = addr
  }
}

fun Stack.swap() {
  val a = pop()
  val b = pop()
  push(a)
  push(b)
}

fun Stack.pop64(): i64 {
  val msb = pop()
  val lsb = pop()
  return (msb.l shl 32) or lsb.l
}

fun Stack.push64(b: i64) {
  push(b.i)
  push((b shr 32).i)
}

fun Stack.swap64() {
  val a = pop64()
  val b = pop64()
  push64(a)
  push64(b)
}