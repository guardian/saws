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
