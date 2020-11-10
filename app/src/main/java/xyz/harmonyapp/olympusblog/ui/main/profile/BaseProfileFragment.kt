package xyz.harmonyapp.olympusblog.ui.main.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.ui.UICommunicationListener
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.ArticleViewModel
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileViewModel
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileViewState

abstract class BaseProfileFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment() {

    val TAG: String = "AppDebug"

    val viewModel: ProfileViewModel by viewModels {
        viewModelFactory
    }

    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.profileFragment, activity as AppCompatActivity)
        setupChannel()
    }

    private fun setupChannel() = viewModel.setupChannel()

    private fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }
    }

}