package xyz.harmonyapp.olympusblog.ui.main.create

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
import io.noties.markwon.editor.MarkwonEditor
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.di.Injectable
import xyz.harmonyapp.olympusblog.ui.DataStateChangeListener
import xyz.harmonyapp.olympusblog.ui.UICommunicationListener
import xyz.harmonyapp.olympusblog.ui.main.MainDependencyProvider
import xyz.harmonyapp.olympusblog.ui.main.create.state.CREATE_ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleViewState
import javax.inject.Inject

abstract class BaseCreateArticleFragment : Fragment(), Injectable {

    val TAG: String = "AppDebug"

    @Inject
    lateinit var editor: MarkwonEditor

    lateinit var stateChangeListener: DataStateChangeListener

    lateinit var uiCommunicationListener: UICommunicationListener

    lateinit var viewModel: CreateArticleViewModel

    lateinit var mainDependencyProvider: MainDependencyProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.createArticleFragment, activity as AppCompatActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(
                this,
                mainDependencyProvider.getVMProviderFactory()
            ).get(CreateArticleViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        cancelActiveJobs()

        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[CREATE_ARTICLE_VIEW_STATE_BUNDLE_KEY] as CreateArticleViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    fun isViewModelInitialized() = ::viewModel.isInitialized

    /**
     * !IMPORTANT!
     * Must save ViewState b/c in event of process death the LiveData in ViewModel will be lost
     */
    override fun onSaveInstanceState(outState: Bundle) {
        if (isViewModelInitialized()) {
            outState.putParcelable(
                CREATE_ARTICLE_VIEW_STATE_BUNDLE_KEY,
                viewModel.viewState.value
            )
        }
        super.onSaveInstanceState(outState)
    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

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
}