package com.kneelawk.remapcheck

import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

abstract class RemapCheckExtension(private val project: Project) {
    fun checkRemap(configure: Action<CheckRemapSpec>) {
        val spec = project.objects.newInstance(CheckRemapSpec::class)
        configure.execute(spec)

        val minecraftVersion = project.property("minecraft_version") as String?
            ?: throw IllegalStateException("Missing `minecraft_version` property")

        val target = project.evaluationDependsOn(spec.targetProject.get())

        val loomEx = project.extensions.getByType(LoomGradleExtensionAPI::class) as LoomGradleExtension

        project.dependencies {
            add("minecraft", "com.mojang:minecraft:$minecraftVersion")
            add("mappings", spec.targetMapping.get())
        }

        project.tasks.apply {
            create(spec.taskNameBase.get(), RemapJarTask::class) {
                classpath.from(loomEx.getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY))
                dependsOn(target.tasks.named("remapJar"))

                archiveClassifier.set(spec.taskNameBase)

                inputFile.set(target.tasks.named("remapJar", RemapJarTask::class).flatMap { it.archiveFile })
                sourceNamespace.set("intermediary")
                targetNamespace.set("named")

                remapperIsolation.set(true)
            }
        }
    }
}