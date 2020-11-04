package xyz.harmonyapp.olympusblog.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentLauncherBinding
import xyz.harmonyapp.olympusblog.models.AuthToken
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthStateEvent.LoginAttemptEvent
import xyz.harmonyapp.olympusblog.ui.auth.state.LoginFields


class LauncherFragment : BaseAuthFragment() {

    private var _binding: FragmentLauncherBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLauncherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "LauncherFragment: ${viewModel}")


        binding.loginButton.setOnClickListener {
            login()
        }

        binding.register.setOnClickListener {
            navRegister()
        }

        binding.forgotPassword.setOnClickListener {
            navForgotPassword()
        }

        binding.focusableView.requestFocus() // reset focus

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
                it.login_password?.let { binding.inputEmail.setText(it) }
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
    }
}