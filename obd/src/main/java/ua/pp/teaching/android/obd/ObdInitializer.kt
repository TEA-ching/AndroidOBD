package ua.pp.teaching.android.obd

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import ua.pp.teaching.android.obd.statics.ObdLibrary

@Suppress("unused") //inited in the manifest
class ObdInitializer: Initializer<Context> {
    override fun create(context: Context): Context {
        ObdLibrary.init(context as Application)
        return context
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}