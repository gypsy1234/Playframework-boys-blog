package dao

import models.Post
import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject
import java.util.UUID
import java.sql.Timestamp
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.JdbcProfile

class PostDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val Posts = TableQuery[BlogPostsTable]


  def insert(posts: Post): Future[Unit] = db.run(Posts += posts).map { _ => () }

  def update(post: Post): Future[Unit] = db.run(Posts.filter(_.id === post.id).update(post)).map { _ => () }

  def delete(id: UUID): Future[Unit] = db.run(Posts.filter(_.id === id).delete).map { _ => () }

  def all(): Future[Seq[Post]] = db.run(Posts.result)

  def findById(id: UUID): Future[Option[Post]] = db.run(Posts.filter(x => x.id === id).result headOption)

  def fetchPublishedTake(takeNum: Int): Future[Seq[Post]] = db.run(Posts.filter(x => !x.isDraft).sortBy(x => x.updated_date.ifNull(x.created_date).desc).take(takeNum).result)

  def findByUserId(userId: String): Future[Seq[Post]] = db.run(Posts.filter(x => x.create_user_id === userId).sortBy(x => x.updated_date.ifNull(x.created_date).desc).result)


  private class BlogPostsTable(tag: Tag) extends Table[Post](tag, "blog_posts") {

    def id = column[java.util.UUID]("id", O.PrimaryKey)
    def create_user_id = column[String]("create_user_id")
    def create_user_fk = {
      val app = new GuiceApplicationBuilder().build
      val userDAO = app.injector.instanceOf[UserDAO]
      foreignKey("create_user_fk", create_user_id, userDAO.Users)(_. id, onDelete = ForeignKeyAction.Cascade)
    }
    def title = column[String]("title")
    def body = column[String]("body")
    def isDraft = column[Boolean]("is_draft")
    def created_date = column[Timestamp]("created_date")
    def updated_date = column[Option[Timestamp]]("updated_date")

    def * = (id, create_user_id, title, body, isDraft, created_date, updated_date) <> (Post.tupled, Post.unapply)
  }

}