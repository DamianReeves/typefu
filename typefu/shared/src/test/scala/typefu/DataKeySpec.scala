package typefu

import zio.test._
import zio.test.Assertion._
object DataKeySpec extends DefaultRunnableSpec {
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
