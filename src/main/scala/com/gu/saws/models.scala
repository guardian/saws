package com.gu.saws

trait CheckParameters

case class Arguments(
  credentials: Option[String],
  region: Option[String],
  mode: Option[SignalMode],
  totalTimeout: TotalTimeout,
  individualTimeout: IndividualTimeout,
  sleep: Sleep,
  attempts: Attempts,
  instance: Option[String],
  checkParameters: Option[CheckParameters]
)
case class Http200Arguments(
  protocol: Protocol,
  host: Host,
  port: Port,
  path: Path,
  count: Int
) extends CheckParameters

object Arguments {
  def empty(): Arguments =
    Arguments(
      None,
      None,
      None,
      TotalTimeout(120),
      IndividualTimeout(5),
      Sleep(1),
      Attempts(10),
      None,
      None)
}
object Http200Arguments {
  def empty(): Http200Arguments =
    Http200Arguments(
      Protocol("http"),
      Host("localhost"),
      Port(80),
      Path("/"),
      0)
}
case class Host(host: String)
case class Path(path: String)
case class Protocol(protocol: String)
case class Port(port: Int)
case class Sleep(duration: Int)
case class TotalTimeout(duration: Int)
case class IndividualTimeout(duration: Int)
case class Attempts(count: Int)

sealed trait SignalMode
case object NoCheckMode extends SignalMode
case object Http200Mode extends SignalMode
