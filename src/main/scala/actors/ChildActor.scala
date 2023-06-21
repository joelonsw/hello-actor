package actors

import actors.ChildActor.WordCounterMaster.{Initialize, WordCountTask}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.collection.mutable

object ChildActor extends App {
    object WordCounterMaster {
        case class Initialize(nChildren: Int)

        case class WordCountTask(text: String)

        case class WordCountReply(count: Int)
    }

    class WordCounterMaster extends Actor {

        import WordCounterMaster._

        var childActors: mutable.Queue[ActorRef] = null;

        override def receive: Receive = {
            case Initialize(nChildren) =>
                if (childActors == null) {
                    val actorsSeq = (1 to nChildren).map(n => context.actorOf(Props[WordCounterWorker], s"wcw$n"))
                    childActors = mutable.Queue(actorsSeq: _*)
                } else {
                    println("[WCM] already initialized!")
                }
            case WordCountTask(text) => {
                val childActor: (ActorRef) = childActors.dequeue()
                childActor ! WordCountTask(text)
                childActors.enqueue(childActor)
            }
            case WordCountReply(reply) => {
                println(s"[WCM] received reply: ${reply}")
            }
        }
    }

    class WordCounterWorker extends Actor {

        import WordCounterMaster._

        override def receive: Receive = {
            case WordCountTask(text) => sender() ! {
                val length = text.split(" ").length
                println(s"[WCW] ${self.path} counted $length")
                sender() ! WordCountReply(length)
            }
        }
    }

    val system: ActorSystem = ActorSystem("ExerciseActorSystem")
    val wordCounterMaster = system.actorOf(Props[WordCounterMaster], "wcm")

    wordCounterMaster ! Initialize(5)
    wordCounterMaster ! WordCountTask("Hello World!")
    wordCounterMaster ! WordCountTask("Hello Joel Joel!")
    wordCounterMaster ! WordCountTask("joelonsw")
    wordCounterMaster ! WordCountTask("jsjsj jsjsj sjsjs sd")
}
