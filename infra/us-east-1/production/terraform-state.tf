terraform {
  backend "s3" {
    encrypt = true
    bucket = "your-terraform-remote-state"
    key = "dcp-demo-project/prod/ecs/terraform.tfstate"
    region = "us-east-1"
    profile = "demo"
    shared_credentials_file = "~/.aws/credentials"
    dynamodb_table = "terraform-remote-state"
  }
}
