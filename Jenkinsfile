pipeline {
  agent any

  environment {
    // let the in-container Docker CLI talk to Docker Desktop’s daemon
    DOCKER_HOST        = "tcp://host.docker.internal:2375"
    // your local registry on the Windows host
    REGISTRY_URL       = "host.docker.internal:5000"
    IMAGE_BACKEND      = "${REGISTRY_URL}/codex-backend:${env.BUILD_NUMBER}"
    IMAGE_FRONTEND     = "${REGISTRY_URL}/codex-frontend:${env.BUILD_NUMBER}"
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

    stage('Build & Push Images') {
      steps {
        script {
          // wrap in withRegistry so Jenkins knows no credentials are needed
          docker.withRegistry("http://${REGISTRY_URL}", '') {
            // build & push backend
            def backImg = docker.build("${REGISTRY_URL}/codex-backend:${env.BUILD_NUMBER}", "backend")
            backImg.push()
            // build & push frontend
            def frontImg = docker.build("${REGISTRY_URL}/codex-frontend:${env.BUILD_NUMBER}", "frontend")
            frontImg.push()
          }
        }
      }
    }

    stage('Deploy to dev') {
      steps {
        sh 'kubectl apply -f kubernetes/dev'
      }
    }

    stage('Deploy to staging') {
      when { branch 'main' }
      steps {
        input message: 'Deploy to staging?'
        sh 'kubectl apply -f kubernetes/staging'
      }
    }

    stage('Deploy to prod') {
      when { branch 'main' }
      steps {
        input message: 'Deploy to production?'
        sh 'kubectl apply -f kubernetes/prod'
      }
    }
  }

  post {
    always {
      echo "Cleaning up any dangling images..."
      sh 'docker image prune -f'
    }
  }
}
