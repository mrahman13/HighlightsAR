package com.nayeem.highlightsar

import android.app.ActivityManager
import android.content.Context
import android.support.v7.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private val openGLVersion by lazy {
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)

    }
}
