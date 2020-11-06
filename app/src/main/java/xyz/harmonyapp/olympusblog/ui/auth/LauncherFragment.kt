package xyz.harmonyapp.olympusblog.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentLauncherBinding
import xyz.harmonyapp.olympusblog.di.auth.AuthScope
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthStateEvent.LoginAttemptEvent
import xyz.harmonyapp.olympusblog.ui.auth.state.LoginFields
import javax.inject.Inject


@AuthScope
class LauncherFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseAuthFragment(viewModelFactory) {

    private var _binding: FragmentLauncherBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentLauncherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            loginButton.setOnClickListener {
                login()
            }

            register.setOnClickListener {
                navRegister()
            }

            forgotPassword.setOnClickListener {
                navForgotPassword()
            }

            focusableView.requestFocus() // reset focus
        }

        subscribeObservers()
    }

    private fun navForgotPassword() {
        findNavController().navigate(R.id.action_launcherFragment_to_forgotPasswordFragment)
    }

    private fun navRegister() {
        findNavController().navigate(R.id.action_launcherFragment_to_registerFragment)
    }

    private fun login() {
        viewModel.setStateEvent(
            LoginAttemptEvent(
                binding.inputEmail.text.toString(),
                binding.inputPassword.text.toString()
            )
        )
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.loginFields?.let {
                it.login_email?.let { binding.inputEmail.setText(it) }
                it.login_password?.let { binding.inputPassword.setText(it) }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                binding.inputEmail.text.toString(),
                binding.inputPassword.text.toString()
            )
        )
        _binding = null
    }
}