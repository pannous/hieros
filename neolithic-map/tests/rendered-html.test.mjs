import assert from "node:assert/strict";
import { spawn } from "node:child_process";
import test from "node:test";

const port = 4199;
const url = `http://127.0.0.1:${port}/`;

async function waitForServer(process) {
  for (let attempt = 0; attempt < 50; attempt += 1) {
    if (process.exitCode !== null) {
      throw new Error(`Production server exited with code ${process.exitCode}`);
    }

    try {
      const response = await fetch(url);
      if (response.ok) return response;
    } catch {
      // The production server is still starting.
    }

    await new Promise((resolve) => setTimeout(resolve, 100));
  }

  throw new Error(`Production server did not become ready at ${url}`);
}

test("production build renders the map", async (context) => {
  const server = spawn(
    process.execPath,
    ["node_modules/vinext/dist/cli.js", "start", "--port", String(port)],
    { stdio: "pipe" },
  );

  context.after(() => {
    server.kill("SIGTERM");
  });

  const response = await waitForServer(server);
  const html = await response.text();

  assert.match(html, /<title>Neolithic Sites \/ Ancient Architects Map<\/title>/);
  assert.match(html, /aria-label="Interactive Neolithic map"/);
  assert.match(html, /Search sites or videos/);
});
