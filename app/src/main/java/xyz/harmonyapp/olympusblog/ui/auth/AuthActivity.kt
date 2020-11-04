package xyz.harmonyapp.olympusblog.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.ActivityAuthBinding
import xyz.harmonyapp.olympusblog.ui.BaseActivity
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthStateEvent.CheckPreviousAuthEvent
import xyz.harmonyapp.olympusblog.ui.main.MainActivity
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import xyz.harmonyapp.olympusblog.viewmodels.ViewModelProviderFactory
import javax.inject.Inject

class AuthActivity : BaseActivity(), NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityAuthBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener(this)

        subscribeObservers()
    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    private fun subscribeObservers() {

        viewModel.dataState.observe(this, Observer { dataState ->
            onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        it.authToken?.let {
                            Log.d(TAG, "AuthActivity, DataState: ${it}")
                            viewModel.setAuthToken(it)
                        }
                    }
                }

                data.response?.let { event ->
                    event.peekContent().let { response ->
                        response.message?.let { message ->
                            if (message == RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE) {
                                onFinishCheckPreviousAuthUser()
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(this, {
            Log.d(TAG, "AuthActivity, subscribeObservers: AuthViewState: ${it}")
            it.authToken?.let {
                sessionManager.login(it)
            }
        })

        sessionManager.cachedToken.observe(this, { dataState ->
            Log.d(TAG, "AuthActivity, subscribeObservers: AuthDataState: ${dataState}")
            dataState.let { authToken ->
                if (authToken != null && authToken.account_id != -1 && authToken.token != null) {
                    navMainActivity()
                }
            }
        })
    }

    private fun navMainActivity() {
        Log.d(TAG, "navMainActivity: called.")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkPreviousAuthUser() {
        viewModel.setStateEvent(CheckPreviousAuthEvent())
    }

    private fun onFinishCheckPreviousAuthUser() {
        binding.fragmentContainer.visibility = View.VISIBLE
    }


    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun expandAppBar() {
        // ignore
    }
}