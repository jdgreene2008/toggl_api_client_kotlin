package com.jarvis.kotlin.utils

import com.jarvis.kotlin.domain.toggl.Task
import com.jarvis.kotlin.domain.toggl.User

class TogglUtils {

    companion object {

        fun getUserProjectName(projectId: Int?, user: User?): String? {
            if (user == null || user.projects == null) return null
            val projects = user.projects

            for (project in projects) {
                if (project.id == projectId) {
                    return project.name
                }
            }

            return null
        }

        fun getTask(projectId: Int, taskId: Int, user: User): Task? {
            val projects = user.projects
            for (project in projects) {
                if (project.id == projectId) {
                    return project.findTaskById(taskId)
                }
            }

            return null
        }

        fun getUserProjectColor(projectId: Int?, user: User?): String? {
            if (user == null || user.projects == null) return null
            val projects = user.projects

            for (project in projects) {
                if (project.id == projectId) {
                    return project.hexColor
                }
            }

            return null
        }

    }

}
