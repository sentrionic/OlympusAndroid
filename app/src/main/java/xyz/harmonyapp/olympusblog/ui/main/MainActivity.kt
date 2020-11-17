package xyz.harmonyapp.olympusblog.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import xyz.harmonyapp.olympusblog.BaseApplication
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.ActivityMainBinding
import xyz.harmonyapp.olympusblog.models.AUTH_TOKEN_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.models.AuthToken
import xyz.harmonyapp.olympusblog.ui.BaseActivity
import xyz.harmonyapp.olympusblog.ui.auth.AuthActivity
import xyz.harmonyapp.olympusblog.ui.main.account.ChangePasswordFragment
import xyz.harmonyapp.olympusblog.ui.main.account.UpdateAccountFragment
import xyz.harmonyapp.olympusblog.ui.main.article.CommentFragment
import xyz.harmonyapp.olympusblog.ui.main.article.CreateArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.article.UpdateArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.article.ViewArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.search.ViewProfileFragment
import xyz.harmonyapp.olympusblog.utils.BOTTOM_NAV_BACKSTACK_KEY
import xyz.harmonyapp.olympusblog.utils.BottomNavController
import xyz.harmonyapp.olympusblog.utils.BottomNavController.*
import xyz.harmonyapp.olympusblog.utils.setUpNavigation
import javax.inject.Inject
import javax.inject.Named

class MainActivity : BaseActivity(),
    OnNavigationGraphChanged,
    OnNavigationReselectedListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var bottomNavigationView: BottomNavigationView

    @Inject
    @Named("AccountFragmentFactory")
    lateinit var accountFragmentFactory: FragmentFactory

    @Inject
    @Named("ArticleFragmentFactory")
    lateinit var articleFragmentFactory: FragmentFactory

    @Inject
    @Named("ProfileFragmentFactory")
    lateinit var profileFragmentFactory: FragmentFactory

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_fragments_container,
            R.id.menu_nav_article,
            this,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupActionBar()
        setupBottomNavigationView(savedInstanceState)

        subscribeObservers()
        restoreSession(savedInstanceState)
    }

    private fun setupBottomNavigationView(savedInstanceState: Bundle?) {
        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.setupBottomNavigationBackStack(null)
            bottomNavController.onNavigationItemSelected()
        } else {
            (savedInstanceState[BOTTOM_NAV_BACKSTACK_KEY] as IntArray?)?.let { items ->
                val backstack = BackStack()
                backstack.addAll(items.toTypedArray())
                bottomNavController.setupBottomNavigationBackStack(backstack)
            }
        }
    }


    private fun restoreSession(savedInstanceState: Bundle?) {
        savedInstanceState?.get(AUTH_TOKEN_BUNDLE_KEY)?.let { authToken ->
            sessionManager.setValue(authToken as AuthToken)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(AUTH_TOKEN_BUNDLE_KEY, sessionManager.cachedToken.value)
        outState.putIntArray(
            BOTTOM_NAV_BACKSTACK_KEY,
            bottomNavController.navigationBackStack.toIntArray()
        )
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
        (application as BaseApplication).releaseMainComponent()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolBar)
    }

    override fun displayProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onGraphChange() {
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

        is CreateArticleFragment -> {
            navController.navigate(R.id.action_createArticleFragment_to_home)
        }

        is CommentFragment -> {
            navController.navigate(R.id.action_updateAccountFragment_to_home)
        }

        is ChangePasswordFragment -> {
            navController.navigate(R.id.action_changePasswordFragment_to_home)
        }

        is ViewProfileFragment -> {
            navController.navigate(R.id.action_viewProfileFragment_to_home)
        }

        else -> {
            // do nothing
        }
    }

    override fun expandAppBar() {
        binding.appBar.setExpanded(true)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}