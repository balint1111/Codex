pipeline {
  agent any

  environment {
    // point the in-container Docker CLI at Docker Desktop’s daemon
    DOCKER_HOST    = "tcp://host.docker.internal:2375"
    // local registry on the Windows host
    REGISTRY_URL   = "host.docker.internal:5000"
    IMAGE_BACKEND  = "${REGISTRY_URL}/codex-backend:${env.BUILD_NUMBER}"
    IMAGE_FRONTEND = "${REGISTRY_URL}/codex-frontend:${env.BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
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
          // note the explicit "http://"
          docker.withRegistry("http://${REGISTRY_URL}", "") {
            // build & push backend
            def back = docker.build(IMAGE_BACKEND, "backend")
            back.push()
            // build & push frontend
            def front = docker.build(IMAGE_FRONTEND, "frontend")
            front.push()
          }
        }
      }
    }

    stage('Deploy to dev') {
      when { branch 'dev' }
      steps {
        sh '''
          helm upgrade --install codex helm/codex \
            -n dev --create-namespace \
            -f helm/codex/values.yaml \
            --set image.backend=${IMAGE_BACKEND} \
            --set image.frontend=${IMAGE_FRONTEND}
        '''
      }
    }

    stage('Deploy to dani') {
      when { branch 'dani' }
      steps {
        sh '''
          helm upgrade --install codex helm/codex \
            -n dani --create-namespace \
            -f helm/codex/values.yaml \
            -f helm/codex/values-dani.yaml \
            --set image.backend=${IMAGE_BACKEND} \
            --set image.frontend=${IMAGE_FRONTEND}
        '''
      }
    }

    stage('Deploy to staging') {
      when { branch 'main' }
      steps {
        input 'Deploy to staging?'
        sh '''
          helm upgrade --install codex helm/codex \
            -n staging --create-namespace \
            -f helm/codex/values.yaml \
            -f helm/codex/values-staging.yaml \
            --set image.backend=${IMAGE_BACKEND} \
            --set image.frontend=${IMAGE_FRONTEND}
        '''
      }
    }

    stage('Deploy to prod') {
      when { branch 'main' }
      steps {
        input 'Deploy to production?'
        sh '''
          helm upgrade --install codex helm/codex \
            -n prod --create-namespace \
            -f helm/codex/values.yaml \
            -f helm/codex/values-prod.yaml \
            --set image.backend=${IMAGE_BACKEND} \
            --set image.frontend=${IMAGE_FRONTEND}
        '''
      }
    }
  }

  post {
    always {
      echo "Pruning dangling images…"
      sh 'docker image prune -f || true'
    }
  }
}
