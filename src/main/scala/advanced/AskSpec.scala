package advanced

import advanced.AskSpec.AuthManager.{AUTH_FAILURE_NOT_FOUND, AUTH_FAILURE_PW_INCORRECT, AUTH_FAILURE_SYSTEM}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class AskSpec extends TestKit(ActorSystem("AskSpec"))
    with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

    override def afterAll(): Unit = {
        TestKit.shutdownActorSystem(system)
    }

    import AskSpec._

    "AuthManager" should {
        import AuthManager._

        "등록되지 않은 유저 테스트" in {
            val authManager = system.actorOf(Props[AuthManager])
            authManager ! Authenticate("joel", "jo")
            expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
        }

        "비밀번호가 다른 유저 테스트" in {
            val authManager = system.actorOf(Props[AuthManager])
            authManager ! RegisterUser("joel", "joel")
            authManager ! Authenticate("joel", "jo")
            expectMsg(AuthFailure(AUTH_FAILURE_PW_INCORRECT))
        }

        "비밀번호가 알맞은 유저 테스트" in {
            val authManager = system.actorOf(Props[AuthManager])
            authManager ! RegisterUser("joel", "jo")
            authManager ! Authenticate("joel", "jo")
            expectMsg(AuthSuccess)
        }
    }

    "PipedAuthManager" should {
        import AuthManager._

        "등록되지 않은 유저 테스트" in {
            val authManager = system.actorOf(Props[PipedAuthManager])
            authManager ! Authenticate("joel", "jo")
            expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
        }

        "비밀번호가 다른 유저 테스트" in {
            val authManager = system.actorOf(Props[PipedAuthManager])
            authManager ! RegisterUser("joel", "joel")
            authManager ! Authenticate("joel", "jo")
            expectMsg(AuthFailure(AUTH_FAILURE_PW_INCORRECT))
        }

        "비밀번호가 알맞은 유저 테스트" in {
            val authManager = system.actorOf(Props[PipedAuthManager])
            authManager ! RegisterUser("joel", "jo")
            authManager ! Authenticate("joel", "jo")
            expectMsg(AuthSuccess)
        }
    }
}

object AskSpec {
    case class Read(key: String)

    case class Write(key: String, value: String)

    class KVActor extends Actor with ActorLogging {
        override def receive: Receive = online(Map())

        private def online(kv: Map[String, String]): Receive = {
            case Read(key) =>
                log.info(s"[KVActor] trying to read the value at the key $key")
                sender() ! kv.get(key)
            case Write(key, value) =>
                log.info(s"[KVActor] writing the value $value for the key $key")
                context.become(online(kv + (key -> value)))
        }
    }


    case class RegisterUser(username: String, password: String)

    case class Authenticate(username: String, password: String)

    case class AuthFailure(message: String)

    case object AuthSuccess

    object AuthManager {
        val AUTH_FAILURE_NOT_FOUND = "user not found"
        val AUTH_FAILURE_PW_INCORRECT = "pw incorrect"
        val AUTH_FAILURE_SYSTEM = "System fail"
    }

    class AuthManager extends Actor with ActorLogging {

        implicit val timeout: Timeout = Timeout(1.second)
        implicit val exeuctionContext: ExecutionContextExecutor = context.dispatcher

        val authDb = context.actorOf(Props[KVActor])

        override def receive: Receive = {
            case RegisterUser(username, password) => authDb ! Write(username, password)
            case Authenticate(username, password) => handleAuthentication(username, password)
        }

        def handleAuthentication(username: String, password: String) = {
            // ******* Actor 안의 Future에서는 인스턴스나 변하는 상태 끌고 못 들어가! *******
            // 애초에 쓰레드가 달라지니까 this.sender() 이런거 참조해봤자 못찾아 누가 누군지...
            // 변수로 땨로 빼서 관리하는 방법 추천
            val originalSender = sender()
            val future = authDb ? Read(username)
            future.onComplete {
                case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
                case Success(Some(dbPassword)) =>
                    if (dbPassword == password) originalSender ! AuthSuccess
                    else originalSender ! AuthFailure(AUTH_FAILURE_PW_INCORRECT)
                case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
            }
        }
    }

    class PipedAuthManager extends AuthManager {

        import AuthManager._

        override def handleAuthentication(username: String, password: String): Unit = {
            val future = authDb ? Read(username)
            val passwordFuture = future.mapTo[Option[String]]
            val responseFuture: Future[Any] = passwordFuture.map {
                case None => AuthFailure(AUTH_FAILURE_NOT_FOUND)
                case Some(dbPassword) =>
                    if (dbPassword == password) AuthSuccess
                    else AuthFailure(AUTH_FAILURE_PW_INCORRECT)
            }
            // Future가 완료되면, 그 어떤 response가 되었던 actorRef에게 보내줌!
            responseFuture.pipeTo(sender())
        }
    }
}