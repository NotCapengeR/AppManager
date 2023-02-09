package ru.netology.app_manager.ui.backup

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import ru.netology.app_manager.R
import ru.netology.app_manager.core.api.models.Backup
import ru.netology.app_manager.core.apk.manager.ApkExtractor.withExternalStoragePermission
import ru.netology.app_manager.core.helper.viewmodels.ViewModelFactory
import ru.netology.app_manager.core.templates.BaseFragment
import ru.netology.app_manager.databinding.BackupFragmentBinding
import ru.netology.app_manager.di.getAppComponent
import ru.netology.app_manager.ui.app_list.AppListViewModel
import ru.netology.app_manager.ui.app_list.MainFragmentDirections
import ru.netology.app_manager.utils.setDebouncedListener
import ru.netology.app_manager.utils.setVisibility
import timber.log.Timber
import javax.inject.Inject

class BackupFragment : BaseFragment<BackupFragmentBinding>() {


    @Inject
    lateinit var factory: ViewModelFactory
    private val viewModel: AppListViewModel by activityViewModels { factory }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BackupFragmentBinding
        get() = BackupFragmentBinding::inflate

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
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it != null) {
                progress.setVisibility(it)
            }
        }
        btnBackup.setDebouncedListener(300L) {
            if (!viewModel.isLoggedIn) {
                showDialog {
                    setTitle(R.string.attention)
                        .setMessage(R.string.auth_please_signin)
                        .setPositiveButton(R.string.log_in) { _, _ ->
                            Timber.d("Log in dialog pressed")
                            mainNavController?.navigate(
                                BackupFragmentDirections.actionBackupFragmentToLoginFragment(
                                    LoginFragment.LoginFragmentFlag.LOGIN
                                )
                            )
                        }
                        .setNegativeButton(R.string.sign_up) { _, _ ->
                            Timber.d("Sign up dialog pressed")
                            mainNavController?.navigate(
                                BackupFragmentDirections.actionBackupFragmentToLoginFragment(
                                    LoginFragment.LoginFragmentFlag.REGISTER
                                )
                            )
                        }
                        .setNeutralButton(R.string.cancel, null)
                }
            } else {
                withExternalStoragePermission {
                    viewModel.extractAll(etComment.text.toString())
                }
            }
        }
        if (viewModel.isLoggedIn) {
            val adapter = BackupsAdapter(object : BackupListener {
                override fun onDelete(backup: Backup) {
                    viewModel.deleteBackup(backup.id)
                }

                override fun onInstall(backup: Backup) {
                    viewModel.installBackup(backup.id)
                }
            })
            rvBackups.adapter = adapter
            viewModel.backups.observe(viewLifecycleOwner) { backups ->
                rvBackups.setVisibility(backups.isNotEmpty())
                adapter.submitList(backups)
            }
            viewModel.onSuccess.observe(viewLifecycleOwner) {
                if (it != null) {
                    showToast("Backup was successfully installed to $it")
                }
            }
            viewModel.error.observe(viewLifecycleOwner) { error: String? ->
                if (error != null) showToast(error)
            }
            viewModel.fetchData()
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.backup_fragment_menu, menu)
        menu.setGroupVisible(R.id.logged, viewModel.isLoggedIn)
        menu.setGroupVisible(R.id.not_logged, !viewModel.isLoggedIn)
    }


    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out -> {
                viewModel.signOut()
                true
            }
            R.id.sign_in ->{
                mainNavController?.navigate(
                    BackupFragmentDirections.actionBackupFragmentToLoginFragment(
                        LoginFragment.LoginFragmentFlag.LOGIN
                    )
                )
                true
            }
            R.id.sign_up ->{
                mainNavController?.navigate(
                    BackupFragmentDirections.actionBackupFragmentToLoginFragment(
                        LoginFragment.LoginFragmentFlag.REGISTER
                    )
                )
                true
            }
            else -> super.onMenuItemSelected(item)
        }
    }
}