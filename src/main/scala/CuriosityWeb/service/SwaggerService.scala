package CuriosityWeb.service

import CuriosityWeb.AppConfig._
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info

object SwaggerService extends SwaggerHttpService with Service {

  override lazy val apiClasses: Set[Class[_]] = Set(classOf[UserService], classOf[HealthCheckService])

  override lazy val host: String = s"$httpHost:$httpPort"

  override lazy val info: Info =
    Info(title = "Swagger Ui With Akka Http",
      version = "1.0.0",
      description = "A simple example of implementing swagger ui with Akka http")
}
