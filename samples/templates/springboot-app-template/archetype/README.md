# ${{ values.name | capitalize}}

${{ values.description }}

## Build

### Package

```bash
# Clean and Package the Application
mvn clean package 

# Run the application
mvn spring-boot:run
```

### Container image

```bash
# Build image using maven
mvn spring-boot:build-image

# Run container image
docker run -t -p 8080:8080 ${{ values.registry }}/${{ values.artifactId }}:${{ values.version }}

# Publish image to registry
docker push ${{ values.registry }}/${{ values.artifactId }}:${{ values.version }}
```

### Test

Open API Swagger UI to test the application

http://localhost:8080/swagger-ui/index.html

## Kubernetes

If configuration is used, the project can be deploye using kubernetes.

```bash
# Go to configuration folder path
cd ${{ values.targetPath }} 
```

### Helm (manual)

```bash
# Install using Helm
helm3 install app -n ${{ values.namespace }} --create-namespace --dependency-update .

# Install using kubernetes manifest (ArgoCD approach)

# Get template from chart
helm3 template app -n ${{ values.namespace }} --create-namespace --dependency-update . > template.yaml
# Apply the manifest created
kubectl create ns ${{ values.namespace }} 
kubectl apply -f template.yaml
```

### Test

```bash
# Test using port-forward (http://localhost:8080/swagger-ui.html)
kubectl port-forward -n ${{ values.namespace }} svc/${{ values.artifactId }} 8080:80

# Test using Ingress
http://localhost/${{ values.artifactId }}/swagger-ui.html
```

### ArgoCD (GitOps)

If ArgoCd is not installed, use following commands.

```bash
# Add Helm Repo
helm3 repo add argo https://argoproj.github.io/argo-helm

# Update repo
helm3 repo update

## Install ArgoCD with custom values equal to the application (argocd/argocd.yaml)
helm3 install argocd -n argocd --create-namespace argo/argo-cd --version 4.2.1 \
  --set redis-ha.enabled=false \
  --set controller.enableStatefulSet=false \
  --set server.autoscaling.enabled=false \
  --set repoServer.autoscaling.enabled=false
```

Use the `dashboard` to verify the applications

```bash

# Get the ArgoCD password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo

# Access ArgoCD as admin user
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Deploy the appliaction

```bash
# Deploy the applicationo into kubernetes
kubectl apply -f application.yaml  
```