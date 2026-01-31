object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")

    def square(x: Int): Int = x * x  // pure
    println(square(2))
    
    def tripleAll(ints: List[Int]): List[Int] = ints match {
      case Nil => Nil
      case x :: xs => x*3 :: tripleAll(xs)
    }
    
    println(tripleAll(List(1,2,3,4,5,6)))
    
  }
}