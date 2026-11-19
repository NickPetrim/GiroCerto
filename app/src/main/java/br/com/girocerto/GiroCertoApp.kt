package br.com.girocerto

import android.app.Application
import br.com.girocerto.di.AppContainer

class GiroCertoApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}