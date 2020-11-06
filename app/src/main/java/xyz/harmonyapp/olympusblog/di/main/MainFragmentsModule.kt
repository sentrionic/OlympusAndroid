package xyz.harmonyapp.olympusblog.di.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import xyz.harmonyapp.olympusblog.fragments.main.account.AccountFragmentFactory
import xyz.harmonyapp.olympusblog.fragments.main.article.ArticleFragmentFactory
import xyz.harmonyapp.olympusblog.fragments.main.create.CreateArticleFragmentFactory
import javax.inject.Named

@Module
object MainFragmentsModule {

    @JvmStatic
    @MainScope
    @Provides
    @Named("AccountFragmentFactory")
    fun provideAccountFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        requestManager: RequestManager
    ): FragmentFactory {
        return AccountFragmentFactory(
            viewModelFactory,
            requestManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    @Named("ArticleFragmentFactory")
    fun provideArticleFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        requestManager: RequestManager,
        requestOptions: RequestOptions,
        markwon: Markwon,
        editor: MarkwonEditor
    ): FragmentFactory {
        return ArticleFragmentFactory(
            viewModelFactory,
            requestOptions,
            requestManager,
            markwon,
            editor
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    @Named("CreateArticleFragmentFactory")
    fun provideCreateArticleFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        requestManager: RequestManager,
        editor: MarkwonEditor
    ): FragmentFactory {
        return CreateArticleFragmentFactory(
            viewModelFactory,
            requestManager,
            editor
        )
    }

}