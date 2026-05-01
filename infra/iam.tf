# ── ECS Execution Role ────────────────────────────────────────────────────────
# Used by ECS agent to pull ECR images and write CloudWatch logs.

resource "aws_iam_role" "ecs_execution" {
  name = "${local.prefix}-ecs-execution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution_managed" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_execution_ssm" {
  name = "ssm-secrets"
  role = aws_iam_role.ecs_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = ["ssm:GetParameters", "secretsmanager:GetSecretValue"]
      Resource = [
        aws_ssm_parameter.db_username.arn,
        aws_ssm_parameter.db_password.arn,
        aws_secretsmanager_secret.jwt.arn,
      ]
    }]
  })
}

# ── ECS Task Role ─────────────────────────────────────────────────────────────
# Used by the Spring Boot application process itself.

resource "aws_iam_role" "ecs_task" {
  name = "${local.prefix}-ecs-task"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "ecs_task_dynamodb" {
  name = "dynamodb"
  role = aws_iam_role.ecs_task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:Query",
        "dynamodb:Scan",
        "dynamodb:BatchWriteItem",
      ]
      Resource = [
        aws_dynamodb_table.impressions.arn,
        aws_dynamodb_table.clicks.arn,
      ]
    }]
  })
}

resource "aws_iam_role_policy" "ecs_task_secrets" {
  name = "secrets"
  role = aws_iam_role.ecs_task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue"]
      Resource = [aws_secretsmanager_secret.jwt.arn]
    }]
  })
}

# ── Lambda Aggregator Role ────────────────────────────────────────────────────
# Reads DynamoDB streams, writes S3, writes CloudWatch logs.

resource "aws_iam_role" "lambda_aggregator" {
  name = "${local.prefix}-lambda-aggregator"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_aggregator_basic" {
  role       = aws_iam_role.lambda_aggregator.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy" "lambda_aggregator_streams" {
  name = "dynamodb-streams"
  role = aws_iam_role.lambda_aggregator.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "dynamodb:GetRecords",
        "dynamodb:GetShardIterator",
        "dynamodb:DescribeStream",
        "dynamodb:ListStreams",
      ]
      Resource = [
        aws_dynamodb_table.impressions.stream_arn,
        aws_dynamodb_table.clicks.stream_arn,
      ]
    }]
  })
}

resource "aws_iam_role_policy" "lambda_aggregator_s3" {
  name = "s3-write"
  role = aws_iam_role.lambda_aggregator.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:PutObject"]
      Resource = "${aws_s3_bucket.aggregations.arn}/*"
    }]
  })
}

# ── Lambda Loader Role ────────────────────────────────────────────────────────
# Reads/deletes S3 objects, accesses RDS via VPC, writes CloudWatch logs.

resource "aws_iam_role" "lambda_loader" {
  name = "${local.prefix}-lambda-loader"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

# VPC execution role includes ENI permissions needed for VPC Lambda
resource "aws_iam_role_policy_attachment" "lambda_loader_vpc" {
  role       = aws_iam_role.lambda_loader.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

resource "aws_iam_role_policy" "lambda_loader_s3" {
  name = "s3-read-delete"
  role = aws_iam_role.lambda_loader.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:GetObject", "s3:DeleteObject"]
      Resource = "${aws_s3_bucket.aggregations.arn}/*"
    }]
  })
}

resource "aws_iam_role_policy" "lambda_loader_ssm" {
  name = "ssm-db-credentials"
  role = aws_iam_role.lambda_loader.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["ssm:GetParameter"]
      Resource = [
        aws_ssm_parameter.db_username.arn,
        aws_ssm_parameter.db_password.arn,
      ]
    }]
  })
}
