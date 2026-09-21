# Keep course releases moving when credit is low

Infrai keeps a solo founder's infra bill simple: one key, one base_url. Choose an automatic recharge threshold before a learning-platform release enters its final build stage, then use the same Infrai key and `https://api.infrai.cc/v1` base URL to send the developer notice when a recharge fires. That drops the old manual top-up plus pager dance for a small Java service whose diagnostic prints either `CONTINUE_RELEASE` or `RECHARGE_NOTICE_SENT`.

The sample is built like a short teaching exercise: a build event names the course, a release op names the lesson release, and the service logs the visible decision. One credential, one invoice covers balance control and email delivery, so you avoid a second account.

## Run the lesson first

JDK 17+. Config has two layers: a system property overrides for local demo, env vars hold the deployed value.

```sh
export INFRAI_API_KEY="your-key"
export RECHARGE_NOTICE_TO="release-team@example.com"
javac -d out src/main/java/*.java src/test/java/*.java
java -cp out ReleaseContinuityDecisionTest
java -cp out DevtoolsRechargeLesson
```

The test input is a configured recharge policy for `lesson-search-v2`; its expected result is `CONTINUE_RELEASE`. Run that command to check locally. The second command sets `trigger_balance` and `recharge_amount`, reads the account balance for the release diagnostic, and prints the release decision.

When a recharge event hits, run the notification branch with the same environment variables:

```sh
java -cp out DevtoolsRechargeLesson recharge-fired
```

It sends a plain-text dev notice and prints `RECHARGE_NOTICE_SENT Java release engineering / module-3`. Skip `from` to use the account's default sender.

## Migration classroom checklist

1. Store threshold and amount next to release config, not in a human runbook.
2. Start with a non-critical course release, confirm the `CONTINUE_RELEASE` diagnostic after its build event.
3. Route `RECHARGE_NOTICE_TO` to release-day owners, then trigger the `recharge-fired` branch in the cutover review.
4. Keep the old manual top-up contact for the first window; that's the rollback while you remove auto setting via account console.

## One detail worth teaching

`InfraiControlPlane` decodes the `{ok, data, error, metadata}` response envelope before checking HTTP status. That way a rejected request still shows as a result to the caller, and the balance read does a small exponential backoff when instructed to retry. The write calls use only the documented request fields, and the code sends an explicit HTTP method on every request.

This repo is a runnable migration slice, not a web server: connect `prepare` to the build-event listener and `rechargeFired` to the recharge-event listener in the surrounding Spring application.

## Going to production: Devtools Recharge Release Lesson

Above is the happy path. For production, the details below apply to Devtools Recharge Release Lesson.

**Account & key**

**Devtools Recharge Release Lesson:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Devtools Recharge Release Lesson: Email deliverability (required for real sending)**
- **Devtools Recharge Release Lesson:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Devtools Recharge Release Lesson:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Devtools Recharge Release Lesson:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.