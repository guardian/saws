package com.gu.saws

import scopt.OptionParser

object ArgumentParser {

  val argParser: OptionParser[Arguments] = new OptionParser[Arguments]("ssm") {

    cmd("nocheck")
      .action((_, c) => c.copy(mode = Some(NoCheckMode)))

    cmd("http200")
      .action((_, c) =>
        c
          .copy(mode = Some(Http200Mode))
          .copy(checkParameters = Some(Http200Arguments.empty())))
      .text("Check for an http response of 200 on the specified port")
      .children(
        opt[String]('p', "port").optional()
          .action((port, args) =>
            args.copy(checkParameters = Some(args.checkParameters.get.asInstanceOf[Http200Arguments].copy(port = Port(port.toInt)))))
          .text("The port to check"),
        opt[String]('h', "host").optional()
          .action((host, args) =>
            args.copy(checkParameters = Some(args.checkParameters.get.asInstanceOf[Http200Arguments].copy(host = Host(host)))))
          .text("The host to check"),
        opt[Unit]('S', "secure").optional()
          .action((_, args) =>
            args.copy(checkParameters = Some(args.checkParameters.get.asInstanceOf[Http200Arguments].copy(protocol = Protocol("https")).copy(port = Port(443)))))
          .text("Use secure https protocol (sets port to 443)"),
        opt[String]('P', "path").optional()
          .action((path, args) =>
            args.copy(checkParameters = Some(args.checkParameters.get.asInstanceOf[Http200Arguments].copy(path = Path(path)))))
          .text("The protocol to check"),
        opt[String]('C', "count").optional()
          .action((count, args) =>
            args.copy(checkParameters = Some(args.checkParameters.get.asInstanceOf[Http200Arguments].copy(count = count.toInt))))
          .text("The number of times to successfully check"),
      )

    opt[String]('t', "timeout").optional()
      .action((individualTimeout, args) => args.copy(individualTimeout = IndividualTimeout(individualTimeout.toInt)))
      .text("The timeout for each check")

    opt[String]('a', "attempts").optional()
      .action((attempts, args) => args.copy(attempts = Attempts(attempts.toInt)))
      .text("Maximum number of attempts")

    opt[String]('s', "sleep").optional()
      .action((sleep, args) => args.copy(sleep = Sleep(sleep.toInt)))
      .text("Sleep between attempts")

    opt[String]('c', "credentials").optional()
      .action((credentials, args) => args.copy(credentials = Some(credentials)))
      .text("Name of credentials stanza")

    opt[String]('r', "region").optional()
      .action((region, args) => args.copy(region = Some(region)))
      .text("Name of AWS region")

    opt[String]('i', "instance").optional()
      .action((instance, args) => args.copy(instance = Some(instance)))
      .text("Instance ID")

    checkConfig { args =>
      if (args.mode.isEmpty) Left("You must select a mode to use: http200|nocheck")
      else Right(())
    }
  }
}
