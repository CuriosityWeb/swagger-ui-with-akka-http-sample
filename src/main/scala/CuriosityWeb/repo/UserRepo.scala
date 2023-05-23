package CuriosityWeb.repo

import CuriosityWeb.model.User
import CuriosityWeb.repo.impl.UserRepoImpl
import akka.Done

import scala.concurrent.{ExecutionContext, Future}

trait UserRepo extends Repo {

  def addUser(user: User): Future[Done]

  def getUser(id: String): Future[Option[User]]

  def getAllUsers: Future[Set[User]]

  def getAllUserIds: Future[Set[String]]

  def removeUser(id: String): Future[Done]

  def updateName(id: String, name: String): Future[Done]

  def removeMobile(id: String): Future[Done]

  def updateMobile(id: String, mobile: String): Future[Done]
}

object UserRepo {

  def apply()(implicit ec: ExecutionContext): UserRepo = new UserRepoImpl
}
