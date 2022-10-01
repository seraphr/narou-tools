package jp.seraphr.narou.commands.collect

import com.dropbox.core.oauth.DbxCredential

object DropboxApp {
  val appKey       = "4gpor2ahiidljm7"
  val appSecret    = System.getenv("NAROU_TOOL_DROPBOX_APP_SECRET")
  val refreshToken = System.getenv("NAROU_TOOL_DROPBOX_REFRESH_TOKEN")

  def newCredential() = new DbxCredential("", 0, refreshToken, appKey, appSecret)
}
