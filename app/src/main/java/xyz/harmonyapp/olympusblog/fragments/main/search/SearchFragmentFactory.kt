package xyz.harmonyapp.olympusblog.fragments.main.search

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import io.noties.markwon.Markwon
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.main.article.CommentFragment
import xyz.harmonyapp.olympusblog.ui.main.article.ViewArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.search.SearchFragment
import xyz.harmonyapp.olympusblog.ui.main.search.ViewProfileFragment
import javax.inject.Inject

@MainScope
class SearchFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val requestOptions: RequestOptions,
    private val markwon: Markwon
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            SearchFragment::class.java.name -> {
                SearchFragment(viewModelFactory, requestOptions)
            }

            ViewProfileFragment::class.java.name -> {
                ViewProfileFragment(viewModelFactory, requestManager)
            }

            ViewArticleFragment::class.java.name -> {
                ViewArticleFragment(viewModelFactory, requestManager, markwon)
            }

            CommentFragment::class.java.name -> {
                CommentFragment(viewModelFactory, requestManager)
            }

            else -> {
                SearchFragment(viewModelFactory, requestOptions)
            }
        }
}