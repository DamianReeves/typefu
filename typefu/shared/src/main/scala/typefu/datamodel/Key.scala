package typefu.datamodel
import zio.prelude._
//import zio.=!=
//import typefu.hset.{HEmpty, HSet, :&:}
sealed trait Key { self =>
  type KeyType
  type Target

  final def =~=[B](that: KeyT[B])(implicit ev: B <:< KeyType, eq: Equal[KeyType] = Equal.default[KeyType]) =
    isEquivalentTo(that)

  final def isEquivalentTo[B](
    that: KeyT[B]
  )(implicit ev: B <:< KeyType, eq: Equal[KeyType] = Equal.default[KeyType]) =
    eq.equal(self.value, that.value)

  def map[B](f: KeyType => B): B KeyOn Target = KeyOn(f(value))

  def value: KeyType
  override def toString(): String = value.toString()

  final def withTarget[Target1]: KeyOn[KeyType, Target1] = KeyOn(value)
}

final case class KeyOn[K, T](value: K) extends Key {
  type KeyType = K
  type Target  = T
}

/// Make Key not polymorphic but with Type and Entity members

object Key {
  def apply[T](value: T): T KeyOn Any = KeyOn[T, Any](value)

  def on[Target]: OnPartiallyApplied[Target] = new OnPartiallyApplied[Target]

  final class OnPartiallyApplied[Target](val dummy: Boolean = false) extends AnyVal {
    def apply[K](value: K): KeyOn[K, Target] = KeyOn(value)
  }
}

// sealed trait Keys {
//   type Targets
//   type KeySet <: HSet
//   type Main

//   def keySet: KeySet
//   def main: Main
//   override def toString(): String = main.toString()
// }

// object Keys {
//   type For[Targets0] = Keys {
//     type Target = Targets0
//   }
//   object For {
//     def apply[Target]: ApplyPartiallyApplied[Target] = ???

//     final class ApplyPartiallyApplied[Target](val dummy: Boolean = false) extends AnyVal {
//       def apply[K](key: K): Keys.For[Target] = ???
//     }
//   }

//   val someKey = Keys.For[BigDecimal]("")

//   type StringKey = String with Singleton
//   type On[K, A]  = K

//   type Simple[Key] = DataKey {
//     type KeySet = Key :&: HEmpty
//     type Main   = Key
//   }

//   type Advanced[Main0, KeySet0] = DataKey {
//     type Main   = Main0
//     type KeySet = KeySet0
//   }

//   def on[Target]: OnPartiallyApplied[Target] = new OnPartiallyApplied[Target]

//   final case class Single[Target, Key <: StringKey](key: Key) extends Keys {
//     type KeySet = Main :&: HEmpty
//     type Main   = StringKey On Target

//     def keySet: KeySet = key :&: HEmpty
//     def main: Main     = key
//   }

//   // final case class Composite[Key1, Key2](left: Key1, right: Key2) extends DataKey {
//   //   type KeyTypes = (Key1)
//   // }

//   final class OnPartiallyApplied[Target](val dummy: Boolean = false) extends AnyVal {
//     def apply[Key <: StringKey](key: Key): DataKey.Simple[StringKey On Target] = Single[Target, Key](key)
//   }
// }
