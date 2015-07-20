package service

import java.util.concurrent.TimeUnit

import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{OneForOneStrategy, Props, Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.eny.actor.ReadPostActor
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike, FlatSpec}
import akka.pattern.pipe
import scala.concurrent.Future
import akka.pattern.ask
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Try, Failure}

class DummyReadActor extends Actor {

  override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = Duration.create(1, TimeUnit.SECONDS)) {
    case _: IllegalArgumentException => Escalate
  }

  override def receive: Receive = {
    case "load" => Future.successful("Load OK") pipeTo sender
    case "emulateException" => Future.successful(Try(throw new IllegalArgumentException("illegalstate"))) pipeTo sender
  }
}

class ActorBasedService(system:ActorSystem) {
  implicit val timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS))
  val read = system.actorOf(Props[DummyReadActor])

  def load(): Future[String] = (read ? "load").mapTo[String]

  def error(): Future[Try[String]] = (read ? "emulateException").mapTo[Try[String]]
}

class ServiceBaseTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with ScalaFutures {

  def this() = this(ActorSystem("ServiceBaseTest"))
  override def afterAll() = TestKit.shutdownActorSystem(system)

  "ServiceBase underlying actor" must {
    "pipe message as future" in {
      val service = new ActorBasedService(system)
      whenReady(service.load(), timeout(10 seconds)) {
        res => res should === ("Load OK")
      }
    }

    "pipe exception back to sender" in {
      val service = new ActorBasedService(system)
      whenReady(service.error(), timeout(10 seconds)) {
        res => res.isFailure should === (true)
      }
    }
  }
}
