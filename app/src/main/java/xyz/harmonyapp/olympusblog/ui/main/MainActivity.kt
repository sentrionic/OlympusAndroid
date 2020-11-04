package xyz.harmonyapp.olympusblog.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.ActivityMainBinding
import xyz.harmonyapp.olympusblog.ui.BaseActivity
import xyz.harmonyapp.olympusblog.ui.auth.AuthActivity
import xyz.harmonyapp.olympusblog.ui.main.account.BaseAccountFragment
import xyz.harmonyapp.olympusblog.ui.main.account.ChangePasswordFragment
import xyz.harmonyapp.olympusblog.ui.main.account.UpdateAccountFragment
import xyz.harmonyapp.olympusblog.ui.main.article.BaseArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.article.UpdateArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.article.ViewArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.create.BaseCreateArticleFragment
import xyz.harmonyapp.olympusblog.utils.BottomNavController
import xyz.harmonyapp.olympusblog.utils.setUpNavigation

class MainActivity : BaseActivity(),
    BottomNavController.NavGraphProvider,
    BottomNavController.OnNavigationGraphChanged,
    BottomNavController.OnNavigationReselectedListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var bottomNavigationView: BottomNavigationView

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_nav_host_fragment,
            R.id.nav_article,
            this,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupActionBar()
        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.onNavigationItemSelected()
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Log.d(TAG, "MainActivity, subscribeObservers: ViewState: ${authToken}")
            if (authToken == null || authToken.account_id == -1 || authToken.token == null) {
                navAuthActivity()
                finish()
            }
        })
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolBar)
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun getNavGraphId(itemId: Int) = when (itemId) {
        R.id.nav_article -> {
            R.navigation.nav_article
        }
        R.id.nav_create_article -> {
            R.navigation.nav_create
        }
        R.id.nav_account -> {
            R.navigation.nav_account
        }
        else -> {
            R.navigation.nav_article
        }
    }

    override fun onGraphChange() {
        cancelActiveJobs()
        expandAppBar()
    }

    override fun onReselectNavItem(
        navController: NavController,
        fragment: Fragment
    ) = when (fragment) {

        is ViewArticleFragment -> {
            navController.navigate(R.id.action_viewArticleFragment_to_home)
        }

        is UpdateArticleFragment -> {
            navController.navigate(R.id.action_updateArticleFragment_to_home)
        }

        is UpdateAccountFragment -> {
            navController.navigate(R.id.action_updateAccountFragment_to_home)
        }

        is ChangePasswordFragment -> {
            navController.navigate(R.id.action_changePasswordFragment_to_home)
        }

        else -> {
            // do nothing
        }
    }

    override fun expandAppBar() {
        binding.appBar.setExpanded(true)
    }

    private fun cancelActiveJobs() {
        val fragments = bottomNavController.fragmentManager
            .findFragmentById(bottomNavController.containerId)
            ?.childFragmentManager
            ?.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment is BaseAccountFragment) {
                    fragment.cancelActiveJobs()
                }
                if (fragment is BaseArticleFragment) {
                    fragment.cancelActiveJobs()
                }
                if (fragment is BaseCreateArticleFragment) {
                    fragment.cancelActiveJobs()
                }
            }
        }
        displayProgressBar(false)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}