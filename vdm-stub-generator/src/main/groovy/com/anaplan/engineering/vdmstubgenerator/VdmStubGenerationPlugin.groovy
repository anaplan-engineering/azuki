package com.anaplan.engineering.vdmstubgenerator


import org.gradle.api.Plugin
import org.gradle.api.Project

class VdmStubGenerationPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.tasks.create([
            "name" : "generateVdmStubs",
            "type" : VdmStubGenerationTask.class,
            "group": "vdm"
        ])

    }
}
