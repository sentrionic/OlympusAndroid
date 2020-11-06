package xyz.harmonyapp.olympusblog.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import xyz.harmonyapp.olympusblog.BaseApplication
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.ActivityAuthBinding
import xyz.harmonyapp.olympusblog.fragments.auth.AuthNavHostFragment
import xyz.harmonyapp.olympusblog.ui.BaseActivity
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthStateEvent.CheckPreviousAuthEvent
import xyz.harmonyapp.olympusblog.ui.main.MainActivity
import xyz.harmonyapp.olympusblog.utils.StateMessageCallback
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthBinding

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    val viewModel: AuthViewModel by viewModels {
        providerFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        subscribeObservers()
        onRestoreInstanceState()
    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    private fun createNavHost() {
        val navHost = AuthNavHostFragment.create(
            R.navigation.auth_nav_graph
        )
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.auth_fragments_container,
                navHost,
                getString(R.string.AuthNavHost)
            )
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }

    private fun subscribeObservers() {

        viewModel.viewState.observe(this, Observer { viewState ->
            Log.d(TAG, "AuthActivity, subscribeObservers: AuthViewState: ${viewState}")
            viewState.authToken?.let {
                sessionManager.login(it)
            }
        })

        viewModel.numActiveJobs.observe(this, Observer { jobCounter ->
            displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(this, Observer { stateMessage ->

            stateMessage?.let {

                if (stateMessage.response.message.equals(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE)) {
                    onFinishCheckPreviousAuthUser()
                }

                onResponseReceived(
                    response = it.response,
                    stateMessageCallback = object : StateMessageCallback {
                        override fun removeMessageFromStack() {
                            viewModel.clearStateMessage()
                        }
                    }
                )
            }
        })

        sessionManager.cachedToken.observe(this, Observer { token ->
            token.let { authToken ->
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
        (application as BaseApplication).releaseAuthComponent()
    }

    private fun checkPreviousAuthUser() {
        viewModel.setStateEvent(CheckPreviousAuthEvent())
    }

    private fun onFinishCheckPreviousAuthUser() {
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.splashLogo.visibility = View.INVISIBLE
    }

    private fun onRestoreInstanceState() {
        val host = supportFragmentManager.findFragmentById(R.id.auth_fragments_container)
        host?.let {
            // do nothing
        } ?: createNavHost()
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun inject() {
        (application as BaseApplication).authComponent()
            .inject(this)
    }

    override fun expandAppBar() {
        // ignore
    }
}