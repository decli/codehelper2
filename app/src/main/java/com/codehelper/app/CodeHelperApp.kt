package com.codehelper.app

import android.app.Application
import com.codehelper.app.data.db.AppDatabase

class CodeHelperApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
