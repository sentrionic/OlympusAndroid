package xyz.harmonyapp.olympusblog.fragments.main.profile

import android.content.Context
import android.os.Bundle
import androidx.annotation.NavigationRes
import androidx.navigation.fragment.NavHostFragment
import xyz.harmonyapp.olympusblog.ui.main.MainActivity

class ProfileNavHostFragment : NavHostFragment() {

    override fun onAttach(context: Context) {
        childFragmentManager.fragmentFactory =
            (activity as MainActivity).profileFragmentFactory
        super.onAttach(context)
    }

    companion object {

        const val KEY_GRAPH_ID = "android-support-nav:fragment:graphId"

        @JvmStatic
        fun create(
            @NavigationRes graphId: Int = 0
        ): ProfileNavHostFragment {
            var bundle: Bundle? = null
            if (graphId != 0) {
                bundle = Bundle()
                bundle.putInt(KEY_GRAPH_ID, graphId)
            }
            val result =
                ProfileNavHostFragment()
            if (bundle != null) {
                result.arguments = bundle
            }
            return result
        }
    }
}