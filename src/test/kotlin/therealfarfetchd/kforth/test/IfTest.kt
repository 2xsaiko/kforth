package therealfarfetchd.kforth.test

import org.junit.Assert
import org.junit.Test
import therealfarfetchd.kforth.light.Forth

class IfTest {

  @Test
  fun `test basic IF`() {
    val forth = Forth()

    forth.eval(": TEST  IF 1 ELSE 0 THEN  ;")

    forth.eval("TRUE TEST")
    Assert.assertEquals(forth.data.pop(), 1)

    forth.eval("FALSE TEST")
    Assert.assertEquals(forth.data.pop(), 0)
  }

}