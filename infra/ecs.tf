resource "aws_ecs_cluster" "main" {
  name = local.prefix

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.prefix}"
  retention_in_days = 14
}

resource "aws_ecs_task_definition" "app" {
  family                   = local.prefix
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = var.app_name
    image     = "${aws_ecr_repository.app.repository_url}:latest"
    essential = true

    portMappings = [
      { containerPort = var.container_port, protocol = "tcp" },
      { containerPort = 8081,               protocol = "tcp" }
    ]

    environment = [
      { name = "SPRING_PROFILES_ACTIVE",        value = "prod" },
      { name = "AWS_REGION",                    value = var.aws_region },
      { name = "SPRING_DATASOURCE_URL",         value = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/${var.db_name}" },
      { name = "AUTH_JWT_SECRET_NAME",          value = aws_secretsmanager_secret.jwt.name },
      { name = "DYNAMODB_IMPRESSIONS_TABLE",    value = aws_dynamodb_table.impressions.name },
      { name = "DYNAMODB_CLICKS_TABLE",         value = aws_dynamodb_table.clicks.name },
    ]

    secrets = [
      { name = "SPRING_DATASOURCE_USERNAME", valueFrom = aws_ssm_parameter.db_username.arn },
      { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = aws_ssm_parameter.db_password.arn },
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

resource "aws_ecs_service" "app" {
  name                               = local.prefix
  cluster                            = aws_ecs_cluster.main.id
  task_definition                    = aws_ecs_task_definition.app.arn
  desired_count                      = var.ecs_desired_count
  launch_type                        = "FARGATE"
  health_check_grace_period_seconds  = 120

  network_configuration {
    subnets         = aws_subnet.private[*].id
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = var.app_name
    container_port   = var.container_port
  }

  depends_on = [aws_lb_listener.http]

  lifecycle {
    # Allow external deployments (CI/CD) to update the image without Terraform drift
    ignore_changes = [task_definition]
  }
}
