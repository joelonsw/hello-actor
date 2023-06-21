package actors

import akka.actor.{Actor, ActorSystem, Props}

object ChangeBehavior extends App {

    // Refactor to stateless Actor!
    object Counter {
        case object Increment

        case object Decrement

        case object Print
    }

    class Counter extends Actor {

        import Counter._

        override def receive: Receive = countReceive(0)

        def countReceive(count: Int): Receive = {
            case Increment =>
                println(s"[currentCount] incrementing")
                context.become(countReceive(count + 1))
            case Decrement =>
                println(s"[currentCount] decrementing")
                context.become(countReceive(count - 1))
            case Print =>
                println(s"[currentCount] $count")
        }
    }

    import Counter._

    val system: ActorSystem = ActorSystem("ExerciseActorSystem")
    val counter = system.actorOf(Props[Counter])

    (1 to 5).foreach(_ => counter ! Increment)
    (1 to 3).foreach(_ => counter ! Decrement)
    counter ! Print
}
