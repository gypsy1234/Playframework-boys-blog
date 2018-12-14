package util

import play.api.mvc._
import scala.language.postfixOps
import java.util.UUID

object SessionManager {

  private val UpperLimitOfRecords = 1000
  private var sessionTable: collection.mutable.Buffer[SessionInfo] = collection.mutable.ListBuffer[SessionInfo]()

  def get()(implicit request: RequestHeader): Option[SessionInfo] ={
    request.session.get("id") match {
      case Some(s) => sessionTable.find(_.getSessionId == s)
      case _ => None
    }
  }

  def create(id :String):SessionInfo = {
    if (UpperLimitOfRecords <= sessionTable.size)
      sessionTable trimStart 1
    val sessionId = UUID.randomUUID().toString
    val sessionInfo = new SessionInfo(sessionId, id)
    sessionTable += sessionInfo
    sessionInfo
  }

  def remove(implicit request: RequestHeader): Unit = {
    get match {
      case Some(s) => sessionTable -= s
      case _ =>
    }
  }

}

class SessionInfo(sessionid :String, loginId :String) {
  def getSessionId :String = sessionid
  def getLoginId :String = loginId
}

//case class SessionInfo(sessionId :String, loginId :String, hasLogin :Boolean = false){
//}