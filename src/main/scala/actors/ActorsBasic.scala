package actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsBasic extends App {

    /*
    * [ACTOR의 구동 방식]
    * - Actor는
    *   - id와 queue가 있고요
    *   - 내부 상태 (internal state)
    *   - message handler가 있어요
    *
    * - Actor가 메시지 받으면요
    *   - 메시지가 actor의 메일 박스에 들어가고요
    *   - 액터는 수행되는 것이 스케줄링 되어 있어요
    *   - 쓰레드가 메일박스에서 메시지 꺼내고요
    *   - 쓰레드가 액터의 `receive` 메시지 핸들러를 호출 시켜요
    *   - 수행되고 나면 액터가 스케줄링에서 빠져나가요
    *
    * - 그에 따라 액터는!
    *   - 내부 상태가 바뀔수도 있고
    *   - 다른 액터에게 메시지를 전송할 수도 있어요
    * */

    // 1. ActorSystem 만들기
    val actorSystem = ActorSystem("actorBasic")
    println(actorSystem.name)

    // 2. Actor 만들기
    class WordCountActor extends Actor {
        private var totalWords = 0

        override def receive: PartialFunction[Any, Unit] = {
            case message: String => {
                println(s"[WordCountActor] $message")
                totalWords += message.split(" ").length
            }
            case _ => println("[WordCountActor] I don't understand!!")
        }
    }

    // 3. Actor 객체화 (참고로 액터는 new로 객체화 못시켜! ActorSystem을 거칩시다)
    val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")

    // 4. Actor에게 메시지 전달
    wordCounter ! "hello actor world!"

    // 5. Actor 상속한 클래스에 생성자 매개변수 주고 싶은데요? => 그러면 Companion Object로 Props 만들어서 ActorSystem에 넘겨줍시다
    //  - Props 안에서는 new 생성자 쓸 수 있어요!
    object PersonActor {
        def props(name: String) = Props(new PersonActor(name))
    }

    class PersonActor(name: String) extends Actor {
        override def receive: Receive = {
            case "hello" => println(s"[PersonActor] my name is $name")
        }
    }

    val personActor = actorSystem.actorOf(PersonActor.props("Joel"))
    personActor ! "hello"
}
