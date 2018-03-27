package com.gu.saws.signal
import com.amazonaws.services.autoscaling.AmazonAutoScaling
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest
import com.amazonaws.services.cloudformation.model.{DescribeStackResourcesRequest, ResourceSignalStatus, SignalResourceRequest, StackResource}
import com.amazonaws.services.cloudformation.AmazonCloudFormation

import collection.JavaConverters._
import com.typesafe.scalalogging.StrictLogging

case class Signal(awsClient: AmazonCloudFormation, asgClient: AmazonAutoScaling, instanceId: String) extends StrictLogging {

  def signal(success: Boolean): Either[String, String] = {
    try {
      for {
        stackResource <- findInstanceAutoscalingGroup
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

  private def findInstanceAutoscalingGroup = {
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
