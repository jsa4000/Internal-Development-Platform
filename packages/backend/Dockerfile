# This dockerfile builds an image for the backend package.
# It should be executed with the root of the repo as docker context.
#
# Before building this image, be sure to have run the following commands in the repo root:
#
# yarn install
# yarn tsc
# yarn build:all
#
# Once the commands have been run, you can build the image using `yarn build-image`

FROM debian:10-slim

# Create nooe user
RUN groupadd --gid 1000 node \
  && useradd --uid 1000 --gid node --shell /bin/sh --create-home node

# replace shell with bash so we can source files
RUN rm /bin/sh && ln -s /bin/bash /bin/sh

# update the repository sources list
# and install dependencies
RUN apt-get update \
    && apt-get install -y curl \
    && apt-get -y autoclean

# nvm environment variables
RUN mkdir /usr/local/nvm
ENV NVM_DIR /usr/local/nvm
ENV NVM_VERSION 0.39.3
ENV NODE_VERSION 16.16.0

# install nvm
RUN curl --silent -o- https://raw.githubusercontent.com/nvm-sh/nvm/v$NVM_VERSION/install.sh | bash

# install node and npm
RUN source $NVM_DIR/nvm.sh \
    && nvm install $NODE_VERSION \
    && nvm alias default $NODE_VERSION \
    && nvm use default

# add node and npm to path so the commands are available
ENV NODE_PATH $NVM_DIR/v$NODE_VERSION/lib/node_modules
ENV PATH $NVM_DIR/versions/node/v$NODE_VERSION/bin:$PATH

## Install npm dependencies
RUN npm install -g yarn

# install additional dependencies
RUN apt-get update \
    && apt-get install -y libsqlite3-dev python3 python3-pip cmake g++ git \
    && rm -rf /var/lib/apt/lists/* \
    && yarn config set python /usr/bin/python3 \
    && pip3 install mkdocs mkdocs-techdocs-core

# Set node user
WORKDIR /home/node
USER node

# Copy repo skeleton first, to avoid unnecessary docker cache invalidation.
# The skeleton contains the package.json of each package in the monorepo,
# and along with yarn.lock and the root package.json, that's enough to run yarn install.
COPY yarn.lock package.json packages/backend/dist/skeleton.tar.gz ./
RUN tar xzf skeleton.tar.gz && rm skeleton.tar.gz

RUN yarn install --frozen-lockfile --production --network-timeout 300000 && rm -rf "$(yarn cache dir)"

# Then copy the rest of the backend bundle, along with any other files we might want.
COPY packages/backend/dist/bundle.tar.gz app-config.* ./
RUN tar xzf bundle.tar.gz && rm bundle.tar.gz

# Run backstage using configuration files.
CMD ["node", "packages/backend", "--config", "app-config.yaml", "--config", "app-config.dev.yaml"]
