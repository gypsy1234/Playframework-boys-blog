package controllers

import dao.{PostDAO, UserDAO}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import javax.inject._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import play.api.mvc._

@Singleton
class UserPageController @Inject()(userDAO: UserDAO,
                                   blogPostDao: PostDAO,
                                   cc: ControllerComponents)
  extends AbstractController(cc) {

  def show(id: String) = Action { implicit request: Request[AnyContent] =>
    Await.result(userDAO.findById(id), Duration.Inf) match {
      case Some(u) =>
        val posts = Await.result(blogPostDao.findByUserId(id), Duration.Inf)
        val browser = JsoupBrowser()
        val post_planeText_seq = posts.map(p => (p, browser.parseString(p.body) >> allText))
        Ok(views.html.userpage(id, post_planeText_seq))
      case _ => NotFound(views.html.notFound())
    }

  }
}
