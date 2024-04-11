package io.github.broilogabriel

object Main {

  def sayHello(name: String) = s"Hello $name!"

  def main(args: Array[String]): Unit = println(sayHello("World"))

}
