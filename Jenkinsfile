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
      steps {
        script {
                  // Export both images with the current BUILD_NUMBER
                  sh '''
                        export IMAGE_BACKEND=${REGISTRY_URL}/codex-backend:${BUILD_NUMBER}
                        export IMAGE_FRONTEND=${REGISTRY_URL}/codex-frontend:${BUILD_NUMBER}

			# Render & apply backend YAML
			envsubst < kubernetes/dev/backend-deployment.yaml | kubectl apply -f -

			# Render & apply frontend YAML
			envsubst < kubernetes/dev/frontend-deployment.yaml | kubectl apply -f -
			
			# Apply the Postgres DB
                        kubectl apply -f kubernetes/dev/db.yaml
                  '''
                }
      }
    }

    stage('Deploy to dani') {
      when { branch 'dani' }
      steps {
        script {
                  sh '''
                        export IMAGE_BACKEND=${REGISTRY_URL}/codex-backend:${BUILD_NUMBER}
                        export IMAGE_FRONTEND=${REGISTRY_URL}/codex-frontend:${BUILD_NUMBER}

                        kubectl apply -f kubernetes/dani/namespace.yaml
                        envsubst < kubernetes/dani/backend-deployment.yaml | kubectl apply -f -
                        envsubst < kubernetes/dani/frontend-deployment.yaml | kubectl apply -f -
                        kubectl apply -f kubernetes/dani/db.yaml
                  '''
                }
      }
    }

    stage('Deploy to staging') {
      when { branch 'main' }
      steps {
        input 'Deploy to staging?'
        sh 'kubectl apply -f kubernetes/staging'
      }
    }

    stage('Deploy to prod') {
      when { branch 'main' }
      steps {
        input 'Deploy to production?'
        sh 'kubectl apply -f kubernetes/prod'
      }
    }
  }

  post {
    always {
      echo "Pruning dangling images…"
      sh 'docker image prune -f'
    }
  }
}
