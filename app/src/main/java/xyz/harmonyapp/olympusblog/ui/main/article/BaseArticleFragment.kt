package xyz.harmonyapp.olympusblog.ui.main.article

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.di.Injectable
import xyz.harmonyapp.olympusblog.ui.DataStateChangeListener
import xyz.harmonyapp.olympusblog.ui.UICommunicationListener
import xyz.harmonyapp.olympusblog.ui.main.MainDependencyProvider
import xyz.harmonyapp.olympusblog.ui.main.article.state.ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.ArticleViewModel
import javax.inject.Inject

abstract class BaseArticleFragment : Fragment(), Injectable {

    val TAG: String = "AppDebug"

    lateinit var mainDependencyProvider: MainDependencyProvider

    @Inject
    lateinit var markwon: Markwon

    @Inject
    lateinit var editor: MarkwonEditor

    lateinit var uiCommunicationListener: UICommunicationListener

    lateinit var stateChangeListener: DataStateChangeListener

    lateinit var viewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(
                this,
                mainDependencyProvider.getVMProviderFactory()
            ).get(ArticleViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        cancelActiveJobs()

        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ARTICLE_VIEW_STATE_BUNDLE_KEY] as ArticleViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.articleFragment, activity as AppCompatActivity)
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

        try {
            mainDependencyProvider = context as MainDependencyProvider
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement MainDependencyProvider")
        }
    }

    fun isViewModelInitialized() = ::viewModel.isInitialized

    override fun onSaveInstanceState(outState: Bundle) {
        if (isViewModelInitialized()) {

            val viewState = viewModel.viewState.value
            viewState?.articleFields?.articleList = ArrayList()

            outState.putParcelable(
                ARTICLE_VIEW_STATE_BUNDLE_KEY,
                viewState
            )
        }
        super.onSaveInstanceState(outState)
    }

}