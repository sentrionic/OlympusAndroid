package xyz.harmonyapp.olympusblog.fragments.main.profile

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.main.account.AccountFragment
import xyz.harmonyapp.olympusblog.ui.main.account.ChangePasswordFragment
import xyz.harmonyapp.olympusblog.ui.main.account.UpdateAccountFragment
import xyz.harmonyapp.olympusblog.ui.main.profile.ProfileFragment
import xyz.harmonyapp.olympusblog.ui.main.profile.ViewProfileFragment
import javax.inject.Inject

@MainScope
class ProfileFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val requestOptions: RequestOptions
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            ProfileFragment::class.java.name -> {
                ProfileFragment(viewModelFactory, requestOptions)
            }

            ViewProfileFragment::class.java.name -> {
                ViewProfileFragment(viewModelFactory, requestManager)
            }

            else -> {
                ProfileFragment(viewModelFactory, requestOptions)
            }
        }
}