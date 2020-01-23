package me.pascal.modolotl.utils

import java.util.*

object UnmuteTasks {

    private val tasks = hashMapOf<String, TimerTask>()

    fun getTaskById(id: String) = tasks[id]

    fun addTask(id: String, task: TimerTask) = tasks.put(id, task)
}