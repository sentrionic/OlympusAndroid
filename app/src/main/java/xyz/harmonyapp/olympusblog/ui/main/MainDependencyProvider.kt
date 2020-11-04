package xyz.harmonyapp.olympusblog.ui.main

import com.bumptech.glide.RequestManager
import xyz.harmonyapp.olympusblog.viewmodels.ViewModelProviderFactory

interface MainDependencyProvider {

    fun getVMProviderFactory(): ViewModelProviderFactory

    fun getGlideRequestManager(): RequestManager
}
