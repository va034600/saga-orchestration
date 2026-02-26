#!/bin/bash
set -e

export AWS_DEFAULT_REGION=ap-northeast-1

echo "Initializing AWS resources in LocalStack..."

# --- SQS ---
echo "Creating SQS queue: compensation-queue"
QUEUE_URL=$(awslocal sqs create-queue \
  --queue-name compensation-queue \
  --query 'QueueUrl' --output text)
echo "SQS queue created: ${QUEUE_URL}"

QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${QUEUE_URL}" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)
echo "SQS queue ARN: ${QUEUE_ARN}"

# --- EventBridge ---
echo "Creating EventBridge bus: saga-events"
awslocal events create-event-bus --name saga-events
echo "EventBridge bus created."

echo "Creating EventBridge rule: compensation-rule"
awslocal events put-rule \
  --name compensation-rule \
  --event-bus-name saga-events \
  --event-pattern '{
    "source": ["saga.orchestrator"],
    "detail-type": ["CompensationRequested"]
  }'
echo "EventBridge rule created."

echo "Adding SQS target to compensation-rule"
awslocal events put-targets \
  --rule compensation-rule \
  --event-bus-name saga-events \
  --targets "Id=compensation-queue-target,Arn=${QUEUE_ARN}"
echo "Target added to rule."

# --- Lambda (HTTP proxy for Step Functions) ---
echo "Creating Lambda function: saga-http-proxy"
LAMBDA_DIR=/etc/localstack/init/ready.d/lambda
python3 -c "
import zipfile
with zipfile.ZipFile('/tmp/saga-http-proxy.zip', 'w') as z:
    z.write('${LAMBDA_DIR}/http_proxy.py', 'http_proxy.py')
"
awslocal lambda create-function \
  --function-name saga-http-proxy \
  --runtime python3.12 \
  --handler http_proxy.handler \
  --zip-file fileb:///tmp/saga-http-proxy.zip \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --timeout 30
echo "Lambda function created."

# --- Step Functions ---
echo "Creating Step Functions state machine: order-saga"
STATE_MACHINE_ARN=$(awslocal stepfunctions create-state-machine \
  --name order-saga \
  --definition "$(cat /etc/localstack/init/ready.d/state-machine.json)" \
  --role-arn "arn:aws:iam::000000000000:role/stepfunctions-role" \
  --query 'stateMachineArn' --output text)
echo "State machine created: ${STATE_MACHINE_ARN}"

echo "All AWS resources initialized successfully."
