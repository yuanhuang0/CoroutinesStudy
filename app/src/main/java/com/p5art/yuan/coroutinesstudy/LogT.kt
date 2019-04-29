package com.p5art.yuan.coroutinesstudy

import android.util.Log

fun logt(tag: String, msg: String){
    val currentThread = Thread.currentThread()
    Log.d("#$tag thread = ${currentThread.id}: ${currentThread.name}", msg)
}