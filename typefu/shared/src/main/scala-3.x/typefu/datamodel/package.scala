package typefu

package object datamodel {
  type KeyT[T] = Key {
    type KeyType = T
    type Target <: Any
  }

}
