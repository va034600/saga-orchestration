#!/bin/bash
set -e

echo "Initializing AWS resources in LocalStack..."

# Create SQS queue
echo "Creating SQS queue: compensation-queue"
awslocal sqs create-queue --queue-name compensation-queue
echo "SQS queue created."

# Create EventBridge event bus
echo "Creating EventBridge bus: saga-events"
awslocal events create-event-bus --name saga-events
echo "EventBridge bus created."

# Create EventBridge rule on saga-events bus
echo "Creating EventBridge rule: compensation-rule"
awslocal events put-rule \
  --name compensation-rule \
  --event-bus-name saga-events \
  --event-pattern '{
    "source": ["saga.orchestrator"],
    "detail-type": ["CompensationRequested"]
  }'
echo "EventBridge rule created."

# Get the SQS queue ARN
QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/compensation-queue \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

echo "SQS queue ARN: ${QUEUE_ARN}"

# Create target for the rule pointing to the SQS queue
echo "Adding SQS target to compensation-rule"
awslocal events put-targets \
  --rule compensation-rule \
  --event-bus-name saga-events \
  --targets "Id=compensation-queue-target,Arn=${QUEUE_ARN}"
echo "Target added to rule."

# Create Step Functions state machine
echo "Creating Step Functions state machine: order-saga"
STATE_MACHINE_ARN=$(awslocal stepfunctions create-state-machine \
  --name order-saga \
  --definition "$(cat /etc/localstack/init/ready.d/state-machine.json)" \
  --role-arn "arn:aws:iam::000000000000:role/stepfunctions-role" \
  --query 'stateMachineArn' \
  --output text)
echo "State machine created: ${STATE_MACHINE_ARN}"

echo "All AWS resources initialized successfully."
