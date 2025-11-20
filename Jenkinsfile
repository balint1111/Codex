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
            back.push("latest")
            // build & push frontend
            def front = docker.build(IMAGE_FRONTEND, "frontend")
            front.push()
            front.push("latest")
          }
        }
      }
    }
	
	stage('Approve') {
      when {
        anyOf {
          branch 'staging'
          branch 'prod'
        }
        beforeAgent true
      }
      steps {
        input message: "Deploy to ${env.BRANCH_NAME}?"
      }
    }
	
	stage('Deploy') {
      steps {
        sh '''
          kubectl apply -f kubernetes/keycloak-realm-importer.yaml
          kubectl apply -f kubernetes/keycloak-crds.yaml
          helm dependency update helm/codex
          helm upgrade --install codex helm/codex \
            -n $BRANCH_NAME --create-namespace \
            -f helm/codex/values.yaml \
            -f helm/codex/values-$BRANCH_NAME.yaml \
            --set backend.image=${IMAGE_BACKEND} \
            --set frontend.image=${IMAGE_FRONTEND}
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
