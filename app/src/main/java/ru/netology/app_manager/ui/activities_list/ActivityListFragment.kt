package ru.netology.app_manager.ui.activities_list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import ru.netology.app_manager.core.helper.adapter_decorators.LinearVerticalSpacingDecoration
import ru.netology.app_manager.core.helper.viewmodels.ViewModelFactory
import ru.netology.app_manager.core.templates.BaseFragment
import ru.netology.app_manager.databinding.ActivityListFragmentBinding
import ru.netology.app_manager.di.getAppComponent
import ru.netology.app_manager.ui.app_list.AppListViewModel
import javax.inject.Inject

class ActivityListFragment : BaseFragment<ActivityListFragmentBinding>() {


    @Inject
    lateinit var factory: ViewModelFactory
    private val viewModel: AppListViewModel by activityViewModels { factory }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ActivityListFragmentBinding
        get() = ActivityListFragmentBinding::inflate


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
        val adapter = ParentActivityListAdapter(requireContext())
        rvApps.adapter = adapter
        rvApps.addItemDecoration(
            LinearVerticalSpacingDecoration(INNER_SPACING_RC_VIEW.dpTpPx())
        )
        viewModel.appList.observe(viewLifecycleOwner) { apks ->
            adapter.submitList(apks)
        }
    }


    private companion object {
        private const val INNER_SPACING_RC_VIEW = 10
    }

}