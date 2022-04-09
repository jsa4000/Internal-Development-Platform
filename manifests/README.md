# Kustomization

This folder is used as an example of a deployment of backstage in Kubernetes using the  `Monolithic` approach.

To deploy backstage into local kubernetes cluster use.

```bash
# Create backstage namespace
kubectl create ns backstage

# Create backstage from kustomize
kubectl apply -n backstage -k overlays/local
```

Verify and test the installation

```bash
# Get all the pods
kubectl get pods -n backstage -w

```