pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'quiz-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = 'your-registry.com'
        SONAR_HOST_URL = 'http://sonarqube:9000'
        SONAR_PROJECT_KEY = 'quiz-app'
        GIT_REPO = 'https://github.com/Ravi02RR/quiz-app.git'
        PROD_BRANCH = 'prod'
    }
    
    tools {
        maven 'Maven-3.9.5'
        jdk 'JDK-17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out code from repository"
                    checkout scm
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo "Building the application with Maven"
                    sh 'mvn clean compile -DskipTests'
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                script {
                    echo "Running unit tests"
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    echo "Running SonarQube analysis"
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            mvn sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        """
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    echo "Checking SonarQube Quality Gate"
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    echo "Packaging the application"
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image"
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        
        stage('Security Scan - Trivy') {
            steps {
                script {
                    echo "Scanning Docker image for vulnerabilities"
                    sh """
                        trivy image --severity HIGH,CRITICAL --exit-code 0 ${DOCKER_IMAGE}:${DOCKER_TAG}
                    """
                }
            }
        }
        
        stage('Push Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'prod'
                }
            }
            steps {
                script {
                    echo "Pushing Docker image to registry"
                    withCredentials([usernamePassword(credentialsId: 'docker-registry-credentials', 
                                                     usernameVariable: 'DOCKER_USER', 
                                                     passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo \${DOCKER_PASS} | docker login -u \${DOCKER_USER} --password-stdin ${DOCKER_REGISTRY}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest
                            docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Prod Branch') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Merging changes to prod branch"
                    withCredentials([usernamePassword(credentialsId: 'git-credentials', 
                                                     usernameVariable: 'GIT_USER', 
                                                     passwordVariable: 'GIT_PASS')]) {
                        sh """
                            git config user.email "jenkins@ci.com"
                            git config user.name "Jenkins CI"
                            
                            # Fetch all branches
                            git fetch origin
                            
                            # Checkout or create prod branch
                            git checkout -B ${PROD_BRANCH}
                            
                            # Merge main into prod
                            git merge origin/main --no-edit
                            
                            # Tag the release
                            git tag -a v${BUILD_NUMBER} -m "Release version ${BUILD_NUMBER}"
                            
                            # Push to prod branch
                            git push https://${GIT_USER}:${GIT_PASS}@github.com/Ravi02RR/quiz-app.git ${PROD_BRANCH}
                            git push https://${GIT_USER}:${GIT_PASS}@github.com/Ravi02RR/quiz-app.git v${BUILD_NUMBER}
                        """
                    }
                }
            }
        }
        
        stage('Deploy with Terraform') {
            when {
                branch 'prod'
            }
            steps {
                script {
                    echo "Deploying infrastructure with Terraform"
                    dir('terraform') {
                        withCredentials([string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                                       string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY')]) {
                            sh """
                                terraform init
                                terraform plan -var="app_version=${DOCKER_TAG}" -out=tfplan
                                terraform apply -auto-approve tfplan
                            """
                        }
                    }
                }
            }
        }
        
        stage('Docker Compose Deploy') {
            when {
                branch 'prod'
            }
            steps {
                script {
                    echo "Deploying with Docker Compose"
                    sh """
                        docker-compose down || true
                        docker-compose up -d
                    """
                }
            }
        }
        
        stage('Health Check') {
            when {
                branch 'prod'
            }
            steps {
                script {
                    echo "Performing health check"
                    sh """
                        sleep 30
                        curl -f http://localhost:8080/actuator/health || exit 1
                    """
                }
            }
        }
    }
    
    post {
        always {
            echo "Cleaning up workspace"
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully!"
            emailext(
                subject: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """
                    Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Status: SUCCESS
                    
                    Check console output at ${env.BUILD_URL}
                """,
                to: "team@example.com"
            )
        }
        failure {
            echo "Pipeline failed!"
            emailext(
                subject: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """
                    Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Status: FAILURE
                    
                    Check console output at ${env.BUILD_URL}
                """,
                to: "team@example.com"
            )
        }
    }
}
