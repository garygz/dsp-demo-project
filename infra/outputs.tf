output "alb_dns_name" {
  description = "ALB DNS — point your browser or DNS CNAME here"
  value       = aws_lb.main.dns_name
}

output "ecr_repository_url" {
  description = "ECR URL — tag and push your Docker image here"
  value       = aws_ecr_repository.app.repository_url
}

output "rds_endpoint" {
  description = "PostgreSQL endpoint (host only)"
  value       = aws_db_instance.postgres.address
}

output "s3_aggregations_bucket" {
  description = "S3 bucket where the aggregator Lambda writes CSV files"
  value       = aws_s3_bucket.aggregations.bucket
}

output "dynamodb_impressions_table" {
  description = "DynamoDB impressions table name"
  value       = aws_dynamodb_table.impressions.name
}

output "dynamodb_clicks_table" {
  description = "DynamoDB clicks table name"
  value       = aws_dynamodb_table.clicks.name
}

output "ecs_cluster_name" {
  description = "ECS cluster name (for deployments)"
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "ECS service name (for deployments)"
  value       = aws_ecs_service.app.name
}
