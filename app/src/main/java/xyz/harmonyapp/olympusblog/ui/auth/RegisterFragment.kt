package xyz.harmonyapp.olympusblog.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import xyz.harmonyapp.olympusblog.databinding.FragmentRegisterBinding
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthStateEvent
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthStateEvent.*
import xyz.harmonyapp.olympusblog.ui.auth.state.RegistrationFields

class RegisterFragment : BaseAuthFragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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

    fun register() {
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
        _binding = null
        viewModel.setRegistrationFields(
            RegistrationFields(
                binding.inputEmail.text.toString(),
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString(),
            )
        )
    }

}