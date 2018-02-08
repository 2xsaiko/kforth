package therealfarfetchd.kforth.light

fun Dictionary.composeWord(name: str, op: ComposedWordDSL.() -> Unit) {
  val cw = ComposedWordDSL(this).also(op)
  cw.raw32(EXIT_ptr)
  cw.compile(name)
}

class ComposedWordDSL internal constructor(private val d: Dictionary) {
  private var list: List<Compilable> = emptyList()
  private var labels: Map<str, i32> = emptyMap()

  // compile words, seperated by space, into definition
  operator fun str.invoke() {
    list += split(" ").map {
      d.findWord(it).takeIf { it != 0 }?.let(::CompilableWord)
      ?: it.toIntOrNull()?.let(::CompilableLit)
      ?: error("Unknown token: $it")
    }
  }

  // compile relative branch into definition
  fun branch(label: str) {
    list += CompilableBranch(d.BRANCH_ptr, label)
  }

  // compile conditional relative branch into definition
  fun cbranch(label: str) {
    list += CompilableBranch(d.CBRANCH_ptr, label)
  }

  // compile raw 32-bit number into definition
  fun raw32(b: i32) {
    list += CompilableRaw(b)
  }

  fun label(name: str) {
    labels += name to curAddr()
  }

  fun curAddr(): i32 = list.fold(0) { acc, a -> acc + a.size() }

  fun compile(name: str) {
    d.createDefinition(name, d.DOCOL_ptr)
    list.forEach { it.compile(d) }
  }

  private interface Compilable {
    fun compile(d: Dictionary)
    fun size(): i32
  }

  private class CompilableWord(val word: i32) : Compilable {
    override fun compile(d: Dictionary) {
      d.append32(d.toCfa(word))
    }

    override fun size(): i32 = 4
  }

  private class CompilableRaw(val b: i32) : Compilable {
    override fun compile(d: Dictionary) {
      d.append32(b)
    }

    override fun size(): i32 = 4
  }

  private class CompilableLit(val b: i32) : Compilable {
    override fun compile(d: Dictionary) {
      d.append32(d.LIT_ptr)
      d.append32(b)
    }

    override fun size(): i32 = 8
  }

  private inner class CompilableBranch(val branchType: i32, val label: str) : Compilable {
    val thisPtr = curAddr()

    override fun compile(d: Dictionary) {
      d.append32(branchType)
      val l = labels[label] ?: error("Undefined label: $label")
      d.append32(l - thisPtr - 4)
    }

    override fun size(): i32 = 8
  }
}