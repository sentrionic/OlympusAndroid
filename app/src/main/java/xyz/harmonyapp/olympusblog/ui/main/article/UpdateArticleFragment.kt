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
import androidx.navigation.fragment.findNavController
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentUpdateArticleBinding
import xyz.harmonyapp.olympusblog.ui.*
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.getUpdatedArticleUri
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.onArticleUpdateSuccess
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.setUpdatedArticleFields
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.GALLERY_REQUEST_CODE
import java.io.File
import java.util.concurrent.Executors

class UpdateArticleFragment : BaseArticleFragment() {

    private var _binding: FragmentUpdateArticleBinding? = null
    private val binding get() = _binding!!

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

        binding.imageContainer.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
    }

    fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let { data ->
                    data.data?.getContentIfNotHandled()?.let { viewState ->

                        // if this is not null, the article was updated
                        viewState.viewArticleFields.article?.let { article ->
                            viewModel.onArticleUpdateSuccess(article).let {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.updatedArticleFields.let { updatedArticleFields ->
                setArticleProperties(
                    updatedArticleFields.updatedArticleTitle,
                    updatedArticleFields.updatedArticleDescription,
                    updatedArticleFields.updatedArticleBody,
                    updatedArticleFields.updatedImageUri
                )
            }
        })
    }

    private fun setArticleProperties(
        title: String?,
        description: String?,
        body: String?,
        image: Uri?
    ) {

        with(binding) {
            mainDependencyProvider.getGlideRequestManager()
                .load(image)
                .into(articleImage)
            articleTitle.setText(title)
            articleDescription.setText(description)
            articleBody.setText(body)
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
                            RequestBody.create(
                                MediaType.parse("image/*"),
                                imageFile
                            )
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
                    multipartBody
                )
            )
            stateChangeListener.hideSoftKeyboard()
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
        stateChangeListener.onDataStateChange(
            DataState(
                Event(
                    StateError(
                        Response(
                            "Something went wrong with the image.",
                            ResponseType.Dialog()
                        )
                    )
                ),
                Loading(isLoading = false),
                Data(Event.dataEvent(null), null)
            )
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
            body = binding.articleBody.text.toString()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}