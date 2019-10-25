package therealfarfetchd.kforth.light

interface Terminal {
  fun attach() {}
  fun detach() {}
  fun width(): i32
  fun height(): i32
  fun read(): char?
  fun readBlocking(): char
  fun readLine(len: i32): str
  fun write(c: char)
  fun clear()
}