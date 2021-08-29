package typefu

import zio.test._
object DataKeySpec extends TestSpecBase {
  def spec = suite("DataKey Spec")(
    suite("DataKey")(
      testM("should support creation with on") {
        check(Gen.anyString) { givenKey =>
          val dataKey = DataKey.on[Item](givenKey)
          assert(dataKey.main)(equalTo(givenKey)) &&
          assertTrue(dataKey.toString() == dataKey.main.toString())
        }
      }
    )
  )

  case class Item()
}
