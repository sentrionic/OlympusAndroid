package xyz.harmonyapp.olympusblog.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import xyz.harmonyapp.olympusblog.databinding.FragmentRegisterBinding
import xyz.harmonyapp.olympusblog.di.auth.AuthScope
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthStateEvent.RegisterAttemptEvent
import xyz.harmonyapp.olympusblog.ui.auth.state.RegistrationFields
import javax.inject.Inject

@AuthScope
class RegisterFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseAuthFragment(viewModelFactory) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            register()
        }

        subscribeObservers()
    }

    private fun register() {
        viewModel.setStateEvent(
            RegisterAttemptEvent(
                binding.inputEmail.text.toString(),
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString()
            )
        )
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.registrationFields?.let {
                it.registration_email?.let { binding.inputEmail.setText(it) }
                it.registration_username?.let { binding.inputUsername.setText(it) }
                it.registration_password?.let { binding.inputPassword.setText(it) }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setRegistrationFields(
            RegistrationFields(
                binding.inputEmail.text.toString(),
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString(),
            )
        )
        _binding = null
    }

}