---
id: creating-and-publishing
title: Creating and publishing your docs
sidebar_label: Creating and Publishing Documentation
description: Guidance on how to create and publish documentation
---

This section will guide you through how to:

- [Create a basic documentation setup](#create-a-basic-documentation-setup)
  - [Use any software template](#use-any-software-template)
  - [Use the documentation template](#use-the-documentation-template)
  - [Enable documentation for an already existing entity](#enable-documentation-for-an-already-existing-entity)
- [Writing and previewing your documentation](#writing-and-previewing-your-documentation)

## Prerequisites

- A working Backstage instance with TechDocs installed (see
  [TechDocs getting started](getting-started.md))

## Create a basic documentation setup

If you have an existing repository that you'd like to add documentation to, skip
to the
[Enable documentation for an already existing entity setup](#enable-documentation-for-an-already-existing-entity)
section below. Otherwise, continue reading to create a new software entity
including documentation from scratch.

### Use any software template

TechDocs is built on top of the
[docs like code approach](https://www.docslikecode.com/about/). This, in short,
means that you should keep documentation close to the code.

Your Backstage app has a set of software templates added by default. All of
these software templates include everything you need to get your TechDocs site
up and running and to start writing your documentation.

If you have created software templates that do not include documentation by
default, we highly recommend you to set that up. Follow our how-to guide
[How to add documentation setup to your software templates](./how-to-guides.md#how-to-add-the-documentation-setup-to-your-software-templates)
to get started.

### Use the documentation template

There could be _some_ situations where you don't want to keep your docs close to
your code, but still want to publish documentation - for example, an onboarding
tutorial. For this use case, we have put together a documentation template. Your
Backstage instance should by default have a documentation template added. If
not, copy the catalog locations from the
[create-app template](https://github.com/backstage/backstage/blob/master/packages/create-app/templates/default-app/app-config.yaml.hbs)
to add the documentation template. The template creates a component with
**only** TechDocs configuration and default markdown files, and is otherwise
empty.

![Documentation Template](../../assets/techdocs/documentation-template.png)

Create an entity from the documentation template and you will get the needed
setup for free.

### Enable documentation for an already existing entity

Prerequisites:

- An existing entity
  [registered in backstage](../software-catalog/index.md#adding-components-to-the-catalog)
  (e.g. via a `catalog-info.yaml` file).

Create an `mkdocs.yml` file in the root of your repository with the following
content:

```yaml
site_name: 'example-docs'

nav:
  - Home: index.md

plugins:
  - techdocs-core
```

> Note - The plugins section above is optional. Backstage automatically adds the `techdocs-core` plugin to the
> mkdocs file if it is missing. This functionality can be turned off with a [configuration option](./configuration.md) in Backstage.

Update your component's entity description by adding the following lines to its
`catalog-info.yaml` in the root of its repository:

```yaml
metadata:
  annotations:
    backstage.io/techdocs-ref: dir:.
```

The
[`backstage.io/techdocs-ref` annotation](../software-catalog/well-known-annotations.md#backstageiotechdocs-ref)
is used by TechDocs to download the documentation source files for generating an
entity's TechDocs site.

Create a `/docs` folder in the root of your repository with at least an
`index.md` file in it. _(If you add more markdown files, make sure to update the
nav in the mkdocs.yml file to get a proper navigation for your documentation.)_

> Note - Although `docs` is a popular directory name for storing documentation,
> it can be renamed to something else and can be configured by `mkdocs.yml`. See
> https://www.mkdocs.org/user-guide/configuration/#docs_dir

The `docs/index.md` can for example have the following content:

```md
# example docs

This is a basic example of documentation.
```

Commit your changes, open a pull request and merge. You will now get your
updated documentation next time you run Backstage!

## Writing and previewing your documentation

Using the [techdocs-cli](https://github.com/backstage/backstage/tree/master/packages/techdocs-cli) you can
preview your docs inside a local Backstage instance and get live reload on
changes. This is useful when you want to preview your documentation while
writing.

To do this you can run:

```bash
cd /path/to/docs-repository/
npx @techdocs/cli serve
```
