# ERP web

Angular frontend for the ERP. See the [monorepo README](../../README.md) for prerequisites and the complete local setup.

From this directory, run `npm ci`. Start the API with its `dev` profile and run `npm run api:generate` once its OpenAPI endpoint is available. Generated clients are ignored by Git and must be regenerated after API contract changes.

Run `npm start` for the development server, `npm run build` for a production build, and `npm test -- --watch=false` for unit tests. The development proxy forwards API requests to localhost port 8080.
