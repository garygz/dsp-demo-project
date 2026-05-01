resource "aws_dynamodb_table" "impressions" {
  name         = var.dynamodb_impressions_table
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "campaignId"
  range_key    = "timestamp"

  attribute {
    name = "campaignId"
    type = "S"
  }

  attribute {
    name = "timestamp"
    type = "S"
  }

  stream_enabled   = true
  stream_view_type = "NEW_IMAGE"
}

resource "aws_dynamodb_table" "clicks" {
  name         = var.dynamodb_clicks_table
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "campaignId"
  range_key    = "timestamp"

  attribute {
    name = "campaignId"
    type = "S"
  }

  attribute {
    name = "timestamp"
    type = "S"
  }

  stream_enabled   = true
  stream_view_type = "NEW_IMAGE"
}
