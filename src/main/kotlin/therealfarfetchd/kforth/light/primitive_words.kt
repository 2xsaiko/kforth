package therealfarfetchd.kforth.light

import java.time.Instant
import java.time.ZoneId
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.abs
import therealfarfetchd.kforth.light.WordFlag.CompileOnly as COMPILE_ONLY
import therealfarfetchd.kforth.light.WordFlag.Hidden as HIDDEN
import therealfarfetchd.kforth.light.WordFlag.Immediate as IMMEDIATE

@Suppress("FunctionName")
@JvmSynthetic
internal fun initDictionary(d: Dictionary) {
  val cellsize = 4

  operator fun str.invoke(standardExitHandling: bool = true, op: Forth.() -> Unit): Int {
    d.createPrimitive(this, !standardExitHandling, op)
    return d.latest
  }

  infix fun str.compose(op: ComposedWordDSL.() -> Unit): Int {
    d.composeWord(this, op)
    return d.latest
  }

  infix fun str.compose(def: str) = this compose { def() }

  infix fun i32.CONSTANT(name: str) = d.createConstant(name, this)
  infix fun i32.VARIABLE(name: str) = d.createVariable(name, this)

  infix fun i32.IS(flags: i8) = d.setFlags(this, flags)
  infix fun str.IS(flags: i8) = d.setFlags(d.findWord(this), flags)

  fun Forth.readCStr(addr: i32): str =
    (addr + 1 until addr + 1 + mem[addr].i).map(mem::get).fold("") { acc, a -> acc + a.c }

  // convert a boolean to a Forth boolean (-1 = true, 0 = false)
  fun fb(b: bool) = if (b) -1 else 0

  run {
    val here = d.here
    "DOPRI" {}
    d.here = here
    d.DOPRI_ptr = d.toCfa(d.latest)
    d.latest = 0
  }

  "DOPRI" {}
  "DOCOL" {}
  d.DOCOL_ptr = d.toCfa(d.latest)
  "DOVAR" { data.push(fip); fip = ret.pop() }
  d.DOVAR_ptr = d.toCfa(d.latest)
  "DOCON" { data.push(mem.read32(fip)); fip = ret.pop() }
  d.DOCON_ptr = d.toCfa(d.latest)
  "DODOES" { ip = mem.read32(fip) } // TODO: is this correct?

  "EXIT" { fip = ret.pop() }
  d.EXIT_ptr = d.toCfa(d.latest)
  "LIT" { data.push(mem.read32(fip)); fip += cellsize } IS COMPILE_ONLY
  d.LIT_ptr = d.toCfa(d.latest)

  "BRANCH" { fip += mem.read32(fip) } IS COMPILE_ONLY
  d.BRANCH_ptr = d.toCfa(d.latest)
  "0BRANCH" { fip += if (!data.pop().v) mem.read32(fip) else 4 } IS COMPILE_ONLY
  d.CBRANCH_ptr = d.toCfa(d.latest)

  "EXECUTE"(false) { ip = data.pop() }

  cellsize CONSTANT "CELL"
  -1 CONSTANT "TRUE"

  0 CONSTANT "FALSE"
  ' '.i CONSTANT "BL"
  '\n'.i CONSTANT "NEWLINE"

  0 CONSTANT "0"
  1 CONSTANT "1"
  2 CONSTANT "2"

  d.forth.sp0 CONSTANT "0SP"
  d.forth.rp0 CONSTANT "0RP"
  "SP0" { data.clear() }
  "RP0" { ret.clear() }

  0 VARIABLE "STATE"
  d.STATE_val = d.toCfa(d.latest) + 4
  0 VARIABLE "LATEST"
  val latestTmp = d.latest
  d.LATEST_val = d.toCfa(d.latest) + 4
  d.latest = latestTmp

  0 VARIABLE "CISL" // case-insensitive lookup
  d.CISL_val = d.toCfa(d.latest) + 4

  "HERE" { data.push(d.here) }
  "HEAP-SIZE" { data.push(memorySize) }

  HIDDEN.i CONSTANT "(FLAG-HIDDEN)"
  COMPILE_ONLY.i CONSTANT "(FLAG-CO)"
  IMMEDIATE.i CONSTANT "(FLAG-IMMED)"

  "(SSMEM)" { dumpMemory(this) } // debug thing

  "+" { data.push(data.pop() + data.pop()) }
  "1+" { data.push(data.pop().inc()) }
  "1-" { data.push(data.pop().dec()) }
  "-" { data.push(-data.pop() + data.pop()) }
  "2+" { data.push(data.pop() + 2) }
  "2-" { data.push(data.pop() - 2) }
  "*" { data.push(data.pop() * data.pop()) }
  "2*" { data.push(data.pop() * 2) }
  "/" { data.swap(); data.push(data.pop() / data.pop()) }
  "2/" { data.push(data.pop() / 2) }
  "*/" { data.pop().also { div -> data.push(((data.pop().toLong() * data.pop().toLong()) / div).i) } }
  "*/MOD" { data.pop().also { div -> data.push(((data.pop().toLong() * data.pop().toLong()) % div).i); data.push(((data.pop().toLong() * data.pop().toLong()) / div).i) } }
  "MOD" { data.swap(); data.push(data.pop() % data.pop()) }
  "M*" { data.push64(data.pop().toLong() * data.pop().toLong()) }
  "NEGATE" { data.push(-data.pop()) }
  "ABS" { data.push(abs(data.pop())) }
  "MIN" { data.push(minOf(data.pop(), data.pop())) }
  "MAX" { data.push(maxOf(data.pop(), data.pop())) }

  "S>D" { data.peek().also { data.push(if (it < 0) -1 else 0) } }
  "D>S" { data.pop() }
  "D+" { data.push64(data.pop64() + data.pop64()) }
  "D1+" { data.push64(data.pop64().inc()) }
  "D1-" { data.push64(data.pop64().dec()) }
  "D-" { data.push64(-data.pop64() + data.pop64()) }
  "D2+" { data.push64(data.pop64() + 2) }
  "D2-" { data.push64(data.pop64() - 2) }
  "D*"  { data.push64(data.pop64() * data.pop64()) }
  "D2*" { data.push64(data.pop64() * 2) }
  "D/" { data.swap64();data.push64(data.pop64() / data.pop64()) }
  "D2/" { data.push64(data.pop64() / 2) }
  "DNEGATE" { data.push64(-data.pop64()) }
  "DABS" { data.push64(abs(data.pop64())) }
  "DMIN" { data.push64(minOf(data.pop64(), data.pop64())) }
  "DMAX" { data.push64(maxOf(data.pop64(), data.pop64())) }

  "=" { data.push(fb(data.pop() == data.pop())) }
  "<>" { data.push(fb(data.pop() != data.pop())) }
  "0<" { data.push(fb(data.pop() < 0)) }
  "0=" { data.push(fb(data.pop() == 0)) }
  "0<>" { data.push(fb(data.pop() != 0)) }
  "0>" { data.push(fb(data.pop() > 0)) }
  "<" { data.push(fb(data.pop() > data.pop())) }
  ">" { data.push(fb(data.pop() < data.pop())) }
  ">=" { data.push(fb(data.pop() <= data.pop())) }
  "<=" { data.push(fb(data.pop() >= data.pop())) }

  "D=" { data.push(fb(data.pop64() == data.pop64())) }
  "D<>" { data.push(fb(data.pop64() != data.pop64())) }
  "D0<" { data.push(fb(data.pop64() < 0L)) }
  "D0=" { data.push(fb(data.pop64() == 0L)) }
  "D0<>" { data.push(fb(data.pop64() != 0L)) }
  "D0<" { data.push(fb(data.pop64() > 0L)) }
  "D<" { data.push(fb(data.pop64() > data.pop64())) }
  "D>" { data.push(fb(data.pop64() < data.pop64())) }
  "D>=" { data.push(fb(data.pop64() <= data.pop64())) }
  "D<=" { data.push(fb(data.pop64() >= data.pop64())) }

  "NOT" { data.push(data.pop().inv()) }
  "AND" { data.push(data.pop() and data.pop()) }
  "OR" { data.push(data.pop() or data.pop()) }
  "XOR" { data.push(data.pop() xor data.pop()) }
  "LSHIFT" { data.swap(); data.push(data.pop() shl data.pop()) }
  "RSHIFT" { data.swap(); data.push(data.pop() ushr data.pop()) }

  "DEPTH" { data.push(data.depth()) }
  "DUP" { data.push(data.peek()) }
  "?DUP" { data.peek().takeIf { it != 0 }?.also(data::push) }
  "2DUP" { data.push(data.indexedPeek(1)); data.push(data.indexedPeek(1)) }
  "?2DUP" { Pair(data.peek(), data.indexedPeek(1)).takeIf { it.first != it.second }?.also { data.push(it.second); data.push(it.first) } }
  "DROP" { data.pop() }
  "2DROP" { data.pop(); data.pop() }
  "SWAP" { data.swap() }
  "2SWAP" { data.push(data.indexedPop(3)); data.push(data.indexedPop(3)) }
  "OVER" { data.push(data.indexedPeek(1)) }
  "NIP" { data.indexedPop(1) }
  "TUCK" { data.swap(); data.push(data.indexedPeek(1)) }
  "ROT" { data.push(data.indexedPop(2)) }
  "-ROT" { data.push(data.indexedPop(2)); data.push(data.indexedPop(2)) }
  "ROLL" { data.push(data.indexedPop(data.pop())) }
  "PICK" { data.push(data.indexedPeek(data.pop())) }

  ">R" { ret.push(data.pop()) }
  "R>" { data.push(ret.pop()) }
  "R@" { data.push(ret.peek()) }
  "2>R" { ret.push(data.pop()); ret.push(data.pop()) }
  "2R>" { data.push(ret.pop()); data.push(ret.pop()) }
  "RDROP" { ret.pop() }
  "UNLOOP" { ret.pop(); ret.pop() }
  "I" { data.push(ret.indexedPeek(1)) }
  "J" { data.push(ret.indexedPeek(3)) }
  "K" { data.push(ret.indexedPeek(5)) }

  "CELLS" { data.push(cellsize * data.pop()) }
  "CELL+" compose "CELL +"
  "@" { data.push(mem.read32(data.pop())) }
  "!" { mem.write32(data.pop(), data.pop()) }
  "+!" { data.pop().also { addr -> mem.write32(addr, mem.read32(addr) + data.pop()) } }
  "C@" { data.push(mem[data.pop()].i) }
  "C!" { mem[data.pop()] = data.pop().b }
  "C+!" { data.pop().also { addr -> mem[addr] = (mem[addr] + data.pop()).b } }
  "2@" compose "DUP CELL+ @ SWAP @"
  "2!" compose "SWAP OVER ! CELL+ !"
  "OFF" compose "FALSE SWAP !"
  "ON" compose "TRUE SWAP !"
  "ALLOT" { dict.allot(data.pop()) }
  ">CFA" { data.push(dict.toCfa(data.pop())) }
  ">DFA" { data.push(data.pop() + 4) }
  "," { dict.append32(data.pop()) }
  "C," { dict.append8(data.pop().b) }
  "CFA," compose ">CFA ,"
  "LATESTXT" compose "LATEST @ >CFA"

  "FILL" {
    val b = data.pop().b
    val len = data.pop()
    val addr = data.pop()
    (addr until addr + len.l).forEach { mem[it.i] = b }
  }

  "CMOVE" {
    val len = data.pop()
    val dest = data.pop()
    val src = data.pop()
    for (i in 0 until len)
      mem[dest + i] = mem[src + i]
  }

  "CMOVE>" {
    val len = data.pop()
    val dest = data.pop()
    val src = data.pop()
    for (i in (len - 1) downTo 0)
      mem[dest + i] = mem[src + i]
  }

  "MOVE" compose {
    "2 PICK 1 PICK >"()
    cbranch("a")
    "CMOVE EXIT"()
    label("a")
    "CMOVE>"()
  }

  10 VARIABLE "BASE"
  "HEX" compose "16 BASE !"
  "DECIMAL" compose "10 BASE !"
  "BINARY" compose "2 BASE !"

  // TODO
  "XKEY?" { data.push(term.read()?.i ?: 0) }
  "XKEY" { data.push(term.readBlocking().i) }
  "KEY?" { data.push(term.read()?.b?.i ?: 0) }
  "KEY" { data.push(term.readBlocking().b.i) }

  "XEMIT" { term.write(data.pop().c) }
  "EMIT" compose "255 AND XEMIT"
  "COUNT" compose "DUP 1+ SWAP C@"
  "." { data.pop().toString().forEach(term::write); term.write(' ') } // TODO: real numeric output!
  "D." { data.pop64().toString().forEach(term::write); term.write(' ') } // TODO: real numeric output!
  "CR" compose "NEWLINE EMIT"
  "SPACE" compose "BL EMIT"
  "?" compose "@ ."
  ".S" {
    val depth = data.depth()
    term.write('<')
    depth.toString().forEach(term::write)
    term.write('>')
    term.write(' ')

    for (i in depth - 1 downTo 0) {
      data.indexedPeek(i).toString().forEach(term::write)
      term.write(' ')
    }
  }

  "MS" { data.pop().takeIf { it > 0 }?.also { Thread.sleep(it.l) } }
  "TIME&DATE" { Instant.now().atZone(ZoneId.systemDefault()).let { listOf(it.second, it.minute, it.hour, it.dayOfMonth, it.monthValue, it.year).forEach(data::push) } }
  "UTIME" { data.push64(System.currentTimeMillis()) }

  "FREE" compose "HEAP-SIZE HERE -"

  "BYE" { "o/\n".forEach(term::write); stop() }

  val quitPtrAddr = d.here
  d.allot(4)

  "ABORT" compose {
    "SP0"()
    "$quitPtrAddr @ EXECUTE"()
  }

  // TODO
  // "-TRAILING" compose ""

  val tibSize = 256
  tibSize CONSTANT "TIB-SIZE"
  d.createDefinition("TIB")
  d.allot(tibSize)
  0 VARIABLE "#TIB"
  0 VARIABLE ">IN"
  // 0 VARIABLE "BLK"

  "TYPE" compose {
    "DUP"()
    cbranch("empty")
    "0"()
    label("loop")
    "2>R"()
    "DUP I + C@ EMIT"()
    "2R> 1+"()
    "2DUP ="()
    cbranch("loop")
    "DROP"()
    label("empty")
    "2DROP"()
  }

  "NEXT-CHAR" compose {
    ">IN @ #TIB @ >="()
    cbranch("scan_next")
    "0 EXIT"()
    label("scan_next")
    ">IN @ TIB + C@"()
    "1 >IN +!"()
  }

  // delim -- addr
  "WORD" compose {
    "0 HERE C!"()

    label("loop")
    "NEXT-CHAR"()
    "DUP 0="()
    cbranch("no_eof")
    branch("exit")
    label("no_eof")
    "2DUP ="()
    cbranch("not_equal")
    "HERE C@ 0="()
    cbranch("exit")
    "DROP"()
    branch("loop")
    label("not_equal")
    "1 HERE C+!"()
    "HERE C@ HERE + C!"()
    branch("loop")
    label("exit")
    "2DROP HERE"()
  }

  "CHAR" compose {
    "BL WORD COUNT"()
    "0="()
    cbranch("nochars")
    "DROP 0 EXIT"()
    label("nochars")
    "C@"()
  }

  "FIND" {
    val saddr = data.pop()
    val s = readCStr(saddr)
    val wptr = d.findWord(s)
    if (wptr == 0) {
      data.push(saddr)
      data.push(0)
    } else {
      val flags = d.getFlags(wptr)
      var n = 0
      if ((flags and IMMEDIATE).v) {
        n = 1
      }
      if ((flags and COMPILE_ONLY).v) {
        n = n or 2
      }
      if (n == 0) {
        n = -1
      }
      data.push(wptr)
      data.push(n)
    }
  }

  "ACCEPT" {
    val maxLen = data.pop()
    val addr = data.pop()
    val str = term.readLine(maxLen)
    str.withIndex().forEach { (index, i) -> mem[addr + index] = i.b }
    data.push(str.length)
  } // addr +n -- n

  "PARSE-NUM" {
    val s = readCStr(data.pop())
    val n = s.toIntOrNull()
    if (n == null) {
      data.push(0)
    } else {
      data.push(n)
      data.push(-1)
    }
  }

  "REVEAL" compose "LATEST @ >DFA DUP C@ (FLAG-HIDDEN) NOT AND SWAP C!"
  "HIDE" compose "LATEST @ >DFA DUP C@ (FLAG-HIDDEN) OR SWAP C!"
  "COMPILE-ONLY" compose "LATEST @ >DFA DUP C@ (FLAG-CO) OR SWAP C!" IS IMMEDIATE
  "IMMEDIATE" compose "LATEST @ >DFA DUP C@ (FLAG-IMMED) OR SWAP C!" IS IMMEDIATE

  val msgStackUnderflow = d.here
  d.appendStr("Stack empty")
  val msgStackOverflow = d.here
  d.appendStr("Stack overflow")

  "(VALIDATE-STATE)" compose {
    "DEPTH 0<"()
    cbranch("next1")
    "$msgStackUnderflow COUNT TYPE ABORT"()
    label("next1")
    "DEPTH ${d.forth.stackHeight} >"()
    cbranch("next2")
    "$msgStackOverflow COUNT TYPE ABORT"()
    label("next2")
  }

  val msgCompileOnly = d.here
  d.appendStr("Interpreting a compile-only word: ")
  val msgUnkToken = d.here
  d.appendStr("Unknown token: ")

  "'" compose {
    "BL WORD FIND 0="()
    cbranch("notfound")
    "$msgUnkToken COUNT TYPE COUNT TYPE ABORT"()
    label("notfound")
  }

  "INTERPRET" compose {
    label("loop") // empty stack
    "BL WORD"()
    "DUP COUNT NIP"()
    cbranch("end") // name
    "FIND"()
    "DUP"()
    cbranch("word_not_found") // wptr flags
    "STATE @ 0="()
    cbranch("compile") // wptr flags
    "DUP -1 <> SWAP 2 AND AND"()
    cbranch("compile_only") // wptr
    "$msgCompileOnly COUNT TYPE HERE COUNT TYPE"() // this "HERE" is cheating, this word needs to be refactored anyways
    "ABORT"()

    label("compile_only") // wptr
    ">CFA EXECUTE (VALIDATE-STATE)"()
    branch("loop") // empty stack

    label("compile") // wptr flags
    "DUP -1 <> SWAP 1 AND AND"()
    cbranch("immediate")
    ">CFA EXECUTE (VALIDATE-STATE)"()
    branch("end_immediate")
    label("immediate")
    ">CFA ,"()
    label("end_immediate")
    branch("loop") // empty stack

    label("word_not_found") // wptr flags
    "DROP"()
    "DUP PARSE-NUM 0="()
    cbranch("parse-fail") // wptr result
    "$msgUnkToken COUNT TYPE COUNT TYPE"()
    "ABORT"()

    label("parse-fail") // wptr result
    "NIP"()
    "STATE @"()
    cbranch("compile_num")
    "' LIT CFA, ,"()
    label("compile_num")
    branch("loop") // result

    label("end") // name
    "DROP"()
  }

  val interpretPrompt = d.here
  d.appendStr("> ")
  val compilePrompt = d.here
  d.appendStr("compile: ")
  val okPrompt = d.here
  d.appendStr(" ok")

  "QUIT" compose {
    "RP0"()
    "0 STATE !"()

    label("loop")
    "CR"()
    "STATE @"()
    cbranch("interpret")
    branch("compile")

    label("exec")
    "TIB TIB-SIZE ACCEPT #TIB !"()
    "0 >IN !"()
    "INTERPRET"()
    "$okPrompt COUNT TYPE"()
    branch("loop")

    label("interpret")
    "DEPTH . $interpretPrompt COUNT TYPE"()
    branch("exec")

    label("compile")
    "$compilePrompt COUNT TYPE"()
    branch("exec")
  }
  d.mem.write32(quitPtrAddr, d.toCfa(d.latest))

  val part1 = d.here
  d.appendStr(
    """kforth $Version, (c) 2018 Marco Rebhan (the_real_farfetchd)
      |Type `BYE' to exit.
      """.trimMargin())
  val part2 = d.here
  d.appendStr("bytes free.")

  "COLD" compose {
    "DECIMAL"()
    "CISL OFF"()
    "$part1 COUNT TYPE CR"()
    "FREE ."()
    "$part2 COUNT TYPE CR"()
    "QUIT"()
  }
  d.COLD_ptr = d.toCfa(d.latest)

  "\\" compose "1 WORD DROP" IS IMMEDIATE
  "(" compose "${')'.b} WORD DROP" IS IMMEDIATE

  "HEADER" compose {
    "HERE"()
    "LATEST @ ,"()
    "LATEST !"()
    "(FLAG-HIDDEN) C,"()
    "BL WORD COUNT NIP 1+ ALLOT"()
  }

  "CREATE" compose "HEADER REVEAL ' DOVAR CFA,"
  "VARIABLE" compose "CREATE CELL ALLOT"

  "(CON)" { d.createConstant(readCStr(data.pop()), data.pop()) }
  "CONSTANT" compose "BL WORD (CON)"

  "]" compose "STATE ON"
  "[" compose "STATE OFF" IS IMMEDIATE
  ";" compose "' EXIT CFA, REVEAL [" IS IMMEDIATE
  ":NONAME" compose "HERE  ' DOCOL CFA, ]"
  ":" compose "HEADER :NONAME DROP"

  "RECURSE" compose "LATESTXT ," IS (IMMEDIATE or COMPILE_ONLY)
  "LITERAL" compose "' LIT CFA, ," IS (IMMEDIATE or COMPILE_ONLY)

  ">MARK" compose "HERE 0 ," IS COMPILE_ONLY
  ">RESOLVE" compose "DUP HERE SWAP - SWAP !" IS COMPILE_ONLY
  "<MARK" compose "HERE" IS COMPILE_ONLY
  "<RESOLVE" compose "HERE - ," IS COMPILE_ONLY

  "IF" compose "' 0BRANCH CFA, >MARK" IS (IMMEDIATE or COMPILE_ONLY)
  "UNLESS" compose "' 0= CFA, IF" IS (IMMEDIATE or COMPILE_ONLY)
  "THEN" compose ">RESOLVE" IS (IMMEDIATE or COMPILE_ONLY)
  "ELSE" compose "' BRANCH CFA, >MARK SWAP >RESOLVE" IS (IMMEDIATE or COMPILE_ONLY)

  "BEGIN" compose "<MARK" IS (IMMEDIATE or COMPILE_ONLY)
  "UNTIL" compose "' 0BRANCH CFA, <RESOLVE" IS (IMMEDIATE or COMPILE_ONLY)
  "AGAIN" compose "' BRANCH CFA, <RESOLVE" IS (IMMEDIATE or COMPILE_ONLY)
  "WHILE" compose "' 0BRANCH CFA, >MARK" IS (IMMEDIATE or COMPILE_ONLY)
  "REPEAT" compose "' BRANCH CFA, SWAP <RESOLVE >RESOLVE" IS (IMMEDIATE or COMPILE_ONLY)

  // TODO do-loop
  "DO" compose { "0 HERE  'CFA 2>R ,"() } IS (IMMEDIATE or COMPILE_ONLY)
  "?DO" compose { "'CFA ?2DUP ,  'CFA <> ,  'CFA 0BRANCH ,  HERE  'CFA 0 ,  HERE  'CFA 2>R ,"() } IS (IMMEDIATE or COMPILE_ONLY)
  "+LOOP" compose { ""() } IS (IMMEDIATE or COMPILE_ONLY)
  "LOOP" compose { "'CFA 1 , +LOOP"() } IS (IMMEDIATE or COMPILE_ONLY)
  "UNLOOP" compose { "RDROP RDROP"() }

  "LATER>" compose "' R> CFA,  ' EXECUTE CFA," IS (IMMEDIATE or COMPILE_ONLY)

  val strbufLen = 1024
  val strbufPtr = d.here
  d.allot(strbufLen)
  strbufPtr CONSTANT "(SBUF)"
  strbufLen CONSTANT "(#SBUF)"
  0 VARIABLE "(>SBUF)"

  val strTooLarge = d.here
  d.appendStr("Data Buffer Overflow")

  "(SSAVE)" compose {
    "DUP (#SBUF) >"()
    cbranch("toolarge")
    "$strTooLarge COUNT TYPE ABORT"()
    label("toolarge")
    "DUP (>SBUF) @ + (#SBUF) >"()
    cbranch("zset")
    "1337 ."()
    "0 (>SBUF) !"()
    label("zset")
    "(>SBUF) @ (SBUF) + OVER 2>R"()
    "2DUP (SBUF) (>SBUF) @ + SWAP MOVE NIP"()
    "(>SBUF) +!"()
    "2R>"()
  }

  "(S\")" { data.push(fip + 1); data.push(mem[fip].i); fip += mem[fip] + 1 } IS COMPILE_ONLY
  "(IS\")" compose "${'"'.b} WORD COUNT (SSAVE)"
  "(CS\")" compose "' (S\") CFA,  ${'"'.b} WORD COUNT 1+ ALLOT DROP"
  "S\"" compose {
    "STATE @"()
    cbranch("interpret")
    "(CS\") EXIT"()
    label("interpret")
    "(IS\")"()
  } IS IMMEDIATE

  ".\"" compose "S\"  ' TYPE CFA," IS (IMMEDIATE or COMPILE_ONLY)
  "ABORT\"" compose ".\"  ' ABORT CFA," IS (IMMEDIATE or COMPILE_ONLY)

  "WORDS" compose {
    "LATEST @"()

    label("loop")
    "DUP 0="()
    cbranch("end_of_words")
    "DROP EXIT"()

    label("end_of_words")
    "DUP CELL+ @ (FLAG-HIDDEN) AND 0="()
    cbranch("hidden")
    "DUP CELL+ 1+ COUNT TYPE SPACE"()
    label("hidden")
    "@"()
    branch("loop")
  }

  "(CON)" IS HIDDEN
  "NEXT-CHAR" IS HIDDEN
  "(IS\")" IS HIDDEN
  "(CS\")" IS HIDDEN
  "(FLAG-IMMED)" IS HIDDEN
  "(FLAG-CO)" IS HIDDEN
  "(FLAG-HIDDEN)" IS HIDDEN
}