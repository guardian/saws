# SAWS: Signal AWS

A simple solution to the signalling requirement for ReplacingUpdate in Auto Scaling Groups.

Install this utility, and signal to cloudformation that the instance is successfully launched after a condition is met.

# Conditions

## No Check (`nocheck`)

Signal success immediately.  Can be used at the end of a cloud-init block.

## HTTP 200 (`http200`)

Signal success when a http 200 response code is received from a service.

# Permissions

  * cloudformation:DescribeStackResources
  * cloudformation:SignalResource
  * autoscaling:DescribeAutoScalingInstances
  * autoscaling:DescribeAutoScalingGroups

# Example Cloud Init Implementation

Add the following to the end of the UserData block.  Note that this installs the application
from an S3 bucket - we intend to publish this as a deb and/or and Amigo role.

```
   #Install
   aws s3 cp s3://security-dist/saws /root/saws
   /bin/chmod +x /root/saws
   # Invoke - check for a good service at http://localhost:9000/healthcheck
   # Sleep five seconds, then check.  Repeat up to ten times.
   /root/saws http200 -p 9000 -t 10 -s 5 -P healthcheck
```

# Cloudformation Additions

Note that the Creation Policy and Update Policy are at the same indent depth as the Type.
```
  AutoscalingGroup:
    Type: AWS::AutoScaling::AutoScalingGroup
    ...
    CreationPolicy:
      AutoScalingCreationPolicy:
        MinSuccessfulInstancesPercent: 100
      ResourceSignal:
        Timeout: PT10M
        Count: 2

  SignalAWSPolicy:
    Type: AWS::IAM::Policy
    Properties:
      PolicyName: security-hq-signal-aws-policy
      PolicyDocument:
        Statement:
        # minimal policy to instance to signal AWS autoscaling groups on success/failure
        - Effect: Allow
          Resource: "*"
          Action:
          - cloudformation:DescribeStackResources
          - cloudformation:SignalResource
          - autoscaling:DescribeAutoScalingInstances
          - autoscaling:DescribeAutoScalingGroups
      Roles:
       - !Ref InstanceRole
```
