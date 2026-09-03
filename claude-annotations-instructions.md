# Annotation fix routine

Fix each requested change in the code. Open a pull request (PR) that
describes each fix. Stay on the PR. Watch it for comments and reply to them.

## Input

This turn's text is the batch: a JSON array of annotation objects. Parse it.
If the array is empty, say so. Stop.

### Annotation fields

| Field | Use |
|---|---|
| `id` | Stable id. Link each fix to its annotation in the PR body. |
| `note` | The requested change. This is the task. |
| `toolkit` | `"view"` or `"compose"`. Tells you which locating strategy applies. |
| `targetType` | View class simple name, or the composable function name — or, for a Compose target, the *hosting* `ComposeView`/`AndroidComposeView` class name. See "A note on Compose targets" below. |
| `targetPath` | Ancestor chain, broadest first, e.g. `CoordinatorLayout>MaterialCardView>MaterialButton`. |
| `viewId` | Resource entry name of the view id, greppable as `R.id.<viewId>` and `@+id/<viewId>`. `null` for Compose. |
| `testTag` | `Modifier.testTag` value, when set. `null` for Views. |
| `sourceLocation` | `file:line` of the composable. Check this first for a Compose target — but see "A note on Compose targets" below, it is usually `null`. |
| `ordinal` / `ordinalCount` | Position "N of M" among same-type elements on the screen, top to bottom then left to right. Use this when there is no id or tag. |
| `text` | Visible text or content description, used to disambiguate identical siblings. |
| `screen` | Simple name of the Activity the target lives in. The main grouping key. |
| `fragment` | Simple name of the deepest Fragment containing the target. The secondary grouping key. |
| `route` | Navigation destination route when androidx-navigation is present. `null` otherwise. |

The batch also carries `rebindX`, `rebindY` (screen coordinates Sentinel uses to re-locate the
element on device; not useful for locating source) and `isSent` (always `true` for anything in
this batch). Ignore them.

### A note on Compose targets

Per-composable resolution does not currently work on the Compose versions Sentinel has been
verified against. When `toolkit` is `"compose"`, `sourceLocation` will normally be `null` and
`targetType` will be `ComposeView` or `AndroidComposeView` — the view *hosting* the composition,
not the tapped composable itself. To locate the actual composable:

1. If `viewId` is set (the app gave its `ComposeView` an `android:id`), search for
   `R.id.<viewId>` and `@+id/<viewId>` — this finds the file that calls `setContent`, which is
   your starting point.
2. Otherwise use `screen` and `fragment` to find the right Activity or Fragment, then open its
   Compose content and use `note`, `text` and `ordinal` to pick the right composable by reading
   the code — there is no id to grep for.
3. If `sourceLocation` is set (an older, unverified Compose version), trust it — it is the exact
   `file:line` of the composable.

## Workflow

1. Parse the input array.
2. If the array is empty, report "No annotations in this batch". Stop.
3. Group the annotations. See "Group the work".
4. Optional: track progress with the task board, one task per annotation id.
5. Dispatch one fix subagent per group. See "Fix subagent prompt".
6. Run independent groups in parallel.
7. Collect each subagent's report: the file:line changed, a one-line summary,
   and any annotation it could not locate.
8. Create a branch.
9. Commit the changes.
10. If the remote name is not `origin`, run `git remote -v` first.
11. Push the branch.
12. Open a PR with `gh pr create`.
13. Build the PR body from the collected reports. See "PR body".
14. Enable notifications on the PR. See "After the PR opens".

## Group the work

Read all annotations before you split the work. Group by `screen` first,
then by `fragment`.

- Put related edits on one screen in one subagent. This avoids collisions.
- Give independent screens their own subagent. Dispatch these in parallel.
- A small batch across a few screens can stay one subagent.

Hard rule: two subagents must never edit the same file at the same time.

## Fix subagent prompt

Dispatch one `Agent` (`general-purpose`) per group with this prompt. Add the
annotation list for that group.

```
A user annotated one or more elements in an Android app. Apply each requested
change to the code. The edit on disk is the deliverable.

Annotations in this group:
- id: <id>
  note (the requested change): "<note>"
  toolkit: <toolkit>
  targetType: <targetType>
  targetPath: <targetPath>
  viewId: <viewId, or "none">
  testTag: <testTag, or "none">
  sourceLocation: <sourceLocation, or "unknown">
  position: <ordinal> of <ordinalCount> same-type elements on the screen
  text: <text, or "none">
  screen: <screen>   fragment: <fragment>   route: <route>
- (repeat for each annotation in this group)

Steps:
1. Locate the exact element for each annotation. For a View target, search
   for R.id.<viewId> and @+id/<viewId> first. For a Compose target, check
   sourceLocation first; when it is missing, targetType names the hosting
   ComposeView/AndroidComposeView rather than the composable — search for its
   viewId if it has one, otherwise use screen and fragment to find the right
   file and pick the composable by reading the code, narrowed with text or
   the ordinal position. Do not guess past what the fields support.
2. Apply the change the note asks for. Keep the edit small. Match the
   surrounding code style.
3. Run `./gradlew detekt lintDebug` on the changed modules. Fix any new issue
   you caused.
4. Report, for each annotation id: the file:line you changed, a one-line
   summary of the edit, and whether detekt/lint is clean. If you could not
   locate an element safely, say so. Do not guess. This report feeds the PR
   body.
```

## PR body

Map each annotation to its fix:

```
### <note>   (id: <id>)
- Annotated: <targetType> on <screen> (<fragment>) at <sourceLocation, viewId, or ordinal>
- Fixed: <file:line> — <one-line description of the edit>
```

Group entries by screen when that matches the split of work. Add a short
summary line at the top (e.g. "N annotations across M screens. `detekt` and
`lintDebug` clean."). List any annotation you could not locate under a **Not
fixed** heading, with the reason.

## After the PR opens

Stay engaged. Answer review feedback.

1. Get the repository name and PR number:
   `gh repo view --json nameWithOwner -q .nameWithOwner`
   `gh pr view --json number -q .number`
2. Enable notifications on the PR:
   `gh api -X PUT repos/<owner>/<repo>/issues/<pr_number>/subscription -f subscribed=true -f ignored=false`
3. Check for comments:
   - General comments: `gh pr view <pr_number> --comments`
   - Inline review comments: `gh api repos/<owner>/<repo>/pulls/<pr_number>/comments`
4. For each new comment:
   - If it asks for a code change, make the change. Commit it. Push it to
     the same branch. This updates the PR in place.
   - Reply to the comment. Use `gh pr comment <pr_number> --body "<reply>"`
     for a general reply. Reference the commit you pushed. For an inline
     comment, reply in its own thread with
     `gh api repos/<owner>/<repo>/pulls/<pr_number>/comments/<comment_id>/replies -f body="<reply>"`.
   - If a comment only asks a question, answer it in a reply. Do not push a
     commit for it.
5. Repeat step 3 and 4 while the routine runs.
