variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name (used in resource names and tags)"
  type        = string
  default     = "production"
}

variable "app_name" {
  description = "Application name — used as a prefix for all resource names"
  type        = string
  default     = "dsp-demo"
}

# ── Networking ────────────────────────────────────────────────────────────────

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

# ── ECS ───────────────────────────────────────────────────────────────────────

variable "container_port" {
  description = "Port the Spring Boot container listens on"
  type        = number
  default     = 8080
}

variable "ecs_task_cpu" {
  description = "ECS Fargate task CPU units (256 / 512 / 1024 / 2048 / 4096)"
  type        = number
  default     = 512
}

variable "ecs_task_memory" {
  description = "ECS Fargate task memory in MB"
  type        = number
  default     = 1024
}

variable "ecs_desired_count" {
  description = "Number of ECS task replicas"
  type        = number
  default     = 1
}

# ── RDS ───────────────────────────────────────────────────────────────────────

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "dspdemo"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "dspadmin"
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB (free tier includes 20 GB)"
  type        = number
  default     = 20
}

# ── DynamoDB ──────────────────────────────────────────────────────────────────

variable "dynamodb_impressions_table" {
  description = "DynamoDB table name for impression events"
  type        = string
  default     = "dsp-demo-impressions"
}

variable "dynamodb_clicks_table" {
  description = "DynamoDB table name for click events"
  type        = string
  default     = "dsp-demo-clicks"
}

# ── CI/CD ─────────────────────────────────────────────────────────────────────

variable "github_owner" {
  description = "GitHub organisation or username that owns the repository"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
  default     = "dsp-demo-project"
}

variable "github_branch" {
  description = "Branch to build and deploy"
  type        = string
  default     = "main"
}

# ── Lambda ────────────────────────────────────────────────────────────────────

variable "aggregator_tumbling_window_seconds" {
  description = "DynamoDB stream tumbling window in seconds for the aggregator Lambda"
  type        = number
  default     = 60
}
