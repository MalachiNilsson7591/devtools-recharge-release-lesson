import java.io.IOException;

final class ReleaseContinuityService {
    private final InfraiControlPlane infrai;
    private final String notificationRecipient;
    private final CourseReleasePolicy policy = new CourseReleasePolicy();

    ReleaseContinuityService(InfraiControlPlane infrai, String notificationRecipient) {
        this.infrai = infrai;
        this.notificationRecipient = notificationRecipient;
    }

    ReleaseDiagnostic prepare(BuildEvent build, ReleaseOperation release, String triggerBalance, String rechargeAmount)
            throws IOException, InterruptedException {
        infrai.configureRecharge(triggerBalance, rechargeAmount);
        String balanceEnvelope = infrai.readBalance();
        return policy.decide(build, release, true, balanceEnvelope);
    }

    ReleaseDiagnostic rechargeFired(BuildEvent build, ReleaseOperation release) throws IOException, InterruptedException {
        String messageEnvelope = infrai.sendRechargeNotice(notificationRecipient, release.releaseName());
        return ReleaseDiagnostic.notified(build.courseName(), release.releaseName(), messageEnvelope);
    }

    record BuildEvent(String courseName, String buildId) { }
    record ReleaseOperation(String releaseName, String operationId) { }

    record ReleaseDiagnostic(String courseName, String releaseName, String decision, String evidence) {
        static ReleaseDiagnostic ready(String course, String release, String balance) {
            return new ReleaseDiagnostic(course, release, "CONTINUE_RELEASE", balance);
        }
        static ReleaseDiagnostic notified(String course, String release, String message) {
            return new ReleaseDiagnostic(course, release, "RECHARGE_NOTICE_SENT", message);
        }
    }
}
