final class CourseReleasePolicy {
    ReleaseContinuityService.ReleaseDiagnostic decide(
            ReleaseContinuityService.BuildEvent build,
            ReleaseContinuityService.ReleaseOperation release,
            boolean automaticRechargeConfigured,
            String evidence) {
        if (automaticRechargeConfigured) {
            return ReleaseContinuityService.ReleaseDiagnostic.ready(build.courseName(), release.releaseName(), evidence);
        }
        return new ReleaseContinuityService.ReleaseDiagnostic(build.courseName(), release.releaseName(), "HOLD_RELEASE", evidence);
    }
}
