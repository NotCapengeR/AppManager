package ru.netology.app_manager.ui.app_list

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import ru.netology.app_manager.R
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.apk.manager.ApkExtractor.extractApkWithPermissions
import ru.netology.app_manager.core.apk.manager.ApkExtractor.getApkFile
import ru.netology.app_manager.core.helper.viewmodels.withArgsViewModel
import ru.netology.app_manager.core.templates.BaseFragment
import ru.netology.app_manager.databinding.AppDetailsFragmentBinding
import ru.netology.app_manager.di.getAppComponent
import ru.netology.app_manager.utils.MathUtils.BYTE_LEN
import ru.netology.app_manager.utils.TimeUtils
import ru.netology.app_manager.utils.pow
import ru.netology.app_manager.utils.setDebouncedListener
import ru.netology.app_manager.utils.setVisibility
import java.text.DecimalFormat
import javax.inject.Inject

class AppDetailsFragment : BaseFragment<AppDetailsFragmentBinding>() {

    private val args: AppDetailsFragmentArgs by navArgs()

    @Inject
    lateinit var factory: AppDetailsViewModel.Factory
    private val viewModel: AppDetailsViewModel by withArgsViewModel {
        factory.create(args.packageName)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> AppDetailsFragmentBinding
        get() = AppDetailsFragmentBinding::inflate


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
        viewModel.app.observe(viewLifecycleOwner) { application ->
            if (application != null) {
                toolbar.title = application.appName
                tvAppVersion.text = getString(R.string.app_version, application.version)
                tvPackageName.text = getString(R.string.app_package_name, application.packageName)
                tvApkSize.text = getString(
                    R.string.app_apk_size,
                    FORMAT.format(application.apkSize.toDouble() / BYTE_LEN.pow(2))
                )
                tvAppSize.text = getString(
                    R.string.app_app_size,
                    FORMAT.format(
                        application.appSize.toDouble() / BYTE_LEN.pow(2)
                    )
                )
                tvInstallDate.text = getString(
                    R.string.app_install_date,
                    TimeUtils.formatMillisAbs(application.installDate)
                )
                tvMD5.text = getString(R.string.md5_sum, application.md5)
                tvSHA1.text = getString(R.string.sha1_sum, application.sha1)
                tvSHA256.text = getString(R.string.sha256_sum, application.sha256)

                tvTargetSdkVersion.text =
                    getString(R.string.app_target_sdk, application.targetSdkVersion)
                tvMinSdkVersion.text = getString(R.string.app_min_sdk, application.minSdkVersion)
                tvCompileSdk.text =
                    getString(R.string.app_compile_sdk, application.compileSdkVersion)
                application.permissions.apply {
                    if (isNullOrEmpty()) {
                        tvPermissions.text = getString(R.string.none_app_permissions)
                    } else {
                        tvPermissions.text = application.permissions.toString()
                            .replace("[", "")
                            .replace("]", "")
                    }
                }
                application.features.apply {
                    if (isNullOrEmpty()) {
                        tvFeatures.text = getString(R.string.none_app_features)
                    } else {
                        tvFeatures.text = application.features.toString()
                            .replace("[", "")
                            .replace("]", "")
                    }
                }
                context?.packageManager?.apply {
                    ivAppIcon.setImageDrawable(getApplicationIcon(application.appInfo))
                }
                btnAppLaunch.setDebouncedListener {
                    launchApp(application.packageName)
                }
                btnAppUninstall.setDebouncedListener {
                    uninstallApp(application.packageName)
                }
                ivVirusTotal.setDebouncedListener(300L) {
                    mainNavController?.navigate(
                        AppDetailsFragmentDirections.actionAppDetailsFragmentToVirusTotalFragment(
                            application.sourceApk.path,
                            application.appName,
                            application.version
                        )
                    )
                }
            }
        }

        viewModel.appInfoVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible != null) {
                tvPackageName.setVisibility(isVisible)
                tvApkSize.setVisibility(isVisible)
                tvAppVersion.setVisibility(isVisible)
                tvInstallDate.setVisibility(isVisible)
                tvAppSize.setVisibility(isVisible && viewModel.app.value?.appSize != 0L)
                tvCompileSdk.setVisibility(isVisible && viewModel.app.value?.compileSdkVersion != null)
                tvMinSdkVersion.setVisibility(isVisible && viewModel.app.value?.minSdkVersion != null)
                tvTargetSdkVersion.setVisibility(isVisible)
            }
        }
        tvAppInfo.setDebouncedListener(200L) {
            viewModel.changeAppInfoVisible()
        }
        viewModel.checkSumVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible != null) {
                tvMD5.setVisibility(isVisible)
                tvSHA256.setVisibility(isVisible)
                tvSHA1.setVisibility(isVisible)
            }
        }
        viewModel.appFeaturesVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible != null) {
                tvFeatures.setVisibility(isVisible)
            }
        }
        tvChecksumInfo.setDebouncedListener(200L) {
            viewModel.changeSumVisible()
        }

        viewModel.appPermissionsVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible != null) {
                tvPermissions.setVisibility(isVisible)
            }
        }
        tvPermissionsInfo.setDebouncedListener(200L) {
            viewModel.changePermissionsVisible()
        }
        tvFeaturesInfo.setDebouncedListener(200L) {
            viewModel.changeFeaturesVisible()
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.app_details, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.extract_apk -> {
                viewModel.app.value?.apply {
                    val file = getApkFile(this)
                    extractApkWithPermissions(
                        info = this,
                        onSuccess = { showToast("Apk was successfully extracted to: ${file?.path}") },
                        onError = { showToast("Could not extract APK: $appName") }
                    )
                }
                true
            }

            R.id.activity_launcher -> {
                viewModel.app.value?.apply {
                    mainNavController?.navigate(
                        AppDetailsFragmentDirections.actionAppDetailsFragmentToActivitiesListAppFragment(
                            this
                        )
                    )
                }
                true
            }
            else -> return super.onMenuItemSelected(item)
        }
    }

    companion object {
        var FORMAT = DecimalFormat("0.##")
    }
}