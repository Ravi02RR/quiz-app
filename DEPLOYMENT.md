# Quiz App - Deployment Guide

Complete guide for deploying the Quiz Application with Docker, Terraform, and Jenkins CI/CD pipeline.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Deployment](#docker-deployment)
4. [AWS Infrastructure with Terraform](#aws-infrastructure-with-terraform)
5. [Jenkins CI/CD Setup](#jenkins-cicd-setup)
6. [Production Deployment](#production-deployment)
7. [Monitoring and Logging](#monitoring-and-logging)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Tools
- Docker (v20.10+)
- Docker Compose (v2.0+)
- Java 17
- Maven 3.9+
- Terraform (v1.0+)
- Jenkins (v2.400+)
- AWS CLI (v2.0+)
- Git

### Required Accounts
- AWS Account with appropriate permissions
- Docker Registry account (Docker Hub, ECR, etc.)
- SonarQube instance (local or cloud)

## Local Development Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Ravi02RR/quiz-app.git
cd quiz-app
```

### 2. Configure Environment Variables
```bash
cp .env.example .env
# Edit .env with your configuration
```

### 3. Build the Application
```bash
mvn clean package
```

### 4. Run with Docker Compose
```bash
docker-compose up -d
```

### 5. Access the Application
- Application: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
- Swagger UI: http://localhost:8080/swagger-ui.html
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

## Docker Deployment

### Build Docker Image
```bash
docker build -t quiz-app:latest .
```

### Tag and Push to Registry
```bash
docker tag quiz-app:latest your-registry.com/quiz-app:latest
docker push your-registry.com/quiz-app:latest
```

### Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f quiz-app

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## AWS Infrastructure with Terraform

### 1. Initialize Terraform Backend

First, create S3 bucket and DynamoDB table for state management:

```bash
# Create S3 bucket for Terraform state
aws s3 mb s3://quiz-app-terraform-state --region us-east-1
aws s3api put-bucket-versioning \
  --bucket quiz-app-terraform-state \
  --versioning-configuration Status=Enabled

# Create DynamoDB table for state locking
aws dynamodb create-table \
  --table-name quiz-app-terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --region us-east-1
```

### 2. Configure Terraform Variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
```

### 3. Deploy Infrastructure

```bash
# Initialize Terraform
terraform init

# Plan the deployment
terraform plan

# Apply the changes
terraform apply

# Get outputs
terraform output
```

### 4. Destroy Infrastructure (when needed)

```bash
terraform destroy
```

## Jenkins CI/CD Setup

### 1. Install Required Jenkins Plugins

- Docker Pipeline
- AWS Steps
- SonarQube Scanner
- Email Extension
- Credentials Binding
- Git Plugin
- Pipeline

### 2. Configure Jenkins Credentials

Add the following credentials in Jenkins:
- `docker-registry-credentials`: Docker registry username/password
- `git-credentials`: GitHub username/token
- `aws-access-key-id`: AWS access key
- `aws-secret-access-key`: AWS secret key
- SonarQube token in SonarQube configuration

### 3. Configure Jenkins Tools

Go to Manage Jenkins → Global Tool Configuration:
- Add Maven installation: `Maven-3.9.5`
- Add JDK installation: `JDK-17`

### 4. Configure SonarQube

1. Go to Manage Jenkins → Configure System
2. Add SonarQube server:
   - Name: `SonarQube`
   - Server URL: `http://sonarqube:9000`
   - Authentication token: Add from credentials

### 5. Create Jenkins Pipeline

1. New Item → Pipeline
2. Configure:
   - Pipeline script from SCM
   - Repository URL: `https://github.com/Ravi02RR/quiz-app.git`
   - Script Path: `Jenkinsfile`
3. Save and build

## Production Deployment

### Pipeline Flow

1. **Checkout**: Code is pulled from the repository
2. **Build**: Maven compiles the code
3. **Unit Tests**: JUnit tests are executed
4. **SonarQube Analysis**: Code quality check
5. **Quality Gate**: Pipeline fails if quality gate fails
6. **Package**: JAR file is created
7. **Docker Build**: Docker image is built
8. **Security Scan**: Trivy scans for vulnerabilities
9. **Push to Registry**: Image is pushed to Docker registry
10. **Deploy to Prod Branch**: Changes merged to prod branch
11. **Terraform Apply**: Infrastructure is provisioned
12. **Docker Compose Deploy**: Application is deployed
13. **Health Check**: Application health is verified

### Deploy to Prod Branch

The Jenkinsfile automatically:
- Merges main branch to prod branch
- Tags the release with build number
- Pushes to remote repository

### Manual Deployment to Prod

If needed, you can manually merge to prod:

```bash
git checkout prod
git merge main
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin prod
git push origin v1.0.0
```

## Monitoring and Logging

### Prometheus Metrics
- URL: http://localhost:9090
- Metrics endpoint: http://localhost:8080/actuator/prometheus

### Grafana Dashboards
- URL: http://localhost:3000
- Default credentials: admin/admin
- Import Spring Boot dashboard (ID: 4701)

### Application Logs
```bash
# View application logs
docker-compose logs -f quiz-app

# View all service logs
docker-compose logs -f

# View logs from specific time
docker-compose logs --since 10m quiz-app
```

### CloudWatch Logs (AWS)
- Log group: `/ecs/quiz-app-prod`
- Access via AWS Console or CLI:
```bash
aws logs tail /ecs/quiz-app-prod --follow
```

## Troubleshooting

### Application Won't Start

**Check logs:**
```bash
docker-compose logs quiz-app
```

**Common issues:**
- Database connection failed: Verify PostgreSQL is running
- Port already in use: Change port in docker-compose.yml
- Memory issues: Increase Docker memory allocation

### Database Connection Issues

**Check PostgreSQL:**
```bash
docker-compose exec postgres psql -U quizuser -d quizdb
```

**Reset database:**
```bash
docker-compose down -v
docker-compose up -d
```

### Docker Build Failures

**Clear Docker cache:**
```bash
docker builder prune
docker-compose build --no-cache
```

### Terraform Errors

**Reset Terraform state:**
```bash
cd terraform
terraform init -reconfigure
```

**Check AWS credentials:**
```bash
aws sts get-caller-identity
```

### Jenkins Pipeline Failures

**SonarQube connection:**
- Verify SonarQube is running
- Check token in Jenkins credentials
- Ensure network connectivity

**Docker registry push fails:**
- Verify registry credentials
- Check network connectivity
- Ensure sufficient disk space

### Performance Issues

**Scale ECS service:**
```bash
aws ecs update-service \
  --cluster quiz-app-cluster-prod \
  --service quiz-app-service-prod \
  --desired-count 4
```

**Check resource usage:**
```bash
docker stats
```

## Security Best Practices

1. **Secrets Management**
   - Never commit secrets to Git
   - Use AWS Secrets Manager or Parameter Store
   - Rotate credentials regularly

2. **Network Security**
   - Use VPC with private subnets
   - Configure security groups properly
   - Enable encryption in transit and at rest

3. **Container Security**
   - Run containers as non-root user
   - Scan images for vulnerabilities
   - Keep base images updated

4. **Access Control**
   - Use IAM roles and policies
   - Enable MFA for AWS accounts
   - Follow principle of least privilege

## Backup and Recovery

### Database Backup

**Manual backup:**
```bash
docker-compose exec postgres pg_dump -U quizuser quizdb > backup.sql
```

**Restore from backup:**
```bash
docker-compose exec -T postgres psql -U quizuser quizdb < backup.sql
```

### AWS RDS Automated Backups
- Configured in Terraform (7-day retention)
- Point-in-time recovery available
- Manual snapshots can be created via AWS Console

## Maintenance

### Update Application

```bash
# Pull latest code
git pull origin main

# Rebuild and restart
docker-compose down
docker-compose build
docker-compose up -d
```

### Update Infrastructure

```bash
cd terraform
terraform plan
terraform apply
```

### Clean Up Unused Resources

```bash
# Docker cleanup
docker system prune -a

# Remove old images
docker image prune -a

# AWS cleanup (be careful!)
# Review resources in AWS Console before deleting
```

## Support

For issues and questions:
- GitHub Issues: https://github.com/Ravi02RR/quiz-app/issues
- Email: team@example.com

## License

[Your License Here]
