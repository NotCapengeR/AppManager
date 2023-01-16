package ru.netology.app_manager.ui.virustotal

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import ru.netology.app_manager.R
import ru.netology.app_manager.core.apk.manager.ApkExtractor.extractApkWithPermissions
import ru.netology.app_manager.core.helper.exceptions.LastError
import ru.netology.app_manager.core.helper.viewmodels.withArgsViewModel
import ru.netology.app_manager.core.templates.BaseFragment
import ru.netology.app_manager.databinding.VirusTotalFragmentBinding
import ru.netology.app_manager.di.getAppComponent
import ru.netology.app_manager.utils.HashUtils.md5
import ru.netology.app_manager.utils.HashUtils.sha1
import ru.netology.app_manager.utils.HashUtils.sha256
import ru.netology.app_manager.utils.getErrorMessage
import ru.netology.app_manager.utils.setDebouncedListener
import ru.netology.app_manager.utils.setVisibility
import timber.log.Timber
import javax.inject.Inject

class VirusTotalFragment : BaseFragment<VirusTotalFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VirusTotalFragmentBinding
        get() = VirusTotalFragmentBinding::inflate

    @Inject
    lateinit var factory: VirusTotalViewModel.Factory

    private val args: VirusTotalFragmentArgs by navArgs()
    private val viewModel: VirusTotalViewModel by withArgsViewModel {
        factory.create(args.apkPath, args.appName, args.appVersion)
    }

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
        btnUpload.setDebouncedListener(300L) {
            viewModel.uploadFile()
        }
        toolbar.title = args.appName
        rvDetects.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            progress.setVisibility(loading)
        }
        val adapter = VirusTotalAdapter()
        rvDetects.adapter = adapter
        viewModel.analysis.observe(viewLifecycleOwner) { analysis ->
            rvDetects.setVisibility(!analysis.isNullOrEmpty())
            analysis?.apply {
                adapter.submitList(values.toList())
            }
        }
        viewModel.lastError.observe(viewLifecycleOwner) { lastError ->
            if (lastError != LastError.NO_ERROR) {
                Timber.e(lastError.error.getErrorMessage())
                showToast(lastError.message)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.virus_total_menu, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sha1 -> {
                val sha1 = viewModel.getApk().sha1()
                viewModel.getFileByHash(sha1)
                true
            }

            R.id.sha256 -> {
                val sha1 = viewModel.getApk().sha256()
                viewModel.getFileByHash(sha1)
                true
            }

            R.id.md5 -> {
                val sha1 = viewModel.getApk().md5()
                viewModel.getFileByHash(sha1)
                true
            }
            else -> return super.onMenuItemSelected(item)
        }
    }
}