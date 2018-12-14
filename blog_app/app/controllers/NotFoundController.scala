package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class NotFoundController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {

  def show(any: String) = Action { implicit request: Request[AnyContent] =>
    NotFound(views.html.notFound())
  }
}