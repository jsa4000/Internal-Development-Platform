# [Backstage](https://backstage.io)

## Node

```bash
# Install Node via nvm (Node Version Manager)
# https://github.com/nvm-sh/nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash

# Install Node v16.x LTS version
nvm install v16.16.0

# Get Node and NPM versions
node -v
npm -v

# Install Yarn
npm install --global yarn
```

## Run

To start the app, run:

```bash
yarn install
yarn dev
```

## Upgrade

To update backstage use the [Upgrade Helper](https://backstage.github.io/upgrade-helper/)

```bash
VERSION=0.4.36
# Create new branch
git checkout -b upgraded-$VERSION

# Use following command to update dependencies and checl changes made with update-helper
yarn backstage-cli versions:bump

# Create Base application with latest version
mkdir temp && cd temp
npx @backstage/create-app@$VERSION

# Copy the content into the original, check changes, test and merge changes
```

## Demo

In order to run the demo, use the following command.

```bash
# You first may define the credentials for Github, Bitbucket, etc..
export GITHUB_TOKEN=****
export AUTH_GITHUB_CLIENT_ID=****            
export AUTH_GITHUB_CLIENT_SECRET=****

# See init-demo.sh to get all variables needed

# The run the following command. 
# If credentials are not detected within environment variables, it should prompt it,
source samples/init-demo.sh
```

## Docker

Backstage can be containerized as a `Monolithic` application or using a `microservice` approach with different images for `frontend` and `backend` correspondingly. Both approaches have their advantages and disadvantages in terms on image size, decoupling, horizontal scaling, etc..

The following procedure generates an **unique** image with the `backend` and `frontend` components. This method allows to share libraries between components so the sum of image size is less than having the images split separately.

```bash
# Install all dependencies
yarn install --frozen-lockfile
# tsc outputs type definitions to dist-types/ in the repo root, which are then consumed by the build
yarn tsc
# Build all packages and in the end bundle them all up into the packages/backend/dist folder.
yarn build

# File 'bundle.tar.gz' and 'skeleton.tar.gz' must be generated in order to create the docker image. These files are create inside 'packages/backend/dist'

# From the root project, run the following command to build the image
VERSION=1.0.3
docker image build . -f packages/backend/Dockerfile --tag jsa4000/backstage:$VERSION

# Publish to container registry
docker image push jsa4000/backstage:$VERSION

# Export GITHUB and BITBUCKET credentials into env variables

# Access backstage using a docker container (http://localhost:7007)
docker run -it -p 7007:7007 -e GITHUB_TOKEN=$GITHUB_TOKEN jsa4000/backstage:$VERSION
```
