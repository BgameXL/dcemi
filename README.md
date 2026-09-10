# DCEMI

**Headless Minecraft as an EMI recipe-image server.**

DCEMI is a mod that boots Minecraft with no visible window,
loads a dummy world, disables everything except [EMI](https://emi.dev/), and
exposes a TCP command server. You ask for a recipe over a socket and it
renders that recipe to a **PNG** and streams the bytes back. It's the render engine behind a Discord bot that posts recipe images.

```
your code --TCP :25599 -> headless Minecraft (Docker, GL) -> PNG bytes
```

---

## build, run, render

**1. Build the portable image**:

```bash
docker build -f docker/Dockerfile.portable -t dcemi:latest .
```

**2. Run it**: the `-t` is obligatory, without a TTY, Minecraft stdout is
block buffered and the `DCEMI_READY` line below won't show up in time:

```bash
docker run -d -t -p 25599:25599 --name dcemi dcemi:latest
```

**3. Wait for it to be ready** Watch until `DCEMI_READY` appears:

```bash
docker logs -f dcemi
```

**4. Render a recipe**:

```bash
python3 test.py
```

You should see something like:

```
connected to 127.0.0.1:25599

/list 'iron_sword' -> 1 matching stacks (e.g. ['minecraft:iron_sword'])
/recipe 'iron_sword' -> 16 recipe(s)
rendering first recipe: emi:/crafting/repairing/minecraft/iron_sword

OK - saved recipe.png (8,042 bytes)
open it to confirm the recipe rendered: xdg-open recipe.png
```

Open `recipe.png` - that's a recipe rendered by the headless Minecraft.

**Cleanup:**

```bash 
docker rm -f dcemi # It removes it entirely. Required to restart `docker run --name dcemi` fails if a container with that name already exists (even if it's stopped). 
# So before running a new `docker run` command, run `docker rm -f dcemi` to remove the old one.
```

```bash
docker stop dcemi
```

```bash
docker rmi dcemi:latest # deletes the image and you will need to re-build it
```

---

## Sharing the image with others

Building the image downloads ~5 GB and takes a while. Once you've built it once,
you can share the finished image to someone else so they skip the build entirely.

**You** export the built image to a compressed tarball:

```bash
docker save dcemi:latest | gzip > dcemi-portable.tar.gz
```

That produces one ~1.4 GB file.

**They** need only Docker + an x86-64 Linux host:

```bash
docker load  -i dcemi-portable.tar.gz
docker run -d -t -p 25599:25599 --name dcemi dcemi:latest
docker logs -f dcemi
```

Then they use it without need to build the image.

> Alternatively, `docker push` to a registry lets users `docker pull` it more convenient.
> The image bakes Minecraft inside it, sharing it privately is fine, publishing it on a public registry is a
> copyright almost gray area. If you care, you should share the tarball with people you know, or let it build their own.

---

## The command protocol

The mod opens a TCP server on `0.0.0.0:25599` **once EMI index is ready**.
One request → one full response, serialized per connection:

| Command                 | Response                                                                                                          |
|-------------------------|-------------------------------------------------------------------------------------------------------------------|
| `/list [query]`         | one JSON array line of stack ids (substring match)                                                                |
| `/recipe <item> [uses]` | one JSON array line of recipe ids (`uses` = recipes that *consume* the item; default = recipes that *produce* it) |
| `/render <recipe_id>`   | `[4-byte big-endian length][PNG bytes]` — length `0` means error                                                  |

Errors on the JSON commands come back as `{"error": "..."}`.

Config via env vars: `DCEMI_PORT`, `DCEMI_BIND`, `DCEMI_SCALE`.

---

## Using in the Discord bot

The bot is a [Sonny](https://github.com/driftbluestone/sonny) extension at
`tagbot/extensions/dcemi/`. It gives three slash command. Because it runs
inside Sonny, standing it up also requires Sonny's own dependencies.

**If using Sonny, you will need to follow its instructions**

---

## Development (running from source)

For iterating on the mod locally you can run it straight from Gradle

```bash
./gradlew runClient
```

`DCEMI_READY` prints once you're in the world and EMI has loaded, the socket
opens on `25599` just like the container.