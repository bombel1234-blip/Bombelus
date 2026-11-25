package com.mojdzien.app

import android.app.Application
import com.mojdzien.app.data.AppDatabase

class MyDayApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
}
