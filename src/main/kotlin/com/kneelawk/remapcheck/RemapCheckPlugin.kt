package com.kneelawk.remapcheck

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class RemapCheckPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("remapCheck", RemapCheckExtension::class, project)
        
        project.tasks.findByName("genSources")?.dependsOn("genSourcesWithVineflower")
    }
}