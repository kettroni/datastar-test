---
name: smoke-test
description: Run the bb smoke-test script to verify the chat HTTP endpoints are working against the running server on port 3000.
---

# Smoke Test

The project has a single utility script worth running from ECA:

```
bb smoke-test
```

It curls `GET /chat` and `POST /chat/send` and prints pass/fail for each.
The server must already be running on port 3000.

```
command: bb smoke-test
working_directory: /home/kettroni/projects/clojure/datastar-test
```

## Dev environment overview

Everything is started **outside of ECA** via the Nix dev shell (`flake.nix`):

- **Server** — started manually or via the REPL (`bb repl` / `bb server`)
- **nREPL** — started manually; ECA connects via `clojure-mcp` at startup
- **Headless Chromium** — launched once by the Nix `shellHook` on port 9222
- **chrome-mcp** — connects to Chromium at ECA startup

None of these need to be started or restarted by ECA. When they are broken the fix is always external (restart ECA, restart the shell, etc).

## chrome-mcp WebSocket dead

If `chrome-mcp` tools return WebSocket errors, only a full **ECA restart** fixes it.
`bb chrome-restart` does not help — chrome-mcp won't reconnect to the new Chromium
instance on its own.

When chrome-mcp is unavailable, test the app via the REPL using `ProcessBuilder`
to invoke `curl` (since `eca__shell_command` blocks paths like `/dev/null` and `/tmp`):

```clojure
(defn curl-status [& args]
  (let [pb (ProcessBuilder. (into ["curl"] args))
        _ (.redirectErrorStream pb true)
        proc (.start pb)
        out (slurp (.getInputStream proc))
        _ (.waitFor proc)]
    (clojure.string/trim out)))

;; GET /chat
(curl-status "-sf" "-o" "/dev/null" "-w" "%{http_code}" "http://localhost:3000/chat")

;; POST /chat/send
(curl-status "-s" "-w" "\nHTTP %{http_code}" "-X" "POST"
             "http://localhost:3000/chat/send"
             "-H" "Content-Type: application/json"
             "-d" "{\"username\":\"alice\",\"message\":\"hello\"}")
```

For SSE streaming use `-v` + `--max-time`:

```clojure
(let [pb (ProcessBuilder. ["curl" "-v" "--max-time" "2"
                           "http://localhost:3000/chat/subscribe"])
      _ (.redirectErrorStream pb true)
      proc (.start pb)
      _ (.waitFor proc 4000 java.util.concurrent.TimeUnit/MILLISECONDS)]
  (println (slurp (.getInputStream proc))))
```
