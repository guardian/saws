package com.gu.saws

import com.amazonaws.SdkClientException
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder
import com.amazonaws.util.EC2MetadataUtils
import com.gu.saws.ArgumentParser.argParser
import com.gu.saws.checks._
import com.gu.saws.signal.Signal
import com.typesafe.scalalogging.StrictLogging

object Main extends StrictLogging {

  def main(args: Array[String]): Unit = {
    val parsedArgs = argParser.parse(args, Arguments.empty())
    (for {
      _ <- checkArgs(parsedArgs)
      instanceId <- getInstanceId(parsedArgs)
      signaller <- getSignaller(parsedArgs, instanceId)
      signal <- check(parsedArgs)
      result <- signaller.signal(signal)
    } yield result) fold (fail, success)
  }

  private def fail(message: String): Unit = {
    logger.error(message)
    System.exit(1)
  }

  private def success(message: String): Unit = logger.info(message)

  private def getInstanceId(parsedArgs: Option[Arguments]): Either[String, String] = {
    parsedArgs match {
      case Some(Arguments(_, _, _, _, _, _, _, Some(instanceId), _)) => Right(instanceId)
      case _ => EC2MetadataUtils.getInstanceId match {
        case s: String => Right(s)
        case _ => Left("Failed: no instance id found from metadata utils")
      }
    }
  }

  private def checkArgs(parsedArgs: Option[Arguments]):Either[String, Unit] = {
    parsedArgs match {
      case Some(Arguments(_, _, Some(_), _, _, _, _, _, _)) => Right(Unit)
      case _ => Left("Aborting")
    }
  }

  private def check(parsedArgs: Option[Arguments]):Either[String, Boolean] = {
    parsedArgs match {
      case Some(Arguments(_, _, Some(NoCheckMode), _, _, _, _, _, _)) =>
        Right(NoCheck().check())
      case Some(Arguments(_, _, Some(Http200Mode), _, individualTimeout, sleep, attempts, _, Some(Http200Arguments(protocol, host, port, path, _)))) =>
        Right(Http200(protocol, host, port, path, individualTimeout).check(attempts, sleep))
      case _ => Left("Impossible application state: this should be enforced by the CLI parser.  Did not receive valid instructions.  Aborting")
    }
  }

  private def getSignaller(parsedArgs: Option[Arguments], instanceId: String): Either[String, Signal] = try {
    parsedArgs match {
      case Some(Arguments(Some(credentials), Some(region), _, _, _, _, _, _, _)) =>
        Signal.getSignaller(
          getCloudformationClient(region, credentials),
          getAutoscalingClient(region, credentials),
          instanceId)
      case Some(Arguments(None, None, _, _, _, _, _, _, _)) =>
        Signal.getSignaller(
          AmazonCloudFormationClientBuilder.defaultClient(),
          AmazonAutoScalingClientBuilder.defaultClient(),
          instanceId)
      case _ => Left("Did not receive valid credentials and region combination (must be both or neither)")
    }
  } catch {
    case e: SdkClientException => Left(e.getMessage)
  }

  private def getAutoscalingClient(region: String, credentials: String) = {
    val awsAutoscalingClientBuilder = AmazonAutoScalingClientBuilder.standard()
    awsAutoscalingClientBuilder.setRegion(region)
    awsAutoscalingClientBuilder.setCredentials(new ProfileCredentialsProvider(credentials))
    awsAutoscalingClientBuilder.build()
  }

  private def getCloudformationClient(region: String, credentials: String) = {
    val awsCloudformationClientBuilder: AmazonCloudFormationClientBuilder = AmazonCloudFormationClientBuilder.standard()
    awsCloudformationClientBuilder.setRegion(region)
    awsCloudformationClientBuilder.setCredentials(new ProfileCredentialsProvider(credentials))
    awsCloudformationClientBuilder.build()
  }

}
