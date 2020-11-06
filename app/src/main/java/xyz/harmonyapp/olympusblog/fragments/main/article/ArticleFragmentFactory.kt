package xyz.harmonyapp.olympusblog.fragments.main.article

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.main.article.ArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.article.UpdateArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.article.ViewArticleFragment
import javax.inject.Inject

@MainScope
class ArticleFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestOptions: RequestOptions,
    private val requestManager: RequestManager,
    private val markwon: Markwon,
    private val editor: MarkwonEditor
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            ArticleFragment::class.java.name -> {
                ArticleFragment(viewModelFactory, requestOptions)
            }

            ViewArticleFragment::class.java.name -> {
                ViewArticleFragment(viewModelFactory, requestManager, markwon)
            }

            UpdateArticleFragment::class.java.name -> {
                UpdateArticleFragment(viewModelFactory, requestManager, editor)
            }

            else -> {
                ArticleFragment(viewModelFactory, requestOptions)
            }
        }


} 