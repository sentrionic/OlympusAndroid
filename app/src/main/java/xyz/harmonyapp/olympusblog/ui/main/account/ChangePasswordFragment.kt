package xyz.harmonyapp.olympusblog.ui.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import xyz.harmonyapp.olympusblog.databinding.FragmentChangePasswordBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountStateEvent
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountViewState
import xyz.harmonyapp.olympusblog.utils.StateMessageCallback
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import javax.inject.Inject

@MainScope
class ChangePasswordFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
) : BaseAccountFragment(viewModelFactory) {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.updatePasswordButton.setOnClickListener {
            viewModel.setStateEvent(
                AccountStateEvent.ChangePasswordEvent(
                    binding.inputCurrentPassword.text.toString(),
                    binding.inputNewPassword.text.toString(),
                    binding.inputConfirmNewPassword.text.toString()
                )
            )
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer { jobCounter ->
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            stateMessage?.let {

                if (stateMessage.response.message.equals(RESPONSE_PASSWORD_UPDATE_SUCCESS)) {
                    uiCommunicationListener.hideSoftKeyboard()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}