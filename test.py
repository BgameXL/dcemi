#!/usr/bin/env python3
"""Smoke test for a running DCEMI container.

  1. connect,
  2. run /list to prove the index is populated,
  3. run /recipe <item> to get recipe ids,
  4. run /render <id> and save the PNG to disk.

Usage:
    python3 test.py # iron_sword against localhost:25599
    python3 test.py iron_pickaxe # a different item
    python3 test.py iron_sword 1.2.3.4 25599
"""

import json
import socket
import struct
import sys
import time

DEFAULT_HOST = "127.0.0.1"
DEFAULT_PORT = 25599
OUT_FILE = "recipe.png"


def connect(host, port, retries=120, delay=1.0):
    last = None
    for attempt in range(retries):
        try:
            sock = socket.create_connection((host, port), timeout=10)
            sock.settimeout(60)  # renders can take a seconds under GL
            return sock
        except OSError as e:
            last = e
            if attempt == 0:
                print(f"waiting for DCEMI at {host}:{port} (is the container up and DCEMI_READY?) ...")
            time.sleep(delay)
    raise ConnectionError(f"could not connect to {host}:{port} after {retries} tries: {last}")


def send_line(sock, line):
    sock.sendall((line + "\n").encode("utf-8"))


def read_line(sock):
    buf = bytearray()
    while True:
        b = sock.recv(1)
        if not b:
            raise ConnectionError("connection closed before a full line was read")
        if b == b"\n":
            break
        buf += b
    return buf.decode("utf-8")


def read_exactly(sock, n):
    buf = bytearray()
    while len(buf) < n:
        chunk = sock.recv(n - len(buf))
        if not chunk:
            raise ConnectionError("connection closed mid-frame")
        buf += chunk
    return bytes(buf)


def read_render(sock):
    n = struct.unpack(">I", read_exactly(sock, 4))[0]
    if n == 0:
        return None
    return read_exactly(sock, n)


def main():
    args = sys.argv[1:]
    item = args[0] if len(args) >= 1 else "iron_sword"
    host = args[1] if len(args) >= 2 else DEFAULT_HOST
    port = int(args[2]) if len(args) >= 3 else DEFAULT_PORT

    sock = connect(host, port)
    print(f"connected to {host}:{port}\n")

    try:
        # /list - prove the EMI index is populated
        send_line(sock, f"/list {item}".strip())
        listed = json.loads(read_line(sock))
        if isinstance(listed, dict) and "error" in listed:
            print(f"/list error: {listed['error']}")
            return 1
        print(f"/list {item!r} -> {len(listed)} matching stacks"
              + (f" (e.g. {listed[:3]})" if listed else ""))

        # /recipe - get recipe ids that outputs this item
        send_line(sock, f"/recipe {item}")
        recipes = json.loads(read_line(sock))
        if isinstance(recipes, dict) and "error" in recipes:
            print(f"/recipe error: {recipes['error']}")
            return 1
        print(f"/recipe {item!r} -> {len(recipes)} recipe(s)")
        if not recipes:
            print("no recipes to render; nothing more to test.")
            return 0
        rid = recipes[0]
        print(f"rendering first recipe: {rid}")

        # /render - get the png bytes and save them
        png = read_render_for(sock, rid)
        if png is None:
            print("render returned an error frame (length 0)")
            return 1
        with open(OUT_FILE, "wb") as f:
            f.write(png)
        print(f"\nOK — saved {OUT_FILE} ({len(png):,} bytes)")
        print(f"open it to confirm the recipe rendered: xdg-open {OUT_FILE}")
        return 0
    finally:
        sock.close()


def read_render_for(sock, rid):
    send_line(sock, f"/render {rid}")
    return read_render(sock)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (ConnectionError, OSError) as e:
        print(f"\nconnection problem: {e}", file=sys.stderr)
        sys.exit(2)
    except KeyboardInterrupt:
        sys.exit(130)
