resource "aws_ssm_parameter" "db_host" {
  name  = "/${var.app_name}/${var.environment}/db/host"
  type  = "String"
  value = aws_db_instance.postgres.address
}

resource "aws_ssm_parameter" "db_name" {
  name  = "/${var.app_name}/${var.environment}/db/name"
  type  = "String"
  value = var.db_name
}

resource "aws_ssm_parameter" "db_username" {
  name  = "/${var.app_name}/${var.environment}/db/username"
  type  = "SecureString"
  value = var.db_username
}

resource "aws_ssm_parameter" "db_password" {
  name  = "/${var.app_name}/${var.environment}/db/password"
  type  = "SecureString"
  value = random_password.db.result
}
