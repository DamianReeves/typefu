package typefu

package object datamodel {
  type KeyT[T] = Key {
    type KeyType = T
    type Target <: Any
  }

  type StringKeyOn[Target] = KeyOn[String, Target]

  type SingletonKey = {
    type KeyType <: Singleton
  }
}
