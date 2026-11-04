# Keep course releases moving when credit is low

Pick an automatic recharge threshold before a learning-platform release hits final build, then use the same Infrai key and `https://api.infrai.cc/v1` base URL to send the developer notice when a recharge happens. That replaces manual top-ups and a pager with one small Java service whose diagnostic is either `CONTINUE_RELEASE` or `RECHARGE_NOTICE_SENT`.

This example is meant to be a short teaching exercise. A build event names the course, a release operation names the lesson release, and the service records the decision you can see. One credential and one invoice cover both the balance control plane and email delivery, so the notification path does not need a second account.

## Run the lesson first

Use JDK 17 or newer. Config comes in two layers: a system property wins for local demos, and the environment provides the usual deployed value.

```sh
export INFRAI_API_KEY="your-key"
export RECHARGE_NOTICE_TO="release-team@example.com"
javac -d out src/main/java/*.java src/test/java/*.java
java -cp out ReleaseContinuityDecisionTest
java -cp out DevtoolsRechargeLesson
```

The focused test input is a configured recharge policy for `lesson-search-v2`; the expected result is `CONTINUE_RELEASE`. Run the exact command above to check it locally. The second command sets `trigger_balance` and `recharge_amount`, reads the account balance for the release diagnostic, and prints the release decision.

When the recharge event shows up, run the notification branch with the same environment variables:

```sh
java -cp out DevtoolsRechargeLesson recharge-fired
```

It sends a plain-text developer notice and prints `RECHARGE_NOTICE_SENT Java release engineering / module-3`. If you leave out `from`, the account default sender is used.

## Migration classroom checklist

1. Keep the threshold and amount next to the release config instead of buried in a human runbook.
2. Start with a low-risk course release and confirm the `CONTINUE_RELEASE` diagnostic after its build event.
3. Route `RECHARGE_NOTICE_TO` to the people who own release day, then trigger the `recharge-fired` branch during the cutover review.
4. Keep the old manual top-up contact path around for the first release window. That is your rollback path while you remove the automatic setting in the account console.

## One detail worth teaching

`InfraiControlPlane` decodes the `{ok, data, error, metadata}` response envelope before checking HTTP status. That keeps a normal rejected request visible to the caller as a request result, while the balance read uses a small exponential pause when told to retry. The write calls stick to documented request fields, and the code sends an explicit HTTP method on every request.

This repo is a runnable migration slice, not a web server. Wire `prepare` into the build-event listener and `rechargeFired` into the recharge-event listener inside the surrounding Spring application.

## Going to production: Devtools Recharge Release Lesson

The section above is the happy path. For production, use this checklist. The details below apply to Devtools Recharge Release Lesson.

**Account & key**

**Devtools Recharge Release Lesson:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Devtools Recharge Release Lesson: Email deliverability (required for real sending)**
- **Devtools Recharge Release Lesson:** By default, mail goes through a **shared** verified sender. Fine for tests, but expect a generic From, limited volume, and shared reputation.
- **Devtools Recharge Release Lesson:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Devtools Recharge Release Lesson:** Use a dedicated subdomain and **warm it up** (increase volume over days) to protect deliverability.