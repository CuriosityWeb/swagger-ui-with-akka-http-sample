package CuriosityWeb.service

import CuriosityWeb.repo.UserRepo
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

trait Service {

  protected val log: Logger = LoggerFactory.getLogger(getClass)

  def routes: Route
}

object Service {

  def routes(userRepo: UserRepo)(implicit system: ActorSystem[_], ec: ExecutionContext): Route = path("") {
    getFromResource("swagger/index.html")
  } ~ getFromResourceDirectory("swagger") ~
    SwaggerService.routes ~
    new UserService(userRepo).routes ~
    new HealthCheckService().routes
}
