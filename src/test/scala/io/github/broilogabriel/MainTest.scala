package io.github.broilogabriel

import org.scalatest.funspec.AnyFunSpec

class MainTest extends AnyFunSpec {

  describe("sayHello") {
    it("should greet World") {
      assertResult("Hello World!")(Main.sayHello("World"))
    }
  }
}
