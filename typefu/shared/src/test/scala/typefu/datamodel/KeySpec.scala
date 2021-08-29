package typefu.datamodel

import typefu.TestSpecBase
import zio.test._
object KeySpec extends TestSpecBase {
  def spec = suite("Key Spec")(
    suite("Key")(),
    suite("Key#OfType")(
      testM("should support consider keys with the same key value as equivalent") {
        check(Gen.alphaNumericString) { input =>
          val key1 = Key(input)
          val key2 = Key(input)
          assertTrue(key1 == key2)
        }
      }
    )
  )
}
