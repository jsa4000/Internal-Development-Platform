# Kustomization

This folder is used as an example of a deployment of backstage in Kubernetes using the  `Monolithic` approach.

To deploy backstage into local kubernetes cluster use.

```bash
# Create backstage namespace
kubectl create ns backstage

# Create secrets needed to run all plugins installed in backstage: Github, Bitbucket, kubernetes, ArgoCD, etc..
kubectl -n backstage create secret generic backstage \
    --from-literal=GITHUB_TOKEN="${GITHUB_TOKEN}" \
    --from-literal=AUTH_GITHUB_CLIENT_ID="${AUTH_GITHUB_CLIENT_ID}" \
    --from-literal=AUTH_GITHUB_CLIENT_SECRET="${AUTH_GITHUB_CLIENT_SECRET}"

# Create backstage from kustomize
kubectl apply -n backstage -k overlays/local
```

Verify and test the installation

```bash
# Get all the pods
kubectl get pods -n backstage -w

```