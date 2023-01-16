package ru.netology.app_manager.ui.backup

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.parcel.Parcelize
import ru.netology.app_manager.core.helper.viewmodels.ViewModelFactory
import ru.netology.app_manager.core.templates.BaseFragment
import ru.netology.app_manager.databinding.LoginFragmentBinding
import ru.netology.app_manager.di.getAppComponent
import ru.netology.app_manager.utils.setVisibility
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
        cardConfirmPass.setVisibility(args.flag == LoginFragmentFlag.REGISTER)
        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess != null) {
                mainNavController?.navigateUp()
            }
        }
    }

    @Parcelize
    enum class LoginFragmentFlag : Parcelable{
        LOGIN, REGISTER
    }
}