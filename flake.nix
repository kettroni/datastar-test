{
  description = "A very basic flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
    utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, utils }:
    utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        chrome-mcp = pkgs.buildNpmPackage {
          pname = "chrome-mcp";
          version = "1.0.0";
          src = pkgs.fetchurl {
            url = "https://registry.npmjs.org/chrome-mcp/-/chrome-mcp-1.0.0.tgz";
            sha256 = "092ffrrnhb4s1wiby7y597b73lnwhv2z4q1f1xs2540ymfwxc0na";
          };
          nativeBuildInputs = [ pkgs.jq pkgs.makeWrapper ];
          postPatch = ''
            cp ${./package-lock.json} package-lock.json
            # Remove lifecycle scripts (prepare calls tsc which is not available at build time;
            # the tarball already ships pre-built JS in build/).
            # Also normalise bin to a plain string so the nixpkgs npm-install-hook's
            # jq query (.bin[0]) doesn't fail on an object value.
            jq 'del(.scripts) | if (.bin | type) == "object" then .bin = (.bin | to_entries[0].value) else . end' \
              package.json > package.json.new
            mv package.json.new package.json
            # Patch chrome-launcher flags to always use headless mode so
            # chrome-mcp self-manages a headless browser with no display needed.
            substituteInPlace build/chrome-controller.js \
              --replace-fail "chromeLauncher.launch({" \
                             "chromeLauncher.launch({ port: 9222," \
            ; substituteInPlace build/chrome-controller.js \
              --replace-fail "'--remote-debugging-port=9222'," \
                             "'--headless=new',"
            # Fix CDP.List() throwing ECONNREFUSED (instead of returning []) when
            # Chrome is not yet running, which prevents the launcher fallback.
            substituteInPlace build/chrome-controller.js \
              --replace-fail "const targets = await CDP.List();" \
                             "let targets = []; try { targets = await CDP.List(); } catch (_) {}"
          '';
          npmDepsHash = "sha256-KNc/Xzi4qUb6FmTqRmZj+aZFP6ViSYa/c2iIwnbFNPk=";
          dontNpmBuild = true;
          postInstall = ''
            wrapProgram $out/bin/chrome-mcp \
              --set CHROME_PATH "${pkgs.chromium}/bin/chromium"
          '';
        };
      in
        {
          devShell = with pkgs; mkShell {
            buildInputs = [
              babashka
              nodejs
              chromium
              clojure
              clojure-lsp
              chrome-mcp
            ];
            shellHook = ''
              export CHROME_PATH="${pkgs.chromium}/bin/chromium"
              # Launch headless Chrome on a fixed port if not already running
              if ! ${pkgs.curl}/bin/curl -sf http://localhost:9222/json/version > /dev/null 2>&1; then
                ${pkgs.chromium}/bin/chromium \
                  --headless=new \
                  --remote-debugging-port=9222 \
                  --no-first-run \
                  --no-default-browser-check \
                  --disable-extensions \
                  --disable-background-networking \
                  --disable-sync \
                  --no-sandbox \
                  about:blank &>/dev/null &
                disown
                echo "Started headless Chromium on port 9222 (PID $!)"
              else
                echo "Headless Chromium already running on port 9222"
              fi
            '';
          };
        }
    );
}
