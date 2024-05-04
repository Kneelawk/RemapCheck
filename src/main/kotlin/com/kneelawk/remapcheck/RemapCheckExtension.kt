package com.kneelawk.remapcheck

import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

abstract class RemapCheckExtension(private val project: Project) {
    fun applyTargetMapping(target: Any) {
        val minecraftVersion = project.property("minecraft_version") as String?
            ?: throw IllegalStateException("Missing `minecraft_version` property")

        project.dependencies {
            add("minecraft", "com.mojang:minecraft:$minecraftVersion")
            add("mappings", target)
        }
    }

    fun checkRemap(configure: Action<CheckRemapSpec>) {
        val spec = project.objects.newInstance(CheckRemapSpec::class)
        configure.execute(spec)

        val target = project.evaluationDependsOn(spec.targetProject.get())

        val loomEx = project.extensions.getByType(LoomGradleExtensionAPI::class) as LoomGradleExtension

        project.tasks.apply {
            val baseName = if (spec.taskNameBase.get().isBlank()) {
                "remapCheck"
            } else {
                spec.taskNameBase.get() + "RemapCheck"
            }

            val remapCheck = create(baseName, RemapJarTask::class) {
                classpath.from(loomEx.getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY))
                dependsOn(target.tasks.named("remapJar"))

                archiveClassifier.set(baseName)

                inputFile.set(target.tasks.named("remapJar", RemapJarTask::class).flatMap { it.archiveFile })
                sourceNamespace.set("intermediary")
                targetNamespace.set("named")

                remapperIsolation.set(true)
            }

            val remapCheckSource = create("${baseName}Source", RemapSourcesJarTask::class) {
                classpath.from(loomEx.getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY))
                dependsOn(target.tasks.named("remapSourcesJar"))

                archiveClassifier.set("${baseName}Sources")

                inputFile.set(
                    target.tasks.named("remapSourcesJar", RemapSourcesJarTask::class).flatMap { it.archiveFile })
                sourceNamespace.set("intermediary")
                targetNamespace.set("named")

                remapperIsolation.set(true)
            }

            named("assemble").configure { dependsOn(remapCheck, remapCheckSource) }
            target.tasks.named("assemble").configure { dependsOn(remapCheck, remapCheckSource) }
            target.tasks.findByName("publish")?.dependsOn(remapCheck, remapCheckSource)
        }
    }
}