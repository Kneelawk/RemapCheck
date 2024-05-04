package com.kneelawk.remapcheck

import org.gradle.api.provider.Property

abstract class CheckRemapSpec {
    abstract val targetProject: Property<String>

    fun targetProject(target: String) {
        targetProject.set(target)
    }

    abstract val targetMapping: Property<Any>

    fun targetMapping(target: Any) {
        targetMapping.set(target)
    }

    abstract val taskNameBase: Property<String>

    init {
        taskNameBase.convention("checkRemap")
    }

    fun tasknameBase(base: String) {
        taskNameBase.set(base)
    }
}