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
          kubectl get secret codex-db-credentials -n dev >/dev/null 2>&1 && \
          kubectl label secret codex-db-credentials -n dev app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate secret codex-db-credentials -n dev meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dev --overwrite || true
          kubectl get pvc codex-db-pvc -n dev >/dev/null 2>&1 && \
          kubectl label pvc codex-db-pvc -n dev app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate pvc codex-db-pvc -n dev meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dev --overwrite || true
          kubectl get service codex-backend -n dev >/dev/null 2>&1 && \
          kubectl label service codex-backend -n dev app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-backend -n dev meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dev --overwrite || true
          kubectl get service codex-frontend -n dev >/dev/null 2>&1 && \
          kubectl label service codex-frontend -n dev app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-frontend -n dev meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dev --overwrite || true
          kubectl get service codex-db -n dev >/dev/null 2>&1 && \
          kubectl label service codex-db -n dev app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-db -n dev meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dev --overwrite || true
          kubectl get deployment codex-db -n dev >/dev/null 2>&1 && \
          kubectl label deployment codex-db -n dev app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-db -n dev meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dev --overwrite || true
          kubectl get deployment codex-backend -n dev >/dev/null 2>&1 && \
          kubectl label deployment codex-backend -n dev app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-backend -n dev meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dev --overwrite || true
          kubectl get deployment codex-frontend -n dev >/dev/null 2>&1 && \
          kubectl label deployment codex-frontend -n dev app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-frontend -n dev meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dev --overwrite || true
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
          kubectl get secret codex-db-credentials -n dani >/dev/null 2>&1 && \
          kubectl label secret codex-db-credentials -n dani app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate secret codex-db-credentials -n dani meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dani --overwrite || true
          kubectl get pvc codex-db-pvc -n dani >/dev/null 2>&1 && \
          kubectl label pvc codex-db-pvc -n dani app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate pvc codex-db-pvc -n dani meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dani --overwrite || true
          kubectl get service codex-backend -n dani >/dev/null 2>&1 && \
          kubectl label service codex-backend -n dani app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-backend -n dani meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dani --overwrite || true
          kubectl get service codex-frontend -n dani >/dev/null 2>&1 && \
          kubectl label service codex-frontend -n dani app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-frontend -n dani meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dani --overwrite || true
          kubectl get service codex-db -n dani >/dev/null 2>&1 && \
          kubectl label service codex-db -n dani app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-db -n dani meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dani --overwrite || true
          kubectl get deployment codex-db -n dani >/dev/null 2>&1 && \
          kubectl label deployment codex-db -n dani app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-db -n dani meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dani --overwrite || true
          kubectl get deployment codex-backend -n dani >/dev/null 2>&1 && \
          kubectl label deployment codex-backend -n dani app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-backend -n dani meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dani --overwrite || true
          kubectl get deployment codex-frontend -n dani >/dev/null 2>&1 && \
          kubectl label deployment codex-frontend -n dani app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-frontend -n dani meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=dani --overwrite || true
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
      when { branch 'staging' }
      steps {
        input 'Deploy to staging?'
        sh '''
          kubectl get secret codex-db-credentials -n staging >/dev/null 2>&1 && \
          kubectl label secret codex-db-credentials -n staging app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate secret codex-db-credentials -n staging meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=staging --overwrite || true
          kubectl get pvc codex-db-pvc -n staging >/dev/null 2>&1 && \
          kubectl label pvc codex-db-pvc -n staging app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate pvc codex-db-pvc -n staging meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=staging --overwrite || true
          kubectl get service codex-backend -n staging >/dev/null 2>&1 && \
          kubectl label service codex-backend -n staging app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-backend -n staging meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=staging --overwrite || true
          kubectl get service codex-frontend -n staging >/dev/null 2>&1 && \
          kubectl label service codex-frontend -n staging app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-frontend -n staging meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=staging --overwrite || true
          kubectl get service codex-db -n staging >/dev/null 2>&1 && \
          kubectl label service codex-db -n staging app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-db -n staging meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=staging --overwrite || true
          kubectl get deployment codex-db -n staging >/dev/null 2>&1 && \
          kubectl label deployment codex-db -n staging app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-db -n staging meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=staging --overwrite || true
          kubectl get deployment codex-backend -n staging >/dev/null 2>&1 && \
          kubectl label deployment codex-backend -n staging app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-backend -n staging meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=staging --overwrite || true
          kubectl get deployment codex-frontend -n staging >/dev/null 2>&1 && \
          kubectl label deployment codex-frontend -n staging app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-frontend -n staging meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=staging --overwrite || true
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
          kubectl get secret codex-db-credentials -n prod >/dev/null 2>&1 && \
          kubectl label secret codex-db-credentials -n prod app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate secret codex-db-credentials -n prod meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=prod --overwrite || true
          kubectl get pvc codex-db-pvc -n prod >/dev/null 2>&1 && \
          kubectl label pvc codex-db-pvc -n prod app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate pvc codex-db-pvc -n prod meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=prod --overwrite || true
          kubectl get service codex-backend -n prod >/dev/null 2>&1 && \
          kubectl label service codex-backend -n prod app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-backend -n prod meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=prod --overwrite || true
          kubectl get service codex-frontend -n prod >/dev/null 2>&1 && \
          kubectl label service codex-frontend -n prod app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-frontend -n prod meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=prod --overwrite || true
          kubectl get service codex-db -n prod >/dev/null 2>&1 && \
          kubectl label service codex-db -n prod app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate service codex-db -n prod meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=prod --overwrite || true
          kubectl get deployment codex-db -n prod >/dev/null 2>&1 && \
          kubectl label deployment codex-db -n prod app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-db -n prod meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=prod --overwrite || true
          kubectl get deployment codex-backend -n prod >/dev/null 2>&1 && \
          kubectl label deployment codex-backend -n prod app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-backend -n prod meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=prod --overwrite || true
          kubectl get deployment codex-frontend -n prod >/dev/null 2>&1 && \
          kubectl label deployment codex-frontend -n prod app.kubernetes.io/managed-by=Helm --overwrite && \
          kubectl annotate deployment codex-frontend -n prod meta.helm.sh/release-name=codex meta.helm.sh/release-namespace=prod --overwrite || true
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
