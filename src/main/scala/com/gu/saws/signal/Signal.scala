package com.gu.saws.signal
import com.amazonaws.services.autoscaling.AmazonAutoScaling
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest
import com.amazonaws.services.cloudformation.model.{DescribeStackResourcesRequest, ResourceSignalStatus, SignalResourceRequest, StackResource}
import com.amazonaws.services.cloudformation.AmazonCloudFormation

import collection.JavaConverters._
import com.typesafe.scalalogging.StrictLogging

object Signal {

  def getSignaller(awsClient: AmazonCloudFormation, asgClient: AmazonAutoScaling, instanceId: String): Either[String, Signal] =
    try {
      for {
        stackResource <- findInstanceAutoscalingGroup(awsClient, asgClient, instanceId)
      } yield Signal(awsClient, stackResource, instanceId)
    } catch {
      case e: RuntimeException => Left(s"Failed: ${e.getMessage}")
    }

  private def findInstanceAutoscalingGroup(awsClient: AmazonCloudFormation, asgClient: AmazonAutoScaling,instanceId: String) = {
    awsClient.describeStackResources(
      new DescribeStackResourcesRequest().withPhysicalResourceId(instanceId)
    )
      .getStackResources.asScala.toList
      .filter(sr => sr.getResourceType.equals("AWS::AutoScaling::AutoScalingGroup"))
      .find(asgResource =>
        asgClient
          .describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(asgResource.getPhysicalResourceId))
          .getAutoScalingGroups.asScala
          .exists(asg =>
            asg
              .getInstances
              .asScala
              .map(i => i.getInstanceId)
              .contains(instanceId)
          )
      ) match {
      case Some(asg) => Right(asg)
      case None => Left(s"Could not find stack resource containing $instanceId")
    }
  }

}

case class Signal(awsClient: AmazonCloudFormation, stackResource: StackResource, instanceId: String) extends StrictLogging {

  def signal(success: Boolean): Either[String, String] = {
    try {
      for {
        result <- signalToASG(stackResource, success)
      } yield result
    } catch {
      case e: RuntimeException => Left(s"Failed: ${e.getMessage}")
    }
  }

  private def signalToASG(autoScalingGroup: StackResource, success: Boolean): Either[String, String] = {
    val signal = if (success) ResourceSignalStatus.SUCCESS else ResourceSignalStatus.FAILURE
    val stackName = autoScalingGroup.getStackName
    val asgLogicalResourceId = autoScalingGroup.getLogicalResourceId
    awsClient.signalResource(
      new SignalResourceRequest()
        .withStackName(stackName)
        .withLogicalResourceId(asgLogicalResourceId)
        .withUniqueId(instanceId)
        .withStatus(signal)
    )
    Right(s"$signal signal sent to $stackName/$asgLogicalResourceId")
  }

}
