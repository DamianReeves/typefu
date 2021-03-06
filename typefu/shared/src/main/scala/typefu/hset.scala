package typefu

import scala.annotation.implicitNotFound
import scala.language.implicitConversions

object hset extends App {

  type ??? = Nothing

  // Set(1, 2, 3) Set of ints
  // Set("Adam", "Kit") Set of strings
  // "Adam" :*: 1 :*: true :*: HNil

  // HSet
  // Type level set
  // Design goals:
  // 1. We should only ever be able to have one instance of a type in the set CHECK
  // 2. Should be commutative and associative HALF CHECK
  // 3. We should be able to look up a value in the set CHECK
  // 4. We should be able to remove a value from the set CHECK
  // 5. We should be able to take the union of two sets CHECK
  // 6. We should be able to add values to the set CHECK

  // 1 :*: true :*: HNil // okay
  // 1 :*: 2 :*: true :*: HNil // not okay

  // 1 :*: true :*: HNil === true :*: 1 :*: HNil
  // set.get[Int]

  // Has[Logging] with Has[Database] with Has[Monitoring]
  // Has[Database] with Has[Monitoring] with Has[Logging] with Has[Database]

  sealed trait HSet { self =>
    def collectIntoList[A](f: PartialFunction[Any, A]): List[A] = self match {
      case HEmpty        => List.empty[A]
      case head :&: tail =>
        if (f.isDefinedAt(head)) f(head) :: tail.collectIntoList(f)
        else tail.collectIntoList(f)
    }

    def collectIntoSet[A](f: PartialFunction[Any, A]): Set[A] = self match {
      case HEmpty        => Set.empty[A]
      case head :&: tail =>
        if (f.isDefinedAt(head)) Set(f(head)) ++ tail.collectIntoSet(f)
        else tail.collectIntoSet(f)
    }
  }

  object HSet {
    implicit def identity[A, B, C, Set <: HSet](
      set: Set
    )(implicit ev1: Includes[A, Set], ev2: Includes[B, Set], ev3: Includes[C, Set]): A :&: B :&: C :&: HEmpty =
      ev1(set) :&: ev2(set) :&: ev3(set) :&: HEmpty
  }

  case object HEmpty extends HSet { self =>
    def :&:[Head](head: Head): Head :&: HEmpty =
      hset.:&:(head, self)

    def union[That <: HSet](that: That): That =
      that
  }

  type HEmpty = HEmpty.type

  final case class :&:[Head, Tail <: HSet](head: Head, tail: Tail) extends HSet { self =>
    def :&:[That](that: That)(implicit eliminate: Eliminate[That, Head :&: Tail]): That :&: eliminate.Out =
      hset.:&:(that, eliminate(self))

    def get[Element](implicit includes: Includes[Element, Head :&: Tail]): Element =
      includes(self)

    def union[That <: HSet](that: That)(implicit union: Union[Head :&: Tail, That]): union.Out =
      union(self, that)
  }

  // Element = type to elemiminate from set
  // Set = original set before elimination
  // Out = resulting set after elimination
  // Eliminate[Int, Int :&: String :&: HEmpty] === String :&: HEmpty
  // Eliminate[Int, String :&: Boolean :&: HEmpty] === String :&: Boolean :&: HEmpty

  trait Eliminate[Element, Set <: HSet] {
    type Out <: HSet
    def apply(set: Set): Out
  }

  object Eliminate extends EliminateLowPriority {
    // Sometimes, unfortunately "Aux"
    type WithOut[Element, Set <: HSet, Out0 <: HSet] = Eliminate[Element, Set] { type Out = Out0 }

    implicit def empty[Head]: Eliminate.WithOut[Head, HEmpty, HEmpty] =
      new Eliminate[Head, HEmpty] {
        type Out = HEmpty
        def apply(set: HEmpty.type): Out =
          set
      }

    implicit def eliminate[Head, Tail <: HSet]: Eliminate.WithOut[Head, Head :&: Tail, Tail] =
      new Eliminate[Head, Head :&: Tail] {
        type Out = Tail
        def apply(set: Head :&: Tail): Out =
          set.tail
      }
  }

  trait EliminateLowPriority {
    implicit def recurse[Element, Head, Tail <: HSet](implicit
      eliminate: Eliminate[Element, Tail]
    ): Eliminate.WithOut[Element, Head :&: Tail, Head :&: eliminate.Out] =
      new Eliminate[Element, Head :&: Tail] {
        type Out = Head :&: eliminate.Out

        def apply(set: Head :&: Tail): Head :&: eliminate.Out =
          hset.:&:(set.head, eliminate(set.tail))
      }
  }

  @implicitNotFound("${Element} was not in ${Set}")
  trait Includes[Element, Set <: HSet] {
    def apply(set: Set): Element
  }

  object Includes extends IncludesLowPriority {
    implicit def includes[Head, Tail <: HSet]: Includes[Head, Head :&: Tail] =
      new Includes[Head, Head :&: Tail] {
        def apply(set: Head :&: Tail): Head =
          set.head
      }

    def includesValue[A](element: A, list: List[A]): A =
      list match {
        case Nil                             => throw new Error(s"${element} was not in ${list}")
        case head :: next if head == element => head
        case head :: tail                    => includesValue(element, tail)
      }
  }

