package typefu

import zio.test.DefaultRunnableSpec
import zio.prelude._
import zio.test.TestAspect
import java.time.Duration
abstract class TestSpecBase extends DefaultRunnableSpec with Assertions {
  override def aspects = List(TestAspect.timeout(Duration.ofSeconds(60)))
}
