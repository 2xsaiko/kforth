//package therealfarfetchd.kforth.light.terminal
//
//import org.jline.reader.LineReaderBuilder
//import org.jline.reader.MaskingCallback
//import org.jline.terminal.TerminalBuilder
//import org.jline.utils.InfoCmp
//import therealfarfetchd.kforth.light.*
//
//object SystemTerminal : Terminal {
//  private val st = TerminalBuilder.builder()
//    .jna(true)
//    .system(true)
//    .jansi(true)
//    .name("kforth")
//    .build()
//
//  private val lr = LineReaderBuilder.builder()
//    .terminal(st)
//    .appName("kforth")
//    .build()
//
//  private val input = st.reader()
//  private val output = st.output()
//
//  private var lineCache = ""
//
//  fun init() {
//    st.enterRawMode()
//    st.echo(false)
//  }
//
//  fun deinit() {
//    input.close()
//    lr.readLine()
//    st.close()
//  }
//
//  override fun width(): i32 = st.width
//
//  override fun height(): i32 = st.height
//
//  override fun read(): u16? = if (input.available() > 0) readBlocking() else null
//
//  override fun readBlocking(): u16 = input.read().c
//
//  override fun readLine(len: i32): str {
//    val lc = lineCache
//    write('\r')
//    return lr.readLine(lc, null, cbmask(len), null).run { slice(0 until minOf(length, len)) }
//  }
//
//  override fun write(c: u16) {
//    output.write(c.i)
//    output.flush()
//    lineCache += c
//    if (c in "\n\r") lineCache = ""
//  }
//
//  override fun clear() {
//    st.puts(InfoCmp.Capability.clear_screen)
//  }
//
//  private fun cbmask(maxlen: i32) = object : MaskingCallback {
//    override fun history(line: str): str = line.slice(0 until minOf(line.length, maxlen))
//    override fun display(line: str): str = history(line) + if (line.length > maxlen) " <…>" else ""
//  }
//}