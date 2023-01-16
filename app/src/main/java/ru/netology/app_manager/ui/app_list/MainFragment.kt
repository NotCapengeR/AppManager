package ru.netology.app_manager.ui.app_list

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import ru.netology.app_manager.R
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.apk.manager.ApkExtractor.withExternalStoragePermission
import ru.netology.app_manager.core.apk.models.Apk
import ru.netology.app_manager.core.helper.adapter_decorators.LinearVerticalSpacingDecoration
import ru.netology.app_manager.core.helper.viewmodels.ViewModelFactory
import ru.netology.app_manager.core.templates.BaseFragment
import ru.netology.app_manager.databinding.FragmentMainBinding
import ru.netology.app_manager.di.getAppComponent
import timber.log.Timber
import javax.inject.Inject


class MainFragment : BaseFragment<FragmentMainBinding>() {

    @Inject
    lateinit var factory: ViewModelFactory

    private val viewModel: AppListViewModel by activityViewModels {
        factory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate

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
        val listener = object : AppListListener {
            override fun onDetails(apk: Apk) {
                mainNavController?.navigate(
                    MainFragmentDirections.actionMainFragmentToAppDetailsFragment(apk.packageName)
                )
            }

        }
        val adapter = AppListAdapter(requireContext(), listener)
        rvApps.adapter = adapter
        rvApps.addItemDecoration(
            LinearVerticalSpacingDecoration(INNER_SPACING_RC_VIEW.dpTpPx())
        )
        rvApps.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        viewModel.appList.observe(viewLifecycleOwner) { apps ->
            adapter.submitList(apps)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading != null) {
                refreshAppsLayout.isRefreshing = isLoading
            }
        }
        refreshAppsLayout.setOnRefreshListener(viewModel::loadApps)

    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_backup -> {
                mainNavController?.navigate(MainFragmentDirections.actionMainFragmentToBackupFragment())
                true
            }
            R.id.action_activities -> {
                mainNavController?.navigate(MainFragmentDirections.actionMainFragmentToActivityListFragment())
                true
            }
            else -> super.onMenuItemSelected(item)
        }
    }

    private companion object {
        private const val INNER_SPACING_RC_VIEW: Int = 10
    }
}