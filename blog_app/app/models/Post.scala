package models

import java.sql.Timestamp
import java.util.UUID

case class Post(id: UUID, createUserId: String, title: String, body: String, isDraft:Boolean, createdDate: Timestamp, updatedDate: Option[Timestamp])