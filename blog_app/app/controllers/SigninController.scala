package controllers

import dao.UserDAO
import models.User
import javax.inject._
import org.springframework.security.crypto.bcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import util._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SigninForm {
  case class UserInfo(id: String, pass: String)
}

@Singleton
class SigninForm @Inject()(userDAO: UserDAO) {

  import SigninForm._

  val form = Form(
    mapping(
      "id" -> text.verifying("IDは英数4～30字で入力してください", {_.matches("""\w{4,30}""")}),
      "pass" -> text.verifying("パスワードは英数8～30字で入力してください", {_.matches("""\w{8,30}""")})
    )(UserInfo.apply)(UserInfo.unapply).verifying("パスワードが一致しません", fields => fields match {
      case userInfo => validate(userInfo.id, userInfo.pass).isDefined
    })
  )

  def validate(id: String, pass: String):Option[User] = {
    Await.result(userDAO.findById(id), Duration.Inf) match {
      case Some(u) if BCrypt.checkpw(pass, u.pass) => Some(u)
      case _ => None
    }
  }
}

@Singleton
class SigninController @Inject()(cc: MessagesControllerComponents,
                                  signinForm: SigninForm) extends MessagesAbstractController(cc) {

  import SigninForm._

  private val postUrl = routes.SigninController.signin()

  def show() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.signin(signinForm.form, postUrl))
  }

  def signin() = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[UserInfo] =>
      BadRequest(views.html.signin(formWithErrors, postUrl))
    }

    val successFunction = { uInfo: UserInfo =>
      val sessionInfo = SessionManager.create(uInfo.id)
      Redirect(routes.HomeController.index()).withSession("id" -> sessionInfo.getSessionId)
    }

    val formValidationResult = signinForm.form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def signout() = Action { implicit request: Request[AnyContent] =>
    SessionManager.remove
    Redirect(routes.HomeController.index()).withNewSession
  }

}


