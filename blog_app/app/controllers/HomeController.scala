package controllers

import dao.PostDAO
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import javax.inject._
import play.api.mvc._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

@Singleton
class HomeController @Inject()(blogPostDAO: PostDAO,
                                cc: ControllerComponents)
  extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    val posts = Await.result(blogPostDAO.fetchPublishedTake(9), Duration.Inf)
    val browser = JsoupBrowser()
    val post_planeText_seq = posts.map(p => (p, browser.parseString(p.body) >> allText))
    Ok(views.html.index(post_planeText_seq))
  }
}
