package dao

import models.User
import scala.concurrent.{ ExecutionContext, Future }
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

class UserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private [dao] val Users = TableQuery[UsersTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  def insert(user: User): Future[Unit] = db.run(Users += user).map { _ => () }

  def findById(id: String):Future[Option[User]] = db.run(Users.filter(x => x.id === id).result.headOption)

  def deleteById(id: String): Future[Unit] = db.run(Users.filter(_.id === id).delete).map { _ => () }

  private[dao] class UsersTable(tag: Tag) extends Table[User](tag, "users") {

    def id = column[String]("id",O.PrimaryKey)
    def name = column[Option[String]]("name")
    def pass = column[String]("pass")

    def * = (id, name, pass) <> (User.tupled, User.unapply)
  }
}