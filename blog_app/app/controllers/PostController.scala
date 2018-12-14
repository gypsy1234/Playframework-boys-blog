package controllers

import java.util.UUID

import dao.PostDAO
import javax.inject.{Inject, Singleton}
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

@Singleton
class PostController @Inject() (blogPostDao: PostDAO,
                                 cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc){

  def show(postId: String) = Action { implicit request: Request[AnyContent] =>
    try {
      val uuid = UUID.fromString(postId)
      Await.result(blogPostDao.findById(uuid), Duration.Inf) match {
        case Some(p) => Ok(views.html.detail(p))
        case _ => NotFound(<h1>Page not found</h1>)
      }
    } catch {
      case ex: IllegalArgumentException => NotFound(views.html.notFound())
    }
  }

}