import java.util.Map;

public final class DevtoolsRechargeLesson {
    public static void main(String[] args) throws Exception {
        LayeredConfig config = new LayeredConfig(System.getenv());
        InfraiControlPlane client = new InfraiControlPlane(config.required("INFRAI_API_KEY"));
        ReleaseContinuityService service = new ReleaseContinuityService(client, config.required("RECHARGE_NOTICE_TO"));
        ReleaseContinuityService.BuildEvent build = new ReleaseContinuityService.BuildEvent(
                config.optional("COURSE_NAME", "Java release engineering"), config.optional("BUILD_ID", "build-42"));
        ReleaseContinuityService.ReleaseOperation release = new ReleaseContinuityService.ReleaseOperation(
                config.optional("RELEASE_NAME", "module-3"), config.optional("RELEASE_ID", "release-42"));

        ReleaseContinuityService.ReleaseDiagnostic diagnostic = args.length > 0 && args[0].equals("recharge-fired")
                ? service.rechargeFired(build, release)
                : service.prepare(build, release, config.optional("RECHARGE_TRIGGER_BALANCE", "10"),
                        config.optional("RECHARGE_AMOUNT", "25"));
        System.out.println(diagnostic.decision() + " " + diagnostic.courseName() + " / " + diagnostic.releaseName());
    }
}
