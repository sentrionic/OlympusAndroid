package xyz.harmonyapp.olympusblog.ui.main.article

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentUpdateArticleBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.main.article.state.ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.getUpdatedArticleUri
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.setUpdatedArticleFields
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.updateListItem
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.GALLERY_REQUEST_CODE
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.SOMETHING_WRONG_WITH_IMAGE
import xyz.harmonyapp.olympusblog.utils.MessageType
import xyz.harmonyapp.olympusblog.utils.Response
import xyz.harmonyapp.olympusblog.utils.StateMessageCallback
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_UPDATED
import xyz.harmonyapp.olympusblog.utils.UIComponentType
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject

@MainScope
class UpdateArticleFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val editor: MarkwonEditor
) : BaseArticleFragment(viewModelFactory) {

    private var _binding: FragmentUpdateArticleBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        subscribeObservers()
        binding.articleBody.addTextChangedListener(
            MarkwonEditorTextWatcher.withPreRender(
                editor,
                Executors.newCachedThreadPool(),
                binding.articleBody
            )
        )

        binding.updateImageButton.setOnClickListener {
            if (uiCommunicationListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
    }

    fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.updatedArticleFields.let { updatedArticleFields ->
                setArticleProperties(
                    updatedArticleFields.updatedArticleTitle,
                    updatedArticleFields.updatedArticleDescription,
                    updatedArticleFields.updatedArticleBody,
                    updatedArticleFields.updatedImageUri,
                    updatedArticleFields.updatedArticleTags
                )
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer { jobCounter ->
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            stateMessage?.let {

                if (stateMessage.response.message.equals(SUCCESS_ARTICLE_UPDATED)) {
                    viewModel.updateListItem()
                    findNavController().popBackStack()
                }

                uiCommunicationListener.onResponseReceived(
                    response = it.response,
                    stateMessageCallback = object : StateMessageCallback {
                        override fun removeMessageFromStack() {
                            viewModel.clearStateMessage()
                        }
                    }
                )
            }
        })
    }


    private fun setArticleProperties(
        title: String?,
        description: String?,
        body: String?,
        image: Uri?,
        tagList: String?
    ) {

        with(binding) {
            requestManager
                .load(image)
                .into(articleImage)
            articleTitle.setText(title)
            articleDescription.setText(description)
            articleBody.setText(body)
            articleTags.setText(tagList)
        }
    }

    private fun saveChanges() {
        with(binding) {
            var multipartBody: MultipartBody.Part? = null
            viewModel.getUpdatedArticleUri()?.let { imageUri ->
                imageUri.path?.let { filePath ->
                    val imageFile = File(filePath)
                    Log.d(TAG, "UpdateBlogFragment, imageFile: file: ${imageFile}")
                    if (imageFile.exists()) {
                        val requestBody =
                            imageFile
                                .asRequestBody("image/*".toMediaTypeOrNull())
                        // name = field name in serializer
                        // filename = name of the image file
                        // requestBody = file with file type information
                        multipartBody = MultipartBody.Part.createFormData(
                            "image",
                            imageFile.name,
                            requestBody
                        )
                    }
                }
            }
            viewModel.setStateEvent(
                ArticleStateEvent.UpdateArticleEvent(
                    articleTitle.text.toString(),
                    articleDescription.text.toString(),
                    articleBody.text.toString(),
                    articleTags.text.toString(),
                    multipartBody
                )
            )
            uiCommunicationListener.hideSoftKeyboard()
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri) {
        context?.let {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    private fun showImageSelectionError() {
        uiCommunicationListener.onResponseReceived(
            response = Response(
                message = SOMETHING_WRONG_WITH_IMAGE,
                uiComponentType = UIComponentType.Dialog(),
                messageType = MessageType.Error()
            ),
            stateMessageCallback = object : StateMessageCallback {
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        activity?.let {
                            launchImageCrop(uri)
                        }
                    } ?: showImageSelectionError()
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri: ${resultUri}")
                    viewModel.setUpdatedArticleFields(
                        title = null,
                        body = null,
                        description = null,
                        tags = null,
                        uri = resultUri
                    )
                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    Log.d(TAG, "CROP: ERROR")
                    showImageSelectionError()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setUpdatedArticleFields(
            uri = null,
            title = binding.articleTitle.text.toString(),
            description = binding.articleDescription.text.toString(),
            tags = binding.articleTags.text.toString(),
            body = binding.articleBody.text.toString()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}