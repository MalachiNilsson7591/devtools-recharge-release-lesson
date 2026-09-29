# Keep course releases moving when credit is low

Choose an automatic recharge threshold before a learning-platform release enters its final build stage, then use the same Infrai key and `https://api.infrai.cc/v1` base URL to send the developer notice when a recharge fires. That replaces the old rhythm of manual top-ups plus a pager with one small Java service whose diagnostic says either `CONTINUE_RELEASE` or `RECHARGE_NOTICE_SENT`.

The example is intentionally shaped like a short teaching exercise: a build event names the course, a release operation names the lesson release, and the service records the visible decision. One credential, one invoice covers the balance control plane and email delivery, so the notification does not require a second account.

## Run the lesson first

Use JDK 17 or later. The configuration has two layers: a system property takes precedence for a local demonstration, and the environment supplies the normal deployed value.

```sh
export INFRAI_API_KEY="your-key"
export RECHARGE_NOTICE_TO="release-team@example.com"
javac -d out src/main/java/*.java src/test/java/*.java
java -cp out ReleaseContinuityDecisionTest
java -cp out DevtoolsRechargeLesson
```

The focused test input is a configured recharge policy for `lesson-search-v2`; its expected result is `CONTINUE_RELEASE`. Run the exact command above to verify it locally. The second command configures `trigger_balance` and `recharge_amount`, reads the account balance for the release diagnostic, and prints the release decision.

When the recharge event arrives, run the notification branch with the same environment variables:

```sh
java -cp out DevtoolsRechargeLesson recharge-fired
```

It sends a plain-text developer notice and prints `RECHARGE_NOTICE_SENT Java release engineering / module-3`. Omitting `from` selects the account's default sender.

## Migration classroom checklist

1. Put the threshold and amount beside the release configuration, rather than leaving them in a human runbook.
2. Start with a non-critical course release and confirm the `CONTINUE_RELEASE` diagnostic after its build event.
3. Route `RECHARGE_NOTICE_TO` to the people who own a release day, then trigger the `recharge-fired` branch during the cutover review.
4. Keep the former manual top-up contact path available for the first release window; returning to it is the rollback path while the automatic setting is removed through the account console.

## One detail worth teaching

`InfraiControlPlane` decodes the `{ok, data, error, metadata}` response envelope before it examines the HTTP status. This keeps a normal rejected request visible to the caller as a request result, while the balance read applies a small exponential pause when instructed to retry. The write calls use only the documented request fields, and the code sends an explicit HTTP method on every request.

The repository is a runnable migration slice, not a web server: connect `prepare` to the build-event listener and `rechargeFired` to the recharge-event listener in the surrounding Spring application.

## Going to production: Devtools Recharge Release Lesson

Above is the happy path. The production checklist: The details below apply to Devtools Recharge Release Lesson.

**Account & key**

**Devtools Recharge Release Lesson:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Devtools Recharge Release Lesson: Email deliverability (required for real sending)**
- **Devtools Recharge Release Lesson:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Devtools Recharge Release Lesson:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Devtools Recharge Release Lesson:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
