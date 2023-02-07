package ru.netology.app_manager.ui.backup

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.backup_fragment.*
import kotlinx.android.synthetic.main.login_fragment.view.*
import ru.netology.app_manager.R
import ru.netology.app_manager.core.helper.viewmodels.ViewModelFactory
import ru.netology.app_manager.core.templates.BaseFragment
import ru.netology.app_manager.databinding.LoginFragmentBinding
import ru.netology.app_manager.di.getAppComponent
import ru.netology.app_manager.utils.setDebouncedListener
import ru.netology.app_manager.utils.setVisibility
import timber.log.Timber
import javax.inject.Inject

class LoginFragment : BaseFragment<LoginFragmentBinding>() {


    @Inject
    lateinit var factory: ViewModelFactory
    private val viewModel: LoginViewModel by viewModels { factory }
    private val args: LoginFragmentArgs by navArgs()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> LoginFragmentBinding
        get() = LoginFragmentBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading != null) {
                progress.setVisibility(isLoading)
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            cardLogin.error = message
            cardPassword.error = message
            cardConfirmPass.error = message
            etLogin.error = message
        }
        val flag = args.flag
        cardConfirmPass.setVisibility(flag == LoginFragmentFlag.REGISTER)
        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess != null && isSuccess) {
                mainNavController?.navigateUp()
            }
        }
        etLogin.doOnTextChanged { text: CharSequence?, _, _, _ ->
            viewModel.setName(text?.toString() ?: "")
        }
        etPassword.doOnTextChanged { text: CharSequence?, _, _, _ ->
            viewModel.setPassword(text?.toString() ?: "")
        }
        etConfirmPassword.doOnTextChanged { text: CharSequence?, _, _, _ ->
            viewModel.setConfirmPassword(text?.toString() ?: "")
        }
        btnAuth.text = getString(args.flag.buttonTextId)
        btnAuth.setDebouncedListener(300L) {
            if (flag == LoginFragmentFlag.REGISTER) {
                viewModel.register()
            } else {
                viewModel.login()
            }
        }
    }

    @Parcelize
    enum class LoginFragmentFlag(@StringRes val buttonTextId: Int) : Parcelable{
        LOGIN(R.string.log_in), REGISTER(R.string.sign_out)
    }
}