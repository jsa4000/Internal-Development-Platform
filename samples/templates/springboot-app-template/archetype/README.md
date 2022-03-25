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