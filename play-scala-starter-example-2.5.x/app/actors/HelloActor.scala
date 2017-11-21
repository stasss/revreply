package actors

import akka.actor.Actor

/**
  * Created by s_stashkevich on 11/21/2017.
  */
import akka.actor._

object HelloActor {
  def props = Props[HelloActor]
  case class SayHello(name: String)
}

class HelloActor extends Actor {
  import HelloActor._

  def receive = {
    case SayHello(name: String) =>
      sender() ! "Hello, " + name
  }
}