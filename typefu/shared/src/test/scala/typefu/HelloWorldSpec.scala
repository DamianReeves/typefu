package typefu

import zio._
import zio.test.Assertion._
import zio.test._
import zio.test.environment._

object HelloWorld {
  def sayHello =
    console.putStrLn("Hello, World!")
}

object HelloWorldSpec extends DefaultRunnableSpec {

  import HelloWorld._

  def spec: ZSpec[Environment, Failure] = suite("HelloWorldSpec")(
    testM("sayHello correctly displays output") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assert(output)(equalTo(Vector("Hello, World!\n")))
    }
  )
}
