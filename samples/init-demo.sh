#!/bin/bash

# To tun use the following command:
# > source init-demo.sh
#
# Note: Since 'source' command need to be used to export environment variables, 'return' statements are used instead 'exit'.

# Set to current directoy, just in case it is executed from another location.
CUR_DIR=$(pwd)
cd "$(dirname "$0")"

finish() {
    echo "Exiting gracefully the script"
    # Return to previous folder
    cd $CUR_DIR
}

# Catch errors ocurred in the script to cleanup
#trap finish EXIT INT QUIT TERM KILL STOP HUP

# Colors
RED="\033[1;31m"
GREEN="\033[32m"
YELLOW="\033[33m"
BOLD="\033[1;0m"
ENDCOLOR="\033[0m"

# Enable Debug Mode (Outputs)
DEBUG_MODE=1

# Variables
ARGOCD_DOMAIN=argocd.devops.com
ARGOCD_CHART_REPO=https://argoproj.github.io/argo-helm
ARGOCD_CHART_VERSION=4.2.1
ARGOCD_VALUES=./k8s/argocd-values.yaml
K8S_ROLE_BINDING=./k8s/default-clusterrolebinding.yaml
INGRESS_CONTROLLER=traefik

# Flags
GITHUB_ENABLED=1
BITBUCKET_ENABLED=0

echo -e"
For the installation the following pre-requisites are needed:

 ${YELLOW}1. Kubernetes +v1.22 cluster installed.
 2. Helm +3.x version installed.
 3. Ingress controller $INGRESS_CONTROLLER installed.}
 4. Host file entry with '127.0.0.1 $ARGOCD_DOMAIN'
 5. Git Credentials for Github, Bitbucket, etc..${ENDCOLOR}

It is recommended to use ${YELLOW}Rancher Desktop${ENDCOLOR} since it provides the majority of the requirements
"

# Check whether the domain name used to deploy argocc locally is configured into hosts file '/etc/hosts'
# i.e. 127.0.0.1 argocd.devops.com

# Check kubectl installation
KUBECTL_VERSION=$(kubectl version)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Checked kubectl is installed.${ENDCOLOR}"
else
    echo -e "${RED}ERROR: kubectl has not been detected${ENDCOLOR}"
    return 1
fi

# Check helm installation
HEML_VERSION=$(helm version)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Checked helm is installed.${ENDCOLOR}"
else
    echo -e "${RED}ERROR: helm has not been detected${ENDCOLOR}"
    return 1
fi

# Check ingress controller installed
if [ -z "$(kubectl get svc --all-namespaces | grep $INGRESS_CONTROLLER)" ]; then
    echo -e "${RED}ERROR: There is no entry for $INGRESS_CONTROLLER.${ENDCOLOR}"
    return 1
else
    echo -e "${GREEN}Checked $INGRESS_CONTROLLER is installed.${ENDCOLOR}"
fi

# Check Host entry configured properly
if [ -z "$(cat '/etc/hosts' | grep $ARGOCD_DOMAIN)" ]; then
    echo -e "${RED}ERROR: There is no entry in host file for $ARGOCD_DOMAIN.${ENDCOLOR}"
    return 1
else
    echo -e "${GREEN}Checked host entry for $ARGOCD_DOMAIN.${ENDCOLOR}"
fi

# Get Git Credentials
echo
echo "Get Git credentials"

# Check Github Credentials
if [ $GITHUB_ENABLED -eq 1 ]; then
    if [ -z "$GITHUB_TOKEN" ]; then
        echo "Enter the Github Token to be used"  
        read -r -s GITHUB_TOKEN
        # Clean characters read input
        export GITHUB_TOKEN=$(echo $GITHUB_TOKEN | sed $'s,\x1b\\[[0-9;]*[a-zA-Z],,g')
    fi
    echo -e "${GREEN}Checked Github Credentials.${ENDCOLOR}"
fi

