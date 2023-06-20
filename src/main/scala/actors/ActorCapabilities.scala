package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

    /*
    * [ACTOR의 메세지]
    * - 처리할 수 있는 메시지는 Any 타입이에요, 다만!
    *   1. 메시지는 불변이여야 하고
    *   2. 메시지는 직렬화가 가능해야 해요
    *   3. case class, case object가 많이 쓰여요!
    *
    * - Actor는 자기 자신을 호출할 수 있어요
    *   - self를 써서 자기 자신을 호출하세요 (OOP의 this와 비슷)
    *   - 내 스스로에게 메시지 보내기 가능!
    *
    * - Actor는 sender를 호출할 수 있어요
    *   - context.sender()를 통해 보낸 사람을 호출하세요!
    *   - 보낸 사람에게 메시지 보내는 것도 가능!
    *
    * - Actor는 메시지 포워딩도 가능해요!
    *   - Original Sender를 유지하면서 포워딩을 해줄 수 있어요 forward
    * */

    class SimpleActor extends Actor {
        override def receive: Receive = {
            case "Reply" => {
                println("This is reply!")
                sender() ! "This is my reply"
            }
            case message: String => println(s"[SimpleActor] received string: $message")
            case number: Int => println(s"[SimpleActor] received number: $number")
            case SpecialMessage(content) => println(s"[SimpleActor] received SpecialMessage: $content")
            case SendMessageToMySelf(content) => {
                println(s"[SimpleActor] sending message to myself")
                self ! content
            }
            case SayHiTo(ref) => {
                println("Hi! I got a message from you!")
                ref ! "Reply"
            }
        }
    }

    val system = ActorSystem("ActorCapabilities")
    val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

    simpleActor ! "joel"
    simpleActor ! 27
    simpleActor ! SpecialMessage("SOMETHING SPECIAL")
    simpleActor ! SendMessageToMySelf("Hello myself")
    simpleActor ! "Reply" // Sender가 없는데 보내면 Dead Letter로 취급

    val joel = system.actorOf(Props[SimpleActor], "joel")
    val yewon = system.actorOf(Props[SimpleActor], "yewon")

    joel ! SayHiTo(yewon)
}

case class SpecialMessage(content: String)

case class SendMessageToMySelf(content: String)

case class SayHiTo(ref: ActorRef)