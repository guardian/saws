package com.gu.saws.checks
import java.net.URL
import java.net.HttpURLConnection
import com.gu.saws._
import com.typesafe.scalalogging.StrictLogging

case class Http200 (
  protocol: Protocol,
  host: Host,
  port: Port,
  path: Path,
  individualTimeout: IndividualTimeout) extends StrictLogging {

  private val qualifiedPath = if (path.path.startsWith("/")) path.path else "/" + path.path
  private val urlString = protocol.protocol + "://" + host.host + ":" + port.port + qualifiedPath

  def check(attempts: Attempts, sleep: Sleep): Boolean = {
    check(1, attempts, sleep)
  }

  def check(attempt: Int, attempts: Attempts, sleep: Sleep): Boolean = {
    logger.info(s"Executing check $attempt of ${attempts.count} checks against $urlString")
    wait(sleep)
    check || (attempt < attempts.count && check(attempt + 1, attempts, sleep))
  }

  private def wait(sleep: Sleep): Unit = {
    logger.debug(s"Sleeping for ${sleep.duration} seconds")
    Thread.sleep(sleep.duration * 1000)
  }

  def check: Boolean = {
    val url = new URL(urlString)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    try {
      connection.connect()
      val responseCode = connection.getResponseCode
      connection.disconnect()
      logger.info(s"Response code was $responseCode")
      responseCode == 200
    } catch {
      case e: Exception =>
        logger.info(e.getMessage)
        false
    }
  }
}

