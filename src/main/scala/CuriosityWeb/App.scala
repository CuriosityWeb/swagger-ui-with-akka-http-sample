package CuriosityWeb

import CuriosityWeb.AppConfig._
import CuriosityWeb.repo.UserRepo
import CuriosityWeb.service.Service
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

object App extends scala.App {

  private val log = LoggerFactory.getLogger(getClass)

  implicit val system: ActorSystem[_] = ActorSystem[Nothing](Behaviors.empty, "CuriosityWeb", config)

  import system.executionContext

  val userRepo: UserRepo = UserRepo()

  Http().newServerAt(httpHost, httpPort).bind(Service.routes(userRepo))
    .onComplete {
      case Success(binding) =>
        log.info(s"Service started successfully: $binding")
      case Failure(exception) =>
        log.error("Failed to start service", exception)
        system.terminate()
    }
}
