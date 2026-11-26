# Maestro regression flows

End-to-end flows for the sample app, written with [Maestro](https://maestro.dev). They run unattended and
check behaviour through the real UI. Today they cover the **Network Proxy** tool
(`tool-networkproxy-okhttp`); put flows for other tools in their own folder under `flows/`.

## What you need on a machine

| Requirement | Notes |
|---|---|
| JDK 17+ | Maestro needs it. |
| Android SDK | `adb` and the emulator on your `PATH` (or `ANDROID_HOME` set). |
| Maestro | Written and run with **2.0.10**. Pin one version in CI and upgrade it in its own PR. Newer releases read the `properties: junitClassname` entry for JUnit grouping, older ones may ignore it. |
| An emulator or device | API 35, **system locale en-US**, light mode, font scale 1.0, **at least 3 GB RAM**. With 1.5 GB the emulator swaps heavily and you get ANR dialogs and flaky timeouts. |
| Internet | Only for flows tagged `network`: the sample calls `https://httpbin.org/get`. |

The flows select text (for example `"Network Proxy"`, `"Path must start with /"`), so the device language has to
be English. Everything else uses view ids.

Prepare the device before a run:

```bash
adb shell cmd uimode night no
adb shell settings put system font_scale 1.0
```

## Running

`maestro test` does not install the app. Install the sample first:

```bash
./gradlew :sample:installDebug

maestro test .maestro/                                          # everything
maestro test .maestro/flows/networkproxy/rule_delete.yaml       # one flow
maestro test --include-tags=smoke .maestro/                     # the fast set
maestro test --exclude-tags=network .maestro/                   # no internet needed
```

For CI, add a JUnit report and keep the artifacts (screenshots, logs, view hierarchy of failed steps):

```bash
maestro test --include-tags=regression \
  --format junit --output build/maestro/report.xml \
  --test-output-dir build/maestro/artifacts .maestro/
```

Check syntax without a device (every flow and subflow):

```bash
find .maestro/flows .maestro/subflows -name '*.yaml' -print0 | xargs -0 -n1 maestro check-syntax
```

## Layout

```
.maestro/
  config.yaml                  workspace config: which flows run, excluded tags, animations off
  flows/networkproxy/          one user journey per file
  subflows/                    reusable steps, never run on their own
```

### Flows

| Flow | Tags | Needs internet |
|---|---|---|
| `mock_rule_returns_body` | smoke, regression | no |
| `mock_error_status_fails_the_call` | smoke, regression | no |
| `global_switch_off_bypasses_rules` | regression | no (see below) |
| `rule_switch_off_bypasses_rule` | regression | no (see below) |
| `rule_validation` | regression | no |
| `rule_delete` | regression | no |
| `rule_persists_after_restart` | regression | no |
| `body_editor_format` | regression | no |
| `recent_call_becomes_rule` | regression, network | **yes** |
| `clear_recent_calls` | regression, network | **yes** |

Mock rules never reach the network, which is what keeps most flows hermetic. The two "switch off" flows do make a
real call in their second half. They wait for the call to finish, then assert that the mocked body is **not** shown,
which holds online (real response) and offline (failure message).

### Subflows

| Subflow | Does |
|---|---|
| `launch_clean` | Cold start with cleared data, waits for Sentinel's sheet. |
| `open_network_proxy` | From the open sheet, opens Network Proxy. |
| `reopen_network_proxy` | From the sample's main screen (scrolls up if needed), opens the sheet, then Network Proxy. |
| `dismiss_sentinel` | Closes the sheet to reach the sample's main screen. |
| `close_sentinel` | From the proxy list, goes back to the sample's main screen. |
| `enable_proxy` | Switches the proxy on and asserts it is on. |
| `add_mock_rule` | Creates a Mock rule. Env: `RULE_PATH`, `RULE_STATUS`, `RULE_BODY` (empty string for none). |
| `fetch_httpbin` | Scrolls to and taps the sample's "Fetch request details (HTTPBin)" button. |
| `format_body` | On the body editor, taps Format, from the toolbar or from the overflow menu. |

## Things that are easy to trip over

- **Sentinel opens its own sheet on every cold start of the sample.** After `launchApp` you land on the tools
  list, not the main screen. Use `open_network_proxy` right after launch, and `dismiss_sentinel` when you need
  the main screen.
- **`enabledSwitch` is used by the toolbar switch and by every rule row.** Pick the one you mean with
  `childOf: { id: "toolbar" }` or `childOf: { id: "recyclerView" }`. Do not combine `index` with `checked`:
  Maestro applies the index after filtering, so the assertion can look for a match that does not exist.
- **The main screen scrolls.** After `fetch_httpbin` the "Show manually now" button is off screen, which is why
  `reopen_network_proxy` scrolls up first.
- **Menu items are found by id** (`save`, `format`). The overflow menu is found by its description
  `"More options"`, which is English-only. `format` is shown in the toolbar only if there is room, so tap it
  through `format_body`.
- **`text:` is a regex, but an exact match also passes.** Paths with `*` (for example `/persist/*`) are written as
  is, which is also how `add_mock_rule` passes `RULE_PATH` to its assertion.

## Writing a new flow

1. One user journey per flow, ending with assertions on the outcome. Check values (`id` plus `text`), not only
   that something is on screen.
2. Start from `launch_clean` unless the journey is about persisted state.
3. Use `id:` selectors. No coordinates. No fixed delays: `assertVisible` and `tapOn` already wait up to 17 s, and
   `extendedWaitUntil` with a `timeout` covers longer waits.
4. Pair every `assertNotVisible` with an earlier `assertVisible` on the same selector. On its own it also passes
   when the id is misspelled.
5. No AI commands (`assertWithAI` and friends) in regression flows.
6. Run `maestro check-syntax`, then run the flow several times in a row. Then **prove it can fail**: skip the
   action step (for example leave out `enable_proxy`) and confirm the flow fails at its outcome assertion.
7. Tag it: `smoke` (fast, critical, also `regression`), `regression`, `network` (needs internet), `wip` and
   `quarantine` (both excluded by `config.yaml`; a flaky flow goes to `quarantine` with a ticket, not behind
   retries).

## What these flows cannot cover

Maestro cannot rotate the screen or kill the process while the app is in the background. These Network Proxy
behaviours need an instrumented or unit test instead:

- a rule's body surviving process death while the editor is open,
- the Method dropdown after a configuration change,
- the `Content-Type` header of a mocked response (the app only sees the body text).

## Troubleshooting

- **`Element not found: showSentinel` right after launch:** Sentinel's sheet is covering the main screen. See the
  first item under "Things that are easy to trip over".
- **Flows pass on one machine and time out on another:** check emulator RAM first, then CPU architecture and
  the system locale.
- **"Sentinel Sample isn't responding" dialog:** the emulator is starved. Two causes seen so far: too little RAM
  (1.5 GB), and the Mac's display being off or locked during a run, which throttles the emulator. Give it 3 GB or
  more and keep the display awake: `caffeinate -d -i -s maestro test .maestro/` (prevents display sleep and
  system sleep while it runs; do not lock the screen by hand).
- **Where to look after a failure:** the `--test-output-dir` folder has one folder per run, with step
  screenshots, logs and the view hierarchy. Without that flag Maestro writes to `~/.maestro/tests`.
