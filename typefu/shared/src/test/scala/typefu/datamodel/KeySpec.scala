package typefu.datamodel

import typefu.TestSpecBase
import zio.test._
object KeySpec extends TestSpecBase {
  def spec = suite("Key Spec")(
    suite("Key")(),
    suite("Key#OfType")(
      testM("should consider keys with the same key value as equivalent") {
        check(Gen.alphaNumericString, Gen.anyInt) { (stringInput, intInput) =>
          val stringKey1     = Key(stringInput)
          val stringKey2     = Key(stringInput)
          val intKey1        = Key(intInput)
          val intKey2        = Key(intInput)
          val stringKeyOfInt = Key(intInput.toString())
          assertTrue(stringKey1 == stringKey2) && assertTrue(intKey1 == intKey2) && assertTrue(
            stringKeyOfInt != intKey1
          )
        }
      }
    )
  )
}