# Check Github Credentials
if [ $BITBUCKET_ENABLED -eq 1 ]; then
    if [ -z "$BITBUCKET_USERNAME" ] || [ -z "$BITBUCKET_APP_PASSWORD" ]; then
        echo "Enter the Github Token to be used"  
        read -r -s BITBUCKET_USERNAME
        echo "Enter the Github Token to be used"  
        read -r -s BITBUCKET_APP_PASSWORD
        # Clean characters read input
        export BITBUCKET_USERNAME=$(echo $BITBUCKET_USERNAME | sed $'s,\x1b\\[[0-9;]*[a-zA-Z],,g')
        export BITBUCKET_APP_PASSWORD=$(echo $BITBUCKET_APP_PASSWORD | sed $'s,\x1b\\[[0-9;]*[a-zA-Z],,g')
    fi
    echo -e "${GREEN}Checked Bitbucket Credentials.${ENDCOLOR}"
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
        return 1
    fi
    sleep 3 && echo -e "${GREEN}ArgoCD has been installed.${ENDCOLOR}"
    
fi

# Export variables for ArgoCD
echo
echo "Get ArgoCD Token Credentials"
export ARGOCD_URL=http://$ARGOCD_DOMAIN
export ARGOCD_USERNAME=admin
export ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo)

# Get Token from ArgoCD Server using Ingress URL
ARGOCD_CREDENTIALS='{"username": "'"$ARGOCD_USERNAME"'", "password": "'"$ARGOCD_PASSWORD"'"}'
export ARGOCD_AUTH_TOKEN="argocd.token=$(curl -s -k -X POST -H 'Content-Type: application/json' $ARGOCD_URL/api/v1/session -d $ARGOCD_CREDENTIALS | jq '.token')"


if [ $DEBUG_MODE -eq 1 ]; then

    echo -e "${YELLOW}
Output Infomation (Debug mode):

GITHUB_TOKEN=$GITHUB_TOKEN

BITBUCKET_USERNAME=$BITBUCKET_USERNAME
BITBUCKET_APP_PASSWORD=$BITBUCKET_APP_PASSWORD

K8S_K3S_URL=$K8S_K3S_URL
K8S_K3S_TOKEN=$K8S_K3S_TOKEN

ARGOCD_URL=$ARGOCD_URL
ARGOCD_AUTH_TOKEN=$ARGOCD_AUTH_TOKEN
${ENDCOLOR}"

fi

echo -e "${GREEN}Installation Finished.${ENDCOLOR}

 - Remember to enable Kubernetes and ArgoCD Configuration in ${YELLOW}app-config.yaml${ENDCOLOR} file.
 - Finally enter ${YELLOW}yarn dev${ENDCOLOR} to start backstage.
"

echo -e ''$BOLD'
kubernetes:
  serviceLocatorMethod:
    type: 'multiTenant'
  clusterLocatorMethods:
  - type: 'config'
    clusters:
      - url: ${K8S_K3S_URL}
        name: k3s
        authProvider: serviceAccount
        skipTLSVerify: true # false
        skipMetricsLookup: true
        serviceAccountToken: ${K8S_K3S_TOKEN}

proxy:

  '/argocd/api':
    # url to the api of your hosted argoCD instance
    target: ${ARGOCD_URL}/api/v1/
    changeOrigin: true
    # this line is required if your hosted argoCD instance has self-signed certificate
    secure: false
    headers:
      Cookie: ${ARGOCD_AUTH_TOKEN}
'$ENDCOLOR''

echo -e ''$BOLD'
Examples:

    - Simple Example

    https://github.com/jsa4000/backstage-idp/blob/main/samples/catalog-info-1.yaml

    - Full Example (Domain, System, Components, Resources, APIs, etc..)

    https://github.com/jsa4000/backstage-idp/blob/main/samples/catalog-info-2.yaml

    - Kubernetes

    https://github.com/jsa4000/backstage-idp/blob/main/samples/catalog-info-3.yaml

    - ArgoCD

    kubectl apply -f samples/k8s/prometheus-app.yaml

    https://github.com/jsa4000/backstage-idp/blob/main/samples/catalog-info-4.yaml

'$ENDCOLOR''

# Return to previous folder
cd $CUR_DIR