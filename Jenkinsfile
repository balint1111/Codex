pipeline {
    agent any
    environment {
        REGISTRY = "myregistry"
        IMAGE_BACKEND = "${REGISTRY}/codex-backend:${env.BUILD_NUMBER}"
        IMAGE_FRONTEND = "${REGISTRY}/codex-frontend:${env.BUILD_NUMBER}"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Backend Tests') {
            steps {
                dir('backend') {
                    sh './gradlew test --no-daemon'
                }
            }
        }
        stage('Build Images') {
            steps {
                sh 'docker build -t $IMAGE_BACKEND ./backend'
                sh 'docker build -t $IMAGE_FRONTEND ./frontend'
                sh 'docker push $IMAGE_BACKEND'
                sh 'docker push $IMAGE_FRONTEND'
            }
        }
        stage('Deploy to dev') {
            steps {
                sh 'kubectl apply -f kubernetes/dev'
            }
        }
        stage('Deploy to staging') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to staging?'
                sh 'kubectl apply -f kubernetes/staging'
            }
        }
        stage('Deploy to prod') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?'
                sh 'kubectl apply -f kubernetes/prod'
            }
        }
    }
}
