resource "aws_s3_bucket" "aggregations" {
  # Suffix with account ID to ensure global uniqueness
  bucket = "${local.prefix}-aggregations-${local.account_id}"
}

resource "aws_s3_bucket_versioning" "aggregations" {
  bucket = aws_s3_bucket.aggregations.id
  versioning_configuration {
    status = "Disabled"
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "aggregations" {
  bucket = aws_s3_bucket.aggregations.id

  rule {
    id     = "expire-old-csv"
    status = "Enabled"

    filter {}

    expiration {
      days = 90
    }
  }
}

resource "aws_s3_bucket_public_access_block" "aggregations" {
  bucket                  = aws_s3_bucket.aggregations.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
