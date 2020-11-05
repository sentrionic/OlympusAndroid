package xyz.harmonyapp.olympusblog.ui.main.article

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.RequestManager
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentArticleBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils.Companion.ARTICLES_ASC
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils.Companion.ARTICLES_DESC
import xyz.harmonyapp.olympusblog.ui.DataState
import xyz.harmonyapp.olympusblog.ui.main.article.state.ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.*
import xyz.harmonyapp.olympusblog.utils.TopSpacingItemDecoration
import javax.inject.Inject

@MainScope
class ArticleFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseArticleFragment(),
    ArticleListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    val viewModel: ArticleViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var recyclerAdapter: ArticleListAdapter
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelActiveJobs()
        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ARTICLE_VIEW_STATE_BUNDLE_KEY] as ArticleViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value

        //clear the list. Don't want to save a large list to bundle.
        viewState?.articleFields?.articleList = ArrayList()

        outState.putParcelable(
            ARTICLE_VIEW_STATE_BUNDLE_KEY,
            viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.swipeRefresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()
    }

    private fun handlePagination(dataState: DataState<ArticleViewState>) {

        // Handle incoming data from DataState
        dataState.data?.let {
            it.data?.let {
                it.getContentIfNotHandled()?.let {
                    viewModel.handleIncomingArticleListData(it)
                }
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            Log.d(TAG, "ArticleFragment, ViewState: ${viewState}")
            if (viewState != null) {
                recyclerAdapter.apply {
                    preloadGlideImages(
                        requestManager = requestManager,
                        list = viewState.articleFields.articleList
                    )

                    recyclerAdapter.submitList(
                        articleList = viewState.articleFields.articleList,
                        isQueryExhausted = viewState.articleFields.isQueryExhausted
                    )
                }
            }
        })
    }

    private fun initRecyclerView() {

        binding.articleRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@ArticleFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = ArticleListAdapter(
                requestManager,
                this@ArticleFragment
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        viewModel.nextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }

    }


    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
        }

        // ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                || actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val searchQuery = v.text.toString()
                Log.e(TAG, "SearchView: (keyboard or arrow) executing search...: ${searchQuery}")
                viewModel.setQuery(searchQuery).let {
                    onArticleSearchOrFilter()
                }
            }
            true
        }

        // SEARCH BUTTON CLICKED (in toolbar)
        val searchButton = searchView.findViewById(R.id.search_go_btn) as View
        searchButton.setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            Log.e(TAG, "SearchView: (button) executing search...: ${searchQuery}")
            viewModel.setQuery(searchQuery).let {
                onArticleSearchOrFilter()
            }

        }
    }

    private fun showFilterDialog() {

        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_article_filter)

            val view = dialog.getCustomView()

            var order = viewModel.getOrder()

            when (order) {
                ARTICLES_DESC -> {
                    view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_desc)
                }
                ARTICLES_ASC -> {
                    view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_asc)
                }
                else -> {
                    view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_top)
                }
            }

            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: apply filter.")

                val selectedOrder = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView()
                        .findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
                )

                order = selectedOrder.text.toString()

                viewModel.saveFilterOptions(order).let {
                    viewModel.setArticleOrder(order)
                    onArticleSearchOrFilter()
                }
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: cancelling filter.")
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun onArticleSearchOrFilter() {
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        binding.articleRecyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        binding.focusableView.requestFocus()
    }

    override fun onItemSelected(position: Int, item: Article) {
        viewModel.setArticle(item)
        findNavController().navigate(R.id.action_articleFragment_to_viewArticleFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onRefresh() {
        onArticleSearchOrFilter()
        binding.swipeRefresh.isRefreshing = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_filter_settings -> {
                showFilterDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun restoreListPosition() {
        viewModel.viewState.value?.articleFields?.layoutManagerState?.let { lmState ->
            if (_binding != null) {
                binding.articleRecyclerview.layoutManager?.onRestoreInstanceState(lmState)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFirstPage()
    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    private fun saveLayoutManagerState() {
        binding.articleRecyclerview.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.articleRecyclerview.adapter = null
        _binding = null
    }
}