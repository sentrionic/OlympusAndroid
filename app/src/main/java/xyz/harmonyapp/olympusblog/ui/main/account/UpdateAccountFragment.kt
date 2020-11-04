package xyz.harmonyapp.olympusblog.ui.main.account

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_account.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentUpdateAccountBinding
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.ui.*
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountStateEvent.UpdateAccountPropertiesEvent
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.GALLERY_REQUEST_CODE
import java.io.File

class UpdateAccountFragment : BaseAccountFragment() {

    private var _binding: FragmentUpdateAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()

        binding.profilePhoto.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)
            Log.d(TAG, "UpdateAccountFragment, DataState: ${dataState}")
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {
                viewState.accountProperties?.let {
                    Log.d(TAG, "UpdateAccountFragment, ViewState: ${it}")
                    setAccountDataFields(it, viewState.updatedImageUri?: it.image.toUri())
                }
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties, uri: Uri?) {

        with(binding) {
            if (inputEmail.text.isNullOrBlank()) {
                inputEmail.setText(accountProperties.email)
            }

            if (inputUsername.text.isNullOrBlank()) {
                inputUsername.setText(accountProperties.username)
            }

            if (inputBio.text.isNullOrBlank()) {
                inputBio.setText(accountProperties.bio)
            }

            requestManager
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(profilePhoto)
        }
    }

    private fun saveChanges() {
        var multipartBody: MultipartBody.Part? = null
        viewModel.getUpdatedImageUri()?.let { imageUri ->
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
            UpdateAccountPropertiesEvent(
                binding.inputEmail.text.toString(),
                binding.inputUsername.text.toString(),
                binding.inputBio.text.toString(),
                multipartBody
            )
        )
        stateChangeListener.hideSoftKeyboard()
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
                .setAspectRatio(1, 1)
                .setRequestedSize(150, 150, CropImageView.RequestSizeOptions.RESIZE_EXACT)
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
                    viewModel.setUpdatedUri(
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}