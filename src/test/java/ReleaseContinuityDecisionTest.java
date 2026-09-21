public final class ReleaseContinuityDecisionTest {
    public static void main(String[] args) {
        CourseReleasePolicy policy = new CourseReleasePolicy();
        ReleaseContinuityService.BuildEvent build = new ReleaseContinuityService.BuildEvent(
                "Database migrations for teachers", "build-17");
        ReleaseContinuityService.ReleaseOperation release = new ReleaseContinuityService.ReleaseOperation(
                "lesson-search-v2", "release-17");
        ReleaseContinuityService.ReleaseDiagnostic diagnostic = policy.decide(build, release, true, "recharge configured");
        if (!"CONTINUE_RELEASE".equals(diagnostic.decision())) {
            throw new AssertionError("A configured recharge policy should allow the learning release to continue.");
        }
        System.out.println("decision test passed: " + diagnostic.decision());
    }
}
