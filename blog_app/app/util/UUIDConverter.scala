package util

import java.util.UUID


object UUIDConverter {

  def convertOrDefault(s: String, default: UUID ): UUID = {
    try {
      UUID.fromString(s)
    } catch {
      case ex: IllegalArgumentException => default
    }
  }

}

