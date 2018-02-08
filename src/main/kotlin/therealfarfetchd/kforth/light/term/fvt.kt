//package therealfarfetchd.kforth.light
//
//import therealfarfetchd.minivt.MiniVT
//
//object VirtualTerminal : Terminal {
//  val vt = MiniVT("kforth", scale = 2)
//
//  override fun attach() {
//    vt.show()
//  }
//
//  override fun detach() {
//    vt.hide()
//  }
//
//  override fun width(): i32 = vt.columns
//
//  override fun height(): i32 = vt.rows
//
//  override fun read(): u16? {
//    attach()
//    return null
//  }
//
//  override fun readBlocking(): u16 {
//    attach()
//    Thread.sleep(100000000000L)
//    TODO("not implemented")
//  }
//
//  override fun readLine(len: i32): str {
//    attach()
//    Thread.sleep(100000000000L)
//    TODO("not implemented")
//  }
//
//  override fun write(c: u16) {
//    attach()
//
//  }
//
//  override fun clear() {
//    attach()
//
//  }
//}