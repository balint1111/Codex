FROM jenkins/jenkins:lts

USER root
RUN apt-get update \
 && apt-get install -y docker.io tar gzip \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd -g 999 docker  || true \
 && usermod -aG docker jenkins
 
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl && \
    rm kubectl

RUN curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 \
    && chmod 700 get_helm.sh \
    && ./get_helm.sh \
    && rm get_helm.sh
USER jenkins
