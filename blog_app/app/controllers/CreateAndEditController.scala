package controllers

import models.Post
import dao.PostDAO
import util._
import javax.inject._
import java.util.UUID
import java.sql.Timestamp
import scala.concurrent._
import scala.concurrent.duration.Duration
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation._
import play.api.mvc._

object EditForm {

  case class Content(title: String, body: String, isDraft: Boolean)

  val form = Form(
    mapping(
      "title" -> text.verifying(titleCheckConstraint()),
      "body" -> text.verifying(bodyCheckConstraint()),
       "isDraft" -> boolean
    )(Content.apply)(Content.unapply)
  )

  def titleCheckConstraint(): Constraint[String] = Constraint("constraints.titlecheck")({
    plainText =>
      val errors = plainText match {
        case t: String if t.length == 0 => Seq(ValidationError("タイトルが入力されていません"))
        case t: String if 100 < t.getBytes.length => Seq(ValidationError("入力文字数が上限をこえています"))
        case _ => Nil
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  })

  def bodyCheckConstraint(): Constraint[String] = Constraint("constraints.bodycheck")({
    plainText =>
      val errors = plainText match {
        case t: String if t.length == 0 => Seq(ValidationError("本文が入力されていません"))
        case t: String if 30000 < t.getBytes.length => Seq(ValidationError("入力文字数が上限をこえています"))
        case _ => Nil
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  })

}

@Singleton
class CreateAndEditController @Inject()(postDAO: PostDAO,
                                        cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  import EditForm._

  private val MaximumNumberOfPosts = 20
  private val postUrlForCreate = routes.CreateAndEditController.create()

  def show_create() = Action { implicit request: MessagesRequest[AnyContent] =>
    SessionManager.get match {
      case Some(s) if Await.result(postDAO.findByUserId(s.getLoginId), Duration.Inf).size < MaximumNumberOfPosts =>
        Ok(views.html.createPost(form, postUrlForCreate))
      case Some(s) =>
        Ok(views.html.notice("投稿記事数が上限に達しているため、新たに記事を投稿することができません", "投稿記事数が上限に達しているため、新たに記事を投稿することができません", "新たに記事を作成される場合、既に作成した投稿を削除する必要があります。"))
      case _ => NotFound(views.html.notFound())
    }
  }

  def create(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>

    val errorFunction = { formWithError: Form[EditForm.Content] =>
      BadRequest(views.html.createPost(formWithError, postUrlForCreate))
    }

    val successFunction = { content: Content =>
      val postId = UUID.randomUUID
      SessionManager.get() match {
        case Some(s) =>
          val loginId = s.getLoginId
          val posts = Post(postId, loginId, content.title, content.body, content.isDraft, new Timestamp(System.currentTimeMillis()), null)
          postDAO.insert(posts)
          Redirect(routes.UserPageController.show(loginId))
        case _ =>
          Redirect(routes.HomeController.index())
      }
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def show_edit(postId: String) = Action { implicit request: MessagesRequest[AnyContent] =>

    val postUUID = UUIDConverter.convertOrDefault(postId, null)
    if (postUUID == null)
      NotFound(views.html.notFound())

    val optPost = Await.result(postDAO.findById(postUUID), Duration.Inf)
    val session = SessionManager.get()

    (optPost, session) match {
      case (Some(p), Some(s)) if p.createUserId == s.getLoginId=>
        val filledForm = EditForm.form.fill(Content(p.title, p.body, p.isDraft))
        Ok(views.html.editPost(filledForm, p))
      case _ => NotFound(views.html.notFound())
    }
  }

  def update(postId: String) = Action { implicit request: MessagesRequest[AnyContent] =>

    val postUUID = UUIDConverter.convertOrDefault(postId, null)
    if (postUUID == null)
      NotFound(views.html.notFound())

    val optPost = Await.result(postDAO.findById(postUUID), Duration.Inf)
    val session = SessionManager.get()

    val errorFunction = { formWithError: Form[EditForm.Content] =>
      optPost match {
        case Some(p) => BadRequest(views.html.editPost(formWithError, p))
        case _ => NotFound(views.html.notFound())
      }
    }

    val successFunction = {content: Content =>
      (optPost, session) match {
        case (Some(p), Some(s)) if p.createUserId == s.getLoginId =>
          val loginId = s.getLoginId
          val updatedPost = Post(p.id, loginId, content.title, content.body, content.isDraft, p.createdDate, Some(new Timestamp(System.currentTimeMillis())))
          Await.result(postDAO.update(updatedPost), Duration.Inf)
          Redirect(routes.UserPageController.show(loginId))
        case _ => NotFound(views.html.notFound())
      }
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def delete(postId: String) = Action { implicit request: Request[AnyContent] =>

    val postUUID = UUIDConverter.convertOrDefault(postId, null)
    if (postUUID == null)
      NotFound(views.html.notFound())

    val optPost = Await.result(postDAO.findById(postUUID), Duration.Inf)
    val session = SessionManager.get()

    (optPost, session) match {
      case (Some(p), Some(s)) if p.createUserId == s.getLoginId =>
        Await.result(postDAO.delete(p.id), Duration.Inf)
        Redirect(routes.UserPageController.show(s.getLoginId))
      case _ => NotFound(views.html.notFound())
    }
  }
}
