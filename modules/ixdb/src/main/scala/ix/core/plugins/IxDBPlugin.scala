package ix.core.plugins

import play.api.db.{DBApi, DBPlugin}
import play.api.{Application, Configuration, Logger}

import scala.util.{Success, Failure, Try}

class IxDBPlugin(app: Application) extends DBPlugin {

  lazy val databaseConfig = app.configuration.getConfig("db").getOrElse(Configuration.empty)

  override def enabled = app.configuration.getBoolean("hikari.enabled").getOrElse(true)

  private val ixDBApi: DBApi = new IxDBApi(databaseConfig, app.classloader)

  def api: DBApi = ixDBApi

  override def onStart() = Logger.info("Starting HikariCP connection pool...")

  override def onStop() {
    Logger.info("Stopping HikariCP connection pool...")
    ixDBApi.datasources.foreach {
      case (ds, name) => Try {
        ixDBApi.shutdownPool(ds)
      } match {
        case Success(r) => Logger.info(s"HikariCP connection pool [$name] was terminated")
        case Failure(t) => Logger.error(s"Was not able to shutdown the connection pool [$name]", t)
      }
    }
  }
}
