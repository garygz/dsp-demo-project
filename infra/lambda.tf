# ── Package Lambda functions ──────────────────────────────────────────────────
# Run `npm install --omit=dev` in each lambda directory before terraform apply.

data "archive_file" "aggregator" {
  type        = "zip"
  source_dir  = "${path.root}/../lambda/aggregator"
  output_path = "${path.root}/../lambda/aggregator.zip"
}

data "archive_file" "loader" {
  type        = "zip"
  source_dir  = "${path.root}/../lambda/loader"
  output_path = "${path.root}/../lambda/loader.zip"
}

# ── Aggregator Lambda ─────────────────────────────────────────────────────────
# Reads DynamoDB streams, aggregates counts per campaign in 1-min tumbling
# windows, and writes a gzipped CSV to S3.
# Does NOT need VPC — only accesses DynamoDB streams and S3 (both public APIs).

resource "aws_cloudwatch_log_group" "aggregator" {
  name              = "/aws/lambda/${local.prefix}-aggregator"
  retention_in_days = 14
}

resource "aws_lambda_function" "aggregator" {
  function_name    = "${local.prefix}-aggregator"
  role             = aws_iam_role.lambda_aggregator.arn
  runtime          = "nodejs20.x"
  handler          = "handler.handler"
  filename         = data.archive_file.aggregator.output_path
  source_code_hash = data.archive_file.aggregator.output_base64sha256
  timeout          = 60
  memory_size      = 256

  environment {
    variables = {
      S3_BUCKET  = aws_s3_bucket.aggregations.bucket
      AWS_REGION = var.aws_region
    }
  }

  depends_on = [aws_cloudwatch_log_group.aggregator]
}

# One event source mapping per DynamoDB table stream

resource "aws_lambda_event_source_mapping" "impressions_stream" {
  event_source_arn              = aws_dynamodb_table.impressions.stream_arn
  function_name                 = aws_lambda_function.aggregator.arn
  starting_position             = "LATEST"
  tumbling_window_in_seconds    = var.aggregator_tumbling_window_seconds
  bisect_batch_on_function_error = true
  maximum_retry_attempts        = 2
}

resource "aws_lambda_event_source_mapping" "clicks_stream" {
  event_source_arn              = aws_dynamodb_table.clicks.stream_arn
  function_name                 = aws_lambda_function.aggregator.arn
  starting_position             = "LATEST"
  tumbling_window_in_seconds    = var.aggregator_tumbling_window_seconds
  bisect_batch_on_function_error = true
  maximum_retry_attempts        = 2
}

# ── Loader Lambda ─────────────────────────────────────────────────────────────
# Triggered by S3 PUT, decompresses CSV, upserts into PostgreSQL.
# Runs inside the VPC to reach RDS in the private subnet.

resource "aws_cloudwatch_log_group" "loader" {
  name              = "/aws/lambda/${local.prefix}-loader"
  retention_in_days = 14
}

resource "aws_lambda_function" "loader" {
  function_name    = "${local.prefix}-loader"
  role             = aws_iam_role.lambda_loader.arn
  runtime          = "nodejs20.x"
  handler          = "handler.handler"
  filename         = data.archive_file.loader.output_path
  source_code_hash = data.archive_file.loader.output_base64sha256
  timeout          = 120
  memory_size      = 256

  vpc_config {
    subnet_ids         = aws_subnet.private[*].id
    security_group_ids = [aws_security_group.lambda_loader.id]
  }

  environment {
    variables = {
      S3_BUCKET   = aws_s3_bucket.aggregations.bucket
      PG_HOST     = aws_db_instance.postgres.address
      PG_PORT     = "5432"
      PG_DATABASE = var.db_name
      PG_SSL      = "true"
    }
  }

  # PG_USER and PG_PASSWORD are injected via SSM in iam.tf policy;
  # here we reference them as environment variables resolved at runtime.
  # For production, switch to secrets = [] with SSM valueFrom.

  depends_on = [aws_cloudwatch_log_group.loader]
}

# Allow S3 to invoke the loader Lambda

resource "aws_lambda_permission" "s3_invoke_loader" {
  statement_id  = "AllowS3Invoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.loader.function_name
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.aggregations.arn
}

resource "aws_s3_bucket_notification" "csv_put" {
  bucket = aws_s3_bucket.aggregations.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.loader.arn
    events              = ["s3:ObjectCreated:*"]
  }

  depends_on = [aws_lambda_permission.s3_invoke_loader]
}
