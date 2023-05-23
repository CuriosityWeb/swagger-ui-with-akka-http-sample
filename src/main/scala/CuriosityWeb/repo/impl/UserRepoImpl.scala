package CuriosityWeb.repo.impl

import CuriosityWeb.AppConfig.dbConfig
import CuriosityWeb.model.User
import CuriosityWeb.repo.UserRepo
import akka.Done
import slick.lifted.ProvenShape

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

private[repo] object UserRepoImpl {

  import dbConfig.profile.api._

  private val TableName: String = "USERS"
  private val CreateTableTimeout: Duration = FiniteDuration(30, TimeUnit.SECONDS)

  private final class Schema(tag: Tag) extends Table[User](tag, TableName) {

    def id: Rep[String] = column[String]("ID", O.PrimaryKey)

    def name: Rep[String] = column[String]("NAME")

    def mobile: Rep[Option[String]] = column[Option[String]]("mobile")


    override def * : ProvenShape[User] = (id, name, mobile) <> (User.tupled, User.unapply)
  }

  private val query: TableQuery[Schema] = TableQuery(new Schema(_))

}

final class UserRepoImpl private[repo](implicit ec: ExecutionContext)
  extends UserRepo {

  import UserRepoImpl._
  import dbConfig.db
  import dbConfig.profile.api._

  Await.result(db.run(query.schema.createIfNotExists), CreateTableTimeout)

  override def addUser(user: User): Future[Done] =
    db.run(query += user).map(_ => Done)

  override def getUser(id: String): Future[Option[User]] =
    db.run(query.filter(_.id === id).result.headOption)

  override def getAllUsers: Future[Set[User]] =
    db.run(query.result).map(_.toSet)

  override def getAllUserIds: Future[Set[String]] =
    db.run(query.map(_.id).result).map(_.toSet)

  override def removeUser(id: String): Future[Done] =
    db.run(query.filter(_.id === id).delete).map(_ => Done)

  override def updateName(id: String, name: String): Future[Done] =
    db.run(query.filter(_.id === id).map(_.name).update(name)).map(_ => Done)

  override def removeMobile(id: String): Future[Done] =
    db.run(query.filter(_.id === id).map(_.mobile).update(None)).map(_ => Done)

  override def updateMobile(id: String, mobile: String): Future[Done] =
    db.run(query.filter(_.id === id).map(_.mobile).update(Option(mobile))).map(_ => Done)
}
