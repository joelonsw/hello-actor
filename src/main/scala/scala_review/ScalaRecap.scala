package scala_review

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object ScalaRecap extends App {

    // FP
    val incrementer: Int => Int = x => x + 1
    val incremented: Int = incrementer(4)
    println(incremented)

    // HOF map, flatMap, filter
    val incrementList = List(1, 2, 3).map(incrementer)
    println(incrementList)

    val flatMapList = List(1, 2, 3).flatMap(x => List(x, x + 1, x + 2))
    println(flatMapList)

    val filteredList = List(1, 2, 3).filter(x => x % 2 == 0)
    println(filteredList)

    // for-comprehensions
    val forLoopTwice = for {
        num <- List(1, 2, 3)
        letter <- List('a', 'b', 'c')
    } yield (num.toString + letter)
    println(forLoopTwice)

    // options & try
    val anOption: Option[Int] = Option(20)
    val aNone = None
    val doubler: Int => Int = x => x * 2

    val doubleOption: Option[Int] = anOption.map(doubler)
    println(doubleOption)
    val doubleNone: Option[Int] = aNone.map(doubler)
    println(doubleNone)

    // Futures
    implicit val executionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))
    val aFuture = Future({
        Thread.sleep(2000)
        10
    })

    aFuture onComplete {
        case Success(value) => println(value)
        case Failure(exception) => println(exception)
    }

    // PartialFunction: 일부 x 에 대해서 y 인 함수 구현
    // 모든 것을 처리하지 않고 싶은 경우 PartialFunction 구현
    val divideByTwo: PartialFunction[Int, Int] = {
        case x if x % 2 == 0 => x / 2
    }

    val result1 = Try(divideByTwo(8))
    println(result1)
    val result2 = Try(divideByTwo(5))
    println(result2)

    // Implicit
    // 1. value
    implicit val joel: String = "hello joel!";

    def printlnHello(f: () => Unit)(implicit name: String) = {
        println(name)
        f()
    }

    printlnHello(() => println("Joel Hi!"))

    // 2. Extension Methods
    implicit class HashUtil(input: String) {
        def hash: String = "hashed" + input + "hashed"
    }

    val hashedInput = "Joel".hash
    println(hashedInput)

    // 3. conversions
    implicit def intToString(input: Int): String = {
        input.toString
    }

    val input: String = 1;
    println(input + 'a')

    Await.result(aFuture, Duration.Inf) // Wait for the Future to complete
    executionContext.shutdown() // Shut down the execution context
}
