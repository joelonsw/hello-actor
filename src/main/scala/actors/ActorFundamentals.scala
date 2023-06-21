package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorFundamentals extends App {

    object Counter {
        case class Increment(amount: Int)

        case class Decrement(amount: Int)

        case object Print
    }

    class Counter extends Actor {

        import Counter._

        var count = 0;

        override def receive: Receive = {
            case Increment(amount) => count += amount
            case Decrement(amount) => count -= amount
            case Print => println(s"[CounterActor] count : ${count}")
        }
    }

    import Counter._

    val system: ActorSystem = ActorSystem("ExerciseActorSystem")
    val counter: ActorRef = system.actorOf(Props[Counter], "counter")

    counter ! Increment(10)
    counter ! Decrement(5)
    counter ! Print

    object BankAccount {
        case object Success

        case object Failure

        case class Deposit(amount: Int)

        case class Withdraw(amount: Int)

        case object Statement
    }

    class BankAccount extends Actor {

        import actors.ActorFundamentals.BankAccount._

        var accountMoney = 0;

        override def receive: Receive = {
            case Deposit(amount) => {
                accountMoney += amount
                println(s"[BankAccountActor] sender ${sender()} deposited ${amount}")
                self ! Statement
                sender() ! Success
            }
            case Withdraw(amount) => {
                if (accountMoney >= amount) {
                    accountMoney -= amount
                    println(s"[BankAccountActor] sender ${sender()} withdraw ${amount}")
                    self ! Statement
                    sender() ! Success
                } else {
                    sender() ! Failure
                }
            }
            case Statement => println(s"[BankAccountActor] current Money: ${accountMoney}")
        }
    }

    object BankCustomer {
        case class DepositMoney(amount: Int, ref: ActorRef)

        case class WithdrawMoney(amount: Int, ref: ActorRef)

        case class CurrentMoney(ref: ActorRef)
    }

    class BankCustomer extends Actor {

        import actors.ActorFundamentals.BankAccount._
        import actors.ActorFundamentals.BankCustomer._

        override def receive: Receive = {
            case Success => println("[BankCustomerActor] Success!")
            case Failure => println("[BankCustomerActor] Failure!")
            case DepositMoney(amount, ref) => ref ! Deposit(amount)
            case WithdrawMoney(amount, ref) => ref ! Withdraw(amount)
            case CurrentMoney(ref) => ref ! Statement
        }
    }

    import actors.ActorFundamentals.BankCustomer._

    val bankAccount: ActorRef = system.actorOf(Props[BankAccount], "bankAccount")
    val bankCustomer: ActorRef = system.actorOf(Props[BankCustomer], "bankCustomer")

    bankCustomer ! DepositMoney(10000, bankAccount)
    bankCustomer ! CurrentMoney(bankAccount)
    bankCustomer ! WithdrawMoney(5000, bankAccount)
    bankCustomer ! WithdrawMoney(6000, bankAccount)
    bankCustomer ! CurrentMoney(bankAccount)
}
