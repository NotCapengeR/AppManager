package ru.netology.app_manager.ui.activities_list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import ru.netology.app_manager.R
import ru.netology.app_manager.core.helper.adapter_decorators.LinearVerticalSpacingDecoration
import ru.netology.app_manager.core.helper.viewmodels.withArgsViewModel
import ru.netology.app_manager.core.templates.BaseFragment
import ru.netology.app_manager.databinding.ActivitiesFragmentBinding
import ru.netology.app_manager.di.getAppComponent
import ru.netology.app_manager.utils.isVisible
import ru.netology.app_manager.utils.setDebouncedListener
import ru.netology.app_manager.utils.setVisibility
import javax.inject.Inject

class ActivitiesListAppFragment : BaseFragment<ActivitiesFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ActivitiesFragmentBinding
        get() = ActivitiesFragmentBinding::inflate

    @Inject
    lateinit var factory: ActivitiesListAppViewModel.Factory
    private val args: ActivitiesListAppFragmentArgs by navArgs()
    private val viewModel: ActivitiesListAppViewModel by withArgsViewModel {
        factory.create(args.info)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        val adapter = ChildActivitiesListAppAdapter(
            context = requireContext(),
            callback = { item, anchor ->
                showPopupMenu {
                    PopupMenu(anchor.context, anchor).apply {
                        menu.add(0, APP_LAUNCH_ID, Menu.NONE, getString(R.string.activity_launch))
                        setOnMenuItemClickListener { menuItem ->
                            if (menuItem.itemId == APP_LAUNCH_ID) {
                                launchActivity(item)
                            }
                            return@setOnMenuItemClickListener true
                        }
                    }
                }
            }
        )
        tvAppName.text = args.info.appName
        tvPackageName.text = args.info.packageName
        ivAppIcon.setImageDrawable(context?.packageManager?.getApplicationIcon(args.info.appInfo))
        rvActivities.adapter = adapter
        rvActivities.addItemDecoration(
            LinearVerticalSpacingDecoration(INNER_SPACING_RC_VIEW.dpTpPx())
        )
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            if (activities != null) {
                adapter.submitList(activities)
            }
        }
        tvPackageContainer.setDebouncedListener {
            rvActivities.setVisibility(!rvActivities.isVisible)
        }
    }

    private companion object {
        private const val INNER_SPACING_RC_VIEW = 10
        private const val APP_LAUNCH_ID: Int = 1
    }

}