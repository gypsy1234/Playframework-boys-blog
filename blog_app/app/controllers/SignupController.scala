package controllers

import dao.UserDAO
import javax.inject._
import models.User
import org.springframework.security.crypto.bcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.db.slick._
import play.api.mvc._
import slick.jdbc.JdbcProfile
import util.SessionManager

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


object CredentForm {
  case class CredentInfo(id: String, pass: String)
}

@Singleton
class CredentForm @Inject() (userDAO: UserDAO) {

  import CredentForm._

  val form = Form(
    mapping(
      "id" -> text.verifying(idCheckConstraint).verifying("IDは英数4～30字で入力してください",{_.matches("""\w{4,30}""")}),
      "pass" -> text.verifying("パスワードは英数8～30字で入力してください",{_.matches("""\w{8,30}""")})
    )(CredentInfo.apply)(CredentInfo.unapply)
  )

  def idCheckConstraint: Constraint[String] = {
    Constraint("constraints.idcheck")({
      plainText =>
        Await.result(userDAO.findById(plainText), Duration.Inf) match {
          case Some(u) => Invalid(Seq(ValidationError("そのユーザーIDは既に使用されています")))
          case _ => Valid
        }
    })
  }
}

@Singleton
class SignupController @Inject()(userDAO: UserDAO,
                                 credentForm: CredentForm,
                                 protected val dbConfigProvider: DatabaseConfigProvider,
                                 cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {

  import CredentForm._

  private val postUrl = routes.SignupController.createUser()

  def show() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.signup(credentForm.form, postUrl))
  }

  def createUser() :Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[CredentInfo] =>
      BadRequest(views.html.signup(formWithErrors, postUrl))
    }

    val successFunction = { cInfo: CredentInfo =>
      val pwHash = BCrypt.hashpw(cInfo.pass, BCrypt.gensalt())
      val user = User(cInfo.id, None, pwHash)
      userDAO.insert(user)
      val sessionInfo = SessionManager.create(cInfo.id)
      Redirect(routes.HomeController.index()).withSession("id" -> sessionInfo.getSessionId)
    }

    val formValidationResult = credentForm.form.bindFromRequest
    Future {
      formValidationResult.fold(errorFunction, successFunction)
    }
  }

  def show_accountDelConfirmation() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.accountDelConfirmation())
  }

  def deleteUser() = Action { implicit request: MessagesRequest[AnyContent] =>
    SessionManager.get match {
      case Some(s) =>
        userDAO.deleteById(s.getLoginId)
        SessionManager.remove
        Redirect(routes.HomeController.index()).withNewSession
      case _ => NotFound(views.html.notFound())
    }
  }

}
