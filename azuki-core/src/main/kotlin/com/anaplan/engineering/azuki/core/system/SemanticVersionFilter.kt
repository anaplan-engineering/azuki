package com.anaplan.engineering.azuki.core.system

import org.semver4j.Semver


class SemanticVersionFilter(verificationVersion: String) : Implementation.VersionFilter {

    private val verificationVersion = Semver(verificationVersion)

    override fun canVerify(scenarioVersion: String?) = if (scenarioVersion == null) {
        true
    } else {
        Semver(scenarioVersion).isLowerThanOrEqualTo(verificationVersion)
    }

}
