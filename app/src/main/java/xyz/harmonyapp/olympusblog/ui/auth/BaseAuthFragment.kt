package xyz.harmonyapp.olympusblog.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import xyz.harmonyapp.olympusblog.ui.UICommunicationListener

abstract class BaseAuthFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment() {

    val TAG: String = "AppDebug"

    val viewModel: AuthViewModel by viewModels {
        viewModelFactory
    }

    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChannel()
    }

    private fun setupChannel() = viewModel.setupChannel()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }

    }
}