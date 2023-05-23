package CuriosityWeb.model

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

object JsonFormat {

  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat3(User)
}
