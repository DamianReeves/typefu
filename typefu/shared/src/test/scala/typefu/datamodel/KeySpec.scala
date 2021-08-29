package typefu.datamodel

import typefu.TestSpecBase
import zio.test._
import zio.test.Assertion._
object KeySpec extends TestSpecBase {
  def spec = suite("Key Spec")(
    suite("Key")(
      testM("should consider keys with the same key value but different targets, different") {
        checkM(Gen.anyInt) { givenInt =>
          val key1 = Key.on[Widget](givenInt)
          val key2 = Key.on[Sprocket](givenInt)
          for {
            typeCheckResult <- typeCheck("key1 === key1")
          } yield assert(typeCheckResult)(isRight) && assertTrue(key1.value == key2.value)
        }
      } @@ TestAspect.ignore /* Need to figure out how to compare these */,
      suite("when its a KeyT")(
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
  )

  final case class Widget()
  final case class Sprocket()
}
