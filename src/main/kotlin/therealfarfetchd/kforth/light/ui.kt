package therealfarfetchd.kforth.light

interface Terminal {
  fun attach() {}
  fun detach() {}
  fun width(): i32
  fun height(): i32
  fun read(): u16?
  fun readBlocking(): u16
  fun readLine(len: i32): str
  fun write(c: u16)
  fun clear()
}