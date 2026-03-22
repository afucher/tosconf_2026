# TosConf 2026

A simple Clojure web application built with [Pedestal](https://github.com/pedestal/pedestal) and [http-kit](https://github.com/http-kit/http-kit).

## Requirements

- [Leiningen](https://leiningen.org/) (`lein`)
- Java 11+
- [Docker](https://docs.docker.com/get-docker/) (for building and running the container image)

## Running locally

Start the application from the REPL:

```bash
lein repl
```

Then, inside the REPL, evaluate:

```clojure
(require 'hello)
(hello/start)
```

The HTTP server will be available at <http://localhost:8890> and the nREPL server at port `8891`.

### Test the endpoint

```bash
curl http://localhost:8890/greet
```

Expected response:

```
Hello, TosConf!
```

## Building and running with Docker

### Build the image

```bash
docker build -t tosconf-2026 .
```

The build uses two stages:
1. A `clojure:lein` container downloads dependencies and compiles an uberjar via `lein uberjar`.
2. A lean `eclipse-temurin:21-jre-alpine` container runs the resulting `app.jar`.

Dependency downloads are cached in a Docker layer — rebuilds are fast as long as `project.clj` hasn't changed.

### Run the container

```bash
docker run --rm -p 8890:8890 -p 8891:8891 tosconf-2026
```

| Flag | Purpose |
|------|---------|
| `--rm` | Remove the container automatically when it stops |
| `-p 8890:8890` | Expose the HTTP server |
| `-p 8891:8891` | Expose the nREPL server (local access only) |

The HTTP server will be available at <http://localhost:8890> and the nREPL server at port `8891`.

### Test the endpoint

```bash
curl http://localhost:8890/greet
```

Expected response:

```
Hello, TosConf!
```

## Deploy to Fly.io

### Prerequisites

- [Fly CLI](https://fly.io/docs/hands-on/install-flyctl/) installed
- A [Fly.io account](https://fly.io/app/sign-up)

### First-time setup

**1. Log in to Fly.io:**

```bash
fly auth login
```

**2. Create the app** (only needed once — registers the app name and allocates resources):

```bash
fly apps create tosconf-2026
```

> If the name `tosconf-2026` is already taken, choose another name and update the `app` field in `fly.toml` accordingly.

**3. Allocate a public IP address** so the HTTP service is reachable:

```bash
fly ips allocate-v4 --shared --app tosconf-2026
fly ips allocate-v6 --app tosconf-2026
```

> `--shared` uses a free shared IPv4 address, which is enough for learning and side projects.
> A dedicated IPv4 costs $2/mo and is only needed if you require a fixed IP (e.g. for DNS A records or IP whitelisting).
> IPv6 is always free.

> **Note:** `fly deploy` allocates IPs automatically on the first deploy, so you can skip this step entirely unless you need to control which type of IP is assigned.

**4. Deploy:**

```bash
fly deploy
```

Fly will build the Docker image remotely using your `Dockerfile` (`lein uberjar` inside the builder stage), push it to its registry, and start the machine. The first deploy takes a couple of minutes while dependencies are downloaded; subsequent deploys are faster thanks to Docker layer caching.

### Subsequent deploys

After the first-time setup, deploying a new version is just:

```bash
fly deploy
```

### Verify the deployment

Once deployed, check the app status:

```bash
fly status
```

Then hit the live endpoint:

```bash
curl https://tosconf-2026.fly.dev/greet
```

### Connect to the nREPL server remotely

The nREPL port (`8891`) is **not** exposed to the public internet. Connect to it securely using `fly proxy`, which tunnels traffic through Fly's private network via WireGuard.

**1. Start the proxy** (runs in the foreground — use a separate terminal):

```bash
fly proxy 18891:8891 -a tosconf-2026
```

This maps local port `18891` to the remote nREPL port `8891` on the Fly machine.

> **Tip:** To run it in the background instead:
> ```bash
> nohup fly proxy 18891:8891 -a tosconf-2026 > /dev/null 2>&1 &
> ```

**2. Connect to the REPL.** With the proxy running, connect your editor or REPL client to:

```
host: localhost
port: 18891
```

For example, using [brepl](https://github.com/licht1stein/brepl):

```bash
brepl --h localhost --p 18891 <<'EOF'
(System/getenv "FLY_APP_NAME")
EOF
```

Or connect from your editor (Calva, CIDER, Cursive, etc.) using `localhost:18891`.

### Useful Fly CLI commands

| Command | Description |
|---------|-------------|
| `fly logs` | Stream live application logs |
| `fly status` | Show running machines and their health |
| `fly ssh console` | Open a shell inside the running container |
| `fly ips list` | List allocated IP addresses |
| `fly wireguard list` | List active WireGuard tunnels |
