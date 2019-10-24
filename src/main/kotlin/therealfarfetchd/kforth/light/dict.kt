package therealfarfetchd.kforth.light

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

// Definition layout:
// 0:   i32   - pointer to next word
// 4:   i8    - flags
// 5:   i8    - name length
// 6:   [i16] - name
// n:   i32   - "interpreter" (DOCOL, DOVAR, …) < CFA
// n+1: data  - program data

@Suppress("PropertyName", "MemberVisibilityCanBePrivate")
class Dictionary(ptr: i32, val mem: Memory, internal val forth: Forth) {
  var here = ptr
  var latest = 0
    get() = if (LATEST_val == 0) field else mem.read32(LATEST_val)
    set(value) = if (LATEST_val == 0) field = value else mem.write32(LATEST_val, value)
  val cisl
    get() = if (CISL_val == 0) false else mem.read32(CISL_val).v

  // Some useful pointers that you don't want to look up everytime
  internal var DOPRI_ptr = 0
  internal var DOCOL_ptr = 0
  internal var DOCON_ptr = 0
  internal var DOVAR_ptr = 0
  internal var LIT_ptr = 0
  internal var EXIT_ptr = 0
  internal var BRANCH_ptr = 0
  internal var CBRANCH_ptr = 0
  internal var COLD_ptr = 0
  internal var STATE_val = 0
  internal var LATEST_val = 0
  internal var CISL_val = 0

  private val primitives: MutableMap<i32, (Forth) -> Unit> = mutableMapOf()
  private val specialReturn: MutableSet<i32> = mutableSetOf()

  init {
    initDictionary(this)
  }

  fun allot(size: i32) {
    here += size
  }

  fun append8(b: i8) {
    allot(1)
    mem[here - 1] = b
  }

  fun append32(b: i32) {
    allot(4)
    mem.write32(here - 4, b)
  }

  fun resetFlags(word: i32, flags: i8) {
    val fl = getFlags(word)
    mem[word + 4] = fl and flags.inv()
  }

  fun setFlags(word: i32, flags: i8) {
    val fl = getFlags(word)
    mem[word + 4] = fl or flags
  }

  fun getFlags(word: i32): i8 {
    return mem[word + 4]
  }

  fun createHeader(name: str) {
    val l = here
    append32(latest)
    latest = l
    append8(WordFlag.Hidden)
    appendStr(name)
  }

  fun toCfa(word: i32): i32 {
    val strlen = getNameLength(word)
    return word + 6 + strlen
  }

  fun appendStr(str: str) {
    append8(str.length.b)
    str.forEach { append8(it.b) }
  }

  fun getWordName(word: i32): str =
    (0 until getNameLength(word)).map { mem[word + 6 + it] }.fold("") { acc, a -> acc + a.c }

  fun getNameLength(word: i32): i8 = mem[word + 5]

  fun createDefinition(name: str, interpreter: i32 = DOVAR_ptr) {
    createHeader(name)
    resetFlags(latest, WordFlag.Hidden)
    append32(interpreter)
  }

  fun findWord(name: str, ignoreHidden: bool = true): i32 {
    var wptr = latest
    while (wptr != 0) {
      if (!ignoreHidden || !(getFlags(wptr) and WordFlag.Hidden).v) {
        val wordName = getWordName(wptr)
        if (wordName == name || (cisl && wordName.equals(name, ignoreCase = true))) {
          return wptr
        }
      }
      wptr = mem.read32(wptr)
    }
    return 0
  }

  fun createVariable(name: str, value: i32 = 0) {
    createDefinition(name)
    append32(value)
  }

  fun createConstant(name: str, value: i32) {
    createDefinition(name, DOCON_ptr)
    append32(value)
  }

  fun createPrimitive(name: str, specialReturn: bool, op: (Forth) -> Unit) {
    createHeader(name)
    resetFlags(latest, WordFlag.Hidden)
    primitives[here] = op
    if (specialReturn) this.specialReturn += here
    append32(DOPRI_ptr)
  }

  fun callWord() {
    val ipr = mem.read32(forth.ip)
    if (ipr == DOPRI_ptr) {
      callPrimitive() // we need to hardcode this
    } else {
      forth.ret.push(forth.fip)
      forth.fip = forth.ip + 4
      forth.ip = ipr
    }
  }

  fun callPrimitive() {
    val ip = forth.ip
    primitives[ip]?.invoke(forth)
    if (ip !in specialReturn) {
      forth.ip = mem.read32(forth.fip)
      forth.fip += 4
    }
  }
}