# [Backstage](https://backstage.io)

This is your newly scaffolded Backstage App, Good Luck!

To start the app, run:

```bash
yarn install
yarn dev
```

## Upgrade

To update backstage use the [Upgrade Helper](https://backstage.github.io/upgrade-helper/)

```bash
VERSION=1.0.0
# Create new branch
git checkout -b upgraded-$VERSION

# Use following command to update dependencies and checl changes made with update-helper
yarn backstage-cli versions:bump

# Create Base application with latest version
cd temp
npx @backstage/create-app@$VERSION

# Copy the content in the original, check changes, test and merge changess

# Check other dependencies
npx npm-check-updates -y
```

## Demo

In order to run the demo, use the following command.

```bash
# You first may define the credentials for Github, Bitbucket, etc..
export GITHUB_TOKEN=****
export AUTH_GITHUB_CLIENT_ID=****            
export AUTH_GITHUB_CLIENT_SECRET=****

# The run the following command. 
# If credentials are not detected within environment variables, it should prompt it,
source samples/init-demo.sh
```
