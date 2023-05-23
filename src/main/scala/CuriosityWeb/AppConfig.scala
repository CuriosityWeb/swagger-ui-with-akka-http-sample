package CuriosityWeb

import com.typesafe.config.{Config, ConfigFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object AppConfig {

  val config: Config = ConfigFactory.load()

  lazy val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("database", config)

  lazy val httpHost: String = config.getString("http.host")
  lazy val httpPort: Int = config.getInt("http.port")
}
