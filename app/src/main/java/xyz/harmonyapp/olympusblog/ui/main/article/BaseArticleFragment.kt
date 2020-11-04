package xyz.harmonyapp.olympusblog.ui.main.article

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.RequestManager
import dagger.android.support.DaggerFragment
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.ui.DataStateChangeListener
import xyz.harmonyapp.olympusblog.ui.UICommunicationListener
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.ArticleViewModel
import xyz.harmonyapp.olympusblog.viewmodels.ViewModelProviderFactory
import javax.inject.Inject

abstract class BaseArticleFragment : DaggerFragment() {

    val TAG: String = "AppDebug"

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var requestManager: RequestManager

    @Inject
    lateinit var markwon: Markwon

    @Inject
    lateinit var editor: MarkwonEditor

    lateinit var uiCommunicationListener: UICommunicationListener

    lateinit var stateChangeListener: DataStateChangeListener

    lateinit var viewModel: ArticleViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.articleFragment, activity as AppCompatActivity)

        viewModel = activity?.run {
            ViewModelProvider(this, providerFactory).get(ArticleViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        cancelActiveJobs()
    }

    private fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement DataStateChangeListener")
        }

        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }
    }
}