package therealfarfetchd.kforth.light

import kotlin.experimental.and

// Rust, boys!

internal typealias bool = Boolean
internal typealias u16 = Char
internal typealias i8 = Byte
internal typealias i16 = Short
internal typealias i32 = Int
internal typealias i64 = Long
internal typealias d32 = Float
internal typealias d64 = Double
internal typealias str = String

val i8.v get() = this != 0.b
val i8.s get() = toShort() and 0xFF
val i8.c get() = s.c
val i8.i get() = toInt() and 0xFF
val i8.l get() = toLong() and 0xFF

val i16.v get() = this != 0.s
val i16.b get() = toByte()
val i16.c get() = toChar()
val i16.i get() = toInt() and 0xFFFF
val i16.l get() = toLong() and 0xFFFF

val u16.v get() = this != 0.c
val u16.b get() = toByte()
val u16.s get() = toShort()
val u16.i get() = toInt()
val u16.l get() = toLong()

val i32.v get() = this != 0
val i32.b get() = toByte()
val i32.s get() = toShort()
val i32.c get() = toChar()
val i32.l get() = toLong() and 0xFFFFFFFF

val i64.v get() = this != 0L
val i64.b get() = toByte()
val i64.s get() = toShort()
val i64.c get() = toChar()
val i64.i get() = toInt()

infix fun i8.shl(n: i32) = (i shl n).b
infix fun i8.shr(n: i32) = (i shr n).b
infix fun i8.ushr(n: i32) = (i ushr n).b

infix fun i16.shl(n: i32) = (i shl n).s
infix fun i16.shr(n: i32) = (i shr n).s
infix fun i16.ushr(n: i32) = (i ushr n).s

infix fun u16.and(other: u16): u16 = (this.toInt() and other.toInt()).toChar()
infix fun u16.or(other: u16): u16 = (this.toInt() or other.toInt()).toChar()
infix fun u16.xor(other: u16): u16 = (this.toInt() xor other.toInt()).toChar()
fun u16.inv(): u16 = (this.toInt().inv()).toChar()
infix fun u16.shl(n: i32) = (i shl n).c
infix fun u16.shr(n: i32) = (i shr n).c
infix fun u16.ushr(n: i32) = (i ushr n).c

fun i8.asBytes() = listOf(this)

fun i16.asBytes() = listOf(
  this.b,
  (this shr 8).b
)

fun u16.asBytes() = listOf(
  this.b,
  (this shr 8).b
)

fun i32.asBytes() = listOf(
  this.b,
  (this shr 8).b,
  (this shr 16).b,
  (this shr 24).b
)

fun i64.asBytes() = listOf(
  this.b,
  (this shr 8).b,
  (this shr 16).b,
  (this shr 24).b,
  (this shr 32).b,
  (this shr 40).b,
  (this shr 48).b,
  (this shr 56).b
)

infix fun i32.umod(i: i32): i32 = (this % i).let { if (it < 0) it + i else it }
infix fun i64.umod(i: i32): i32 = (this % i).let { if (it < 0) (it + i).toInt() else it.toInt() }
infix fun i64.umod(l: i64): i64 = (this % l).let { if (it < 0) it + l else it }