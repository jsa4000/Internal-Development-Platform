---
id: writing-custom-actions
title: Writing Custom Actions
description: How to write your own actions
---

If you're wanting to extend the functionality of the Scaffolder, you can do so
by writing custom actions which can be used along side our
[built-in actions](./builtin-actions.md).

> Note: When adding custom actions, the actions array will **replace the
> built-in actions too**. To ensure you can continue to include the builtin
> actions, see below to include them during registration of your action.

## Writing your Custom Action

Your custom action can live where you choose, but simplest is to include it
alongside your `backend` package in `packages/backend`.

Let's create a simple action that adds a new file and some contents that are
passed as `input` to the function.

In `packages/backend/src/plugins/scaffolder/actions/custom.ts` we can create a
new action.

```ts
import { createTemplateAction } from '@backstage/plugin-scaffolder-backend';
import fs from 'fs-extra';

export const createNewFileAction = () => {
  return createTemplateAction<{ contents: string; filename: string }>({
    id: 'mycompany:create-file',
    schema: {
      input: {
        required: ['contents', 'filename'],
        type: 'object',
        properties: {
          contents: {
            type: 'string',
            title: 'Contents',
            description: 'The contents of the file',
          },
          filename: {
            type: 'string',
            title: 'Filename',
            description: 'The filename of the file that will be created',
          },
        },
      },
    },
    async handler(ctx) {
      await fs.outputFile(
        `${ctx.workspacePath}/${ctx.input.filename}`,
        ctx.input.contents,
      );
    },
  });
};
```

So let's break this down. The `createNewFileAction` is a function that returns a
`createTemplateAction`, and it's a good place to pass in dependencies which
close over the `TemplateAction`. Take a look at our
[built-in actions](https://github.com/backstage/backstage/blob/master/plugins/scaffolder-backend/src/scaffolder/actions/builtin)
for reference.

We set the type generic to `{ contents: string, filename: string }` which is
there to set the type on the handler `ctx` `inputs` property so we get good type
checking. This could be generated from the next part of this guide, the `input`
schema, but it's not supported right now. Feel free to contribute 🚀 👍.

The `createTemplateAction` takes an object which specifies the following:

- `id` - a unique ID for your custom action. We encourage you to namespace these
  in some way so that they won't collide with future built-in actions that we
  may ship with the `scaffolder-backend` plugin.
- `schema.input` - A JSON schema for input values to your function
- `schema.output` - A JSON schema for values which are outputted from the
  function using `ctx.output`
- `handler` - the actual code which is run part of the action, with a context

### The context object

When the action `handler` is called, we provide you a `context` as the only
argument. It looks like the following:

- `ctx.baseUrl` - a string where the template is located
- `ctx.logger` - a Winston logger for additional logging inside your action
- `ctx.logStream` - a stream version of the logger if needed
- `ctx.workspacePath` - a string of the working directory of the template run
- `ctx.input` - an object which should match the JSON schema provided in the
  `schema.input` part of the action definition
- `ctx.output` - a function which you can call to set outputs that match the
  JSON schema in `schema.output` for ex. `ctx.output('downloadUrl', something)`
- `createTemporaryDirectory` a function to call to give you a temporary
  directory somewhere on the runner so you can store some files there rather
  than polluting the `workspacePath`
- `ctx.metadata` - an object containing a `name` field, indicating the template
  name. More metadata fields may be added later.

## Registering Custom Actions

Once you have your Custom Action ready for usage with the scaffolder, you'll
need to pass this into the `scaffolder-backend` `createRouter` function. You
should have something similar to the below in
`packages/backend/src/plugins/scaffolder.ts`

```ts
return await createRouter({
  containerRunner,
  logger,
  config,
  database,
  catalogClient,
  reader,
});
```

There's another property you can pass here, which is an array of `actions` which
will set the available actions that the scaffolder has access to.

```ts
import { createBuiltinActions } from '@backstage/plugin-scaffolder-backend';
import { ScmIntegrations } from '@backstage/integration';

const integrations = ScmIntegrations.fromConfig(config);

const builtInActions = createBuiltinActions({
  containerRunner,
  integrations,
  config,
  catalogClient,
  reader,
});

const actions = [...builtInActions, createNewFileAction()];
return await createRouter({
  containerRunner,
  logger,
  config,
  database,
  catalogClient,
  reader,
  actions,
});
```

## List of custom action packages

Here is a list of Open Source custom actions that you can add to your Backstage
scaffolder backend:

| Name                    | Package                                                                                                                                 | Owner                                        |
| ----------------------- | --------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------- |
| Yeoman                  | [plugin-scaffolder-backend-module-yeoman](https://www.npmjs.com/package/@backstage/plugin-scaffolder-backend-module-yeoman)             | [Backstage](https://backstage.io)            |
| Cookiecutter            | [plugin-scaffolder-backend-module-cookiecutter](https://www.npmjs.com/package/@backstage/plugin-scaffolder-backend-module-cookiecutter) | [Backstage](https://backstage.io)            |
| Rails                   | [plugin-scaffolder-backend-module-rails](https://www.npmjs.com/package/@backstage/plugin-scaffolder-backend-module-rails)               | [Backstage](https://backstage.io)            |
| HTTP requests           | [scaffolder-backend-module-http-request](https://www.npmjs.com/package/@roadiehq/scaffolder-backend-module-http-request)                | [Roadie](https://roadie.io)                  |
| Scaffolder .NET Actions | [plugin-scaffolder-dotnet-backend](https://www.npmjs.com/package/@plusultra/plugin-scaffolder-dotnet-backend)                           | [Alef Carlos](https://github.com/alefcarlos) |

Have fun! 🚀