  trait IncludesLowPriority {
    implicit def recursive[Element, Head, Tail <: HSet](implicit
      includes: Includes[Element, Tail]
    ): Includes[Element, Head :&: Tail] =
      new Includes[Element, Head :&: Tail] {

        def apply(set: Head :&: Tail): Element =
          includes(set.tail)
      }
  }

  trait Union[Left <: HSet, Right <: HSet] {
    type Out <: HSet
    def apply(left: Left, right: Right): Out
  }

  object Union extends UnionLowPriority {
    type WithOut[Left <: HSet, Right <: HSet, Out0] = Union[Left, Right] { type Out = Out0 }

    implicit def leftEmpty[Right <: HSet]: Union.WithOut[HEmpty, Right, Right] =
      new Union[HEmpty, Right] {
        type Out = Right
        def apply(left: HEmpty.type, right: Right): Out =
          right
      }

    implicit def removeAndRecurse[LeftHead, LeftTail <: HSet, Right <: HSet](implicit
      includes: Includes[LeftHead, Right],
      union: Union[LeftTail, Right]
    ): Union.WithOut[LeftHead :&: LeftTail, Right, union.Out] =
      new Union[LeftHead :&: LeftTail, Right] {
        type Out = union.Out
        def apply(left: LeftHead :&: LeftTail, right: Right): union.Out =
          union(left.tail, right)
      }

    def unionValue[A](left: List[A], right: List[A]): List[A] =
      (left, right) match {
        case (Nil, right)                  => right
        case (left, Nil)                   => left
        case (leftHead :: leftTail, right) =>
          if (right.contains(leftHead))
            unionValue(leftTail, right)
          else
            leftHead :: unionValue(leftTail, right)
      }
  }

  trait UnionLowPriority {

    implicit def recurse[LeftHead, LeftTail <: HSet, RightHead, RightTail <: HSet](implicit
      union: Union[LeftTail, RightHead :&: RightTail]
    ): Union.WithOut[LeftHead :&: LeftTail, RightHead :&: RightTail, LeftHead :&: union.Out] =
      new Union[LeftHead :&: LeftTail, RightHead :&: RightTail] {
        type Out = LeftHead :&: union.Out
        def apply(left: LeftHead :&: LeftTail, right: RightHead :&: RightTail): LeftHead :&: union.Out =
          hset.:&:(left.head, union(left.tail, right))
      }

    implicit def rightEmpty[Left <: HSet]: Union.WithOut[Left, HEmpty, Left] =
      new Union[Left, HEmpty] {
        type Out = Left
        def apply(left: Left, right: HEmpty.type): Left =
          left
      }
  }

  object Example {

    def main(args: Array[String]): Unit = {
      val set: String :&: Boolean :&: Int :&: HEmpty.type =
        "Kit" :&: true :&: "Adam" :&: 1 :&: HEmpty

      val set2 = List(1) :&: List("Adam") :&: List(10) :&: true :&: HEmpty

      val myString = set.get[String]
      // val myDouble = set.get[Double]
      println(s"myString: $myString")

      val sameSet =
        "Kit" :&: true :&: "Adam" :&: 1 :&: HEmpty

      val union1 = set union set
      val union2 = set union set2
    }
  }

  trait ZIO[-R, +E, +A]

  implicit final class HSetOps[R <: HSet, E, A](private val self: ZIO[R, E, A]) extends AnyVal {
    def flatMap[R1 <: HSet, E1, B](f: A => ZIO[R1, E1, B])(implicit union: Union[R, R1]): ZIO[union.Out, E1, B] =
      ???
    def provideSome[R1](r1: R1)(implicit eliminate: Eliminate[R1, R]): ZIO[eliminate.Out, E, A]                 =
      ???
  }

  trait Database
  trait Logging
  trait Monitoring

  val sampleDatabase: Database = ???

  val zio1: ZIO[Database :&: Logging :&: HEmpty, Nothing, Int] = ???

  val zio2: ZIO[Logging :&: Monitoring :&: HEmpty, Nothing, String] = ???

  val zio3: ZIO[Database :&: Logging :&: Monitoring :&: HEmpty, Nothing, String] =
    zio1.flatMap(_ => zio2)

  val zio4: ZIO[Logging :&: Monitoring :&: HEmpty.type, Nothing, String] =
    zio3.provideSome(sampleDatabase)
}

object Example2 {
  import typefu.hset._
  def main(args: Array[String]): Unit = {
    val foo: "foo" with Singleton = "foo"
    val bar: "bar" with Singleton = "bar"
    val set                       =
      bar :&: foo :&: "Kit" :&: true :&: "Adam" :&: 1 :&: HEmpty

    val set2 = List(1) :&: List("Adam") :&: List(10) :&: true :&: HEmpty

    val myString = set.get[String]
    // val myDouble = set.get[Double]
    println(s"myString: $myString")
    val allDem   = set.collectIntoList { case o => o.toString }.mkString(",")
    println(s"all dem: $allDem")

    val sameSet = "Kit" :&: true :&: "Adam" :&: 1 :&: HEmpty

    val union1 = set union set
    val union2 = set union set2
  }
}
