package xyz.harmonyapp.olympusblog.ui

import xyz.harmonyapp.olympusblog.utils.Response
import xyz.harmonyapp.olympusblog.utils.StateMessageCallback

interface UICommunicationListener {

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

    fun displayProgressBar(isLoading: Boolean)

    fun expandAppBar()

    fun hideSoftKeyboard()

    fun isStoragePermissionGranted(): Boolean
}