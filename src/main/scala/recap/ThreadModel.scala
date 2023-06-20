package recap

object ThreadModel extends App {

    // [우리가 아는 쓰레드 모델에서의 한계점]
    /*
    *   #1. OOP의 캡슐화는 싱글 쓰레드 환경에서만 의미가 있다
    *   - 쓰레드가 언제 종료되는지 알기 어렵다
    *   - Race Condition에 대한 디버깅은 정말 빡세다
    *
    *   #2. 돌아가고 있는 쓰레드에게 새로운 일을 시키는 것은 어렵다
    *   - wait()
    *       - synchronized block 안에서 락을 풀고 대기 상태로 쓰레드를 전환
    *       - 다른 쓰레드가 notify()를 쳐서 같은 객체를 사용하도록 wake up 할 수 있도록 대기
    *   - notify()
    *       - 하나의 쓰레드가 notify 해주면 해당 객체를 기다리던 쓰레드가 wake up 하도록 함
    *
    *   #3. 멀티 쓰레딩/분산화 앱에서 에러 처리 진짜 개빡세
    * */

    var task: Option[Runnable] = None;

    val runningThread: Thread = new Thread(() => {
        while (true) {
            while (task.isEmpty) {
                runningThread.synchronized {
                    println("[background] waiting for task....")
                    runningThread.wait()
                }
            }

            task.synchronized {
                println("[background] finally task!")
                task.get.run()
                task = None
            }
        }
    })

    private def delegateTask(runnable: Runnable): Unit = {
        if (task.isEmpty) {
            task = Some(runnable)
            runningThread.synchronized {
                runningThread.notify()
            }
        }
    }

    runningThread.start()
    Thread.sleep(1000)
    delegateTask(() => println("This is Joel #1"))
    Thread.sleep(1000)
    delegateTask(() => println("This is Joel #2"))

    runningThread.interrupt()
}
