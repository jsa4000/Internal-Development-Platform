#!/bin/bash

# Set to current directoy, just in case it is executed from another location.
CUR_DIR=$(pwd)
cd "$(dirname "$0")"

# To tun use the following command:
# > source init-demo.sh

# Colors
RED="\033[1;31m"
GREEN="\033[32m"
YELLOW="\033[33m"
ENDCOLOR="\033[0m"

# Enable Debug Mode (Outputs)
DEBUG_MODE=1

# Variables
ARGOCD_URL=argocd.devops.com
ARGOCD_CHART_REPO=https://argoproj.github.io/argo-helm
ARGOCD_CHART_VERSION=4.2.1
ARGOCD_VALUES=./k8s/argocd-values.yaml
K8S_ROLE_BINDING=./k8s/default-clusterrolebinding.yaml
INGRESS_CONTROLLER=traefik

echo
echo -e "For the installation the following pre-requisites are needed:"
echo
echo -e "${YELLOW} 1. Kubernetes +v1.22 cluster installed."
echo -e " 2. Helm +3.x version installed."
echo -e " 3. Ingress controller $INGRESS_CONTROLLER installed.}"
echo -e " 4. Host file entry with '127.0.0.1 $ARGOCD_URL'${ENDCOLOR}"
echo
echo -e "It is recommended to use ${YELLOW}Rancher Desktop${ENDCOLOR} since it provides the majority of the requirements"
echo

# Check whether the domain name used to deploy argocc locally is configured into hosts file '/etc/hosts'
# i.e. 127.0.0.1 argocd.devops.com

# Check kubectl installation
KUBECTL_VERSION=$(kubectl version)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Checked kubectl is installed.${ENDCOLOR}"
else
    echo -e "${RED}ERROR: kubectl has not been detected${ENDCOLOR}"
    exit 1
fi

# Check helm installation
HEML_VERSION=$(helm version)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Checked helm is installed.${ENDCOLOR}"
else
    echo -e "${RED}ERROR: helm has not been detected${ENDCOLOR}"
    exit 1
fi

# Echo the parameters
if [ -z "$(kubectl get svc --all-namespaces | grep $INGRESS_CONTROLLER)" ]; then
    echo -e "${RED}ERROR: There is no entry for $INGRESS_CONTROLLER.${ENDCOLOR}"
    exit 1
else
    echo -e "${GREEN}Checked $INGRESS_CONTROLLER is installed.${ENDCOLOR}"
fi

# Echo the parameters
if [ -z "$(cat '/etc/hosts' | grep $ARGOCD_URL)" ]; then
    echo -e "${RED}ERROR: There is no entry in host file for $ARGOCD_URL.${ENDCOLOR}"
    exit 1
else
    echo -e "${GREEN}Checked host entry for $ARGOCD_URL.${ENDCOLOR}"
fi

# Get Kubernetes Credentials
echo
echo "Get Kubernetes credentials"

# Apply previous ClusterRoleBinding to the default Service Account (To simplify the test)
kubectl apply -f $K8S_ROLE_BINDING  > /dev/null

# Export Kubernetes URL and token
export K8S_K3S_URL=$(kubectl cluster-info | awk {'print $NF'} | head -n 1 | sed $'s,\x1b\\[[0-9;]*[a-zA-Z],,g')
export K8S_K3S_TOKEN=$(kubectl -n default get secret $(kubectl -n default get sa default -o=json \
| jq -r '.secrets[0].name') -o=json \
| jq -r '.data["token"]' \
| base64 --decode)

# Deploy argo-cd into kubernetes
echo
echo "Installing ArgoCD"

# Adding the repo if not aalready addedd
helm repo add argo $ARGOCD_CHART_REPO > /dev/null
helm repo update argo > /dev/null
helm install argocd -n argocd --create-namespace argo/argo-cd --version $ARGOCD_CHART_VERSION -f $ARGOCD_VALUES --wait > /dev/null

# Checking the installation status
if [ $? -eq 0 ]; then
     sleep 3 && echo -e "${GREEN}ArgoCD has been installed.${ENDCOLOR}"
else
    echo -e "${RED}ERROR: Remove ArgoCD and reinstalling again.${ENDCOLOR}"
    sleep 3 && helm delete argocd -n argocd --wait > /dev/null
    sleep 3 && helm install argocd -n argocd --create-namespace argo/argo-cd --version $ARGOCD_CHART_VERSION -f $ARGOCD_VALUES --wait > /dev/null
    if [ ! $? -eq 0 ]; then
        echo -e "${RED}ERROR: Error installing ArgoCD.${ENDCOLOR}"
        exit 1
    fi
    sleep 3 && echo -e "${GREEN}ArgoCD has been installed.${ENDCOLOR}"
    
fi

# Export variables for ArgoCD
echo
echo "Get ArgoCD Token Credentials"
export ARGOCD_URL=$ARGOCD_URL
export ARGOCD_USERNAME=admin
export ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo)

# Get Token from ArgoCD Server using Ingress URL
ARGOCD_CREDENTIALS='{"username": "'"$ARGOCD_USERNAME"'", "password": "'"$ARGOCD_PASSWORD"'"}'
export ARGOCD_AUTH_TOKEN="argocd.token=$(curl -s -k -X POST -H 'Content-Type: application/json' $ARGOCD_URL/api/v1/session -d $ARGOCD_CREDENTIALS | jq '.token')"

echo
echo "${GREEN}Installation Finished.${ENDCOLOR}"
echo "Remember to enable Kubernetes and ArgoCD Configuration in ${YELLOW}app-config.yaml${ENDCOLOR} file"

if [ $DEBUG_MODE -eq 1 ]; then
    echo ${YELLOW}
    echo -e "Output Infomatinon (Debug mode)"
    echo
    echo "K8S_K3S_URL=$K8S_K3S_URL"
    echo "K8S_K3S_TOKEN=$K8S_K3S_TOKEN"
    echo "ARGOCD_URL=$ARGOCD_URL"
    echo "ARGOCD_AUTH_TOKEN=$ARGOCD_AUTH_TOKEN"
    echo ${ENDCOLOR}
fi

# Return to previous folder
cd $CUR_DIR