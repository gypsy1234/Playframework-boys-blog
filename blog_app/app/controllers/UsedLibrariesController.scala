package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class UsedLibrariesController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {

  def show() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.usedLibraries())
  }
}
