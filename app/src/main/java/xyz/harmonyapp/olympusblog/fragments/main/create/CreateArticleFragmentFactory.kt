package xyz.harmonyapp.olympusblog.fragments.main.create

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import io.noties.markwon.editor.MarkwonEditor
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.main.create.CreateArticleFragment
import javax.inject.Inject

@MainScope
class CreateArticleFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val editor: MarkwonEditor
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            CreateArticleFragment::class.java.name -> {
                CreateArticleFragment(viewModelFactory, requestManager, editor)
            }

            else -> {
                CreateArticleFragment(viewModelFactory, requestManager, editor)
            }
        }
} 