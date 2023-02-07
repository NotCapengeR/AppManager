package ru.netology.app_manager.core.templates

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import ru.netology.app_manager.R
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.apk.models.ActivityData
import ru.netology.app_manager.utils.AndroidUtils

abstract class BaseFragment<VB : ViewBinding> : Fragment(), MenuProvider {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    private var popupMenu: PopupMenu? = null
    protected val mainNavController: NavController? by lazy { activity?.findNavController(R.id.nav_host_fragment) }

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = requireNotNull(_binding) as VB

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        clearKeyboard()
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun clearKeyboard(editText: EditText? = null) {
        AndroidUtils.hideKeyboard(activity as AppCompatActivity)
        AndroidUtils.clearEditText(editText)
    }

    protected fun showKeyboard(mEtSearch: EditText) {
        AndroidUtils.showKeyboard(mEtSearch, requireContext())
    }

    protected inline fun <T : BaseFragment<VB>> T.onBackPressed(callback: T.() -> Unit = {}) {
        this.callback()
        mainNavController?.navigateUp()
    }

    protected fun showToast(message: String?, isLong: Boolean = false) {
        AndroidUtils.showToast(requireContext(), message, isLong)
    }

    protected fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(getString(msgResId), isLong)
    }

    protected fun showPopupMenu(inflater: () -> PopupMenu) {
        if (activity?.isFinishing == false) {
            popupMenu = inflater.invoke()
            popupMenu?.show()
        }
    }

    protected fun showDialog(build: AlertDialog.Builder.() -> AlertDialog.Builder) {
        if (activity?.isFinishing == false) AlertDialog.Builder(requireActivity()).build().show()
    }


    protected fun showSnackbar(text: String, isLong: Boolean = false) {
        if (activity?.isFinishing == false) {
            AndroidUtils.showShackbar(
                requireContext(),
                binding.root,
                text,
                isLong
            )
        }
    }

    protected fun openUrl(url: String?) {
        AndroidUtils.openUrl(requireContext(), url)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {}

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> false
        }
    }

    protected fun Int.dpTpPx(): Int = AndroidUtils.dpToPx(activity as AppCompatActivity, this)

    protected fun launchApp(packageName: String) {
        ApkExtractor.launchApp(packageName, requireContext())
    }

    protected fun launchActivity(packageName: String, activityName: String) {
        ApkExtractor.launchActivity(packageName, activityName, requireContext())
    }

    protected fun launchActivity(activity: ActivityData) {
        launchActivity(activity.packageName, activity.name)
    }

    protected fun uninstallApp(packageName: String) {
        ApkExtractor.uninstallApp(packageName, requireContext())
    }

    protected fun getAllActivitiesFromApp(packageName: String): List<ActivityData> =
        ApkExtractor.getAllActivitiesFromApp(
            requireContext(),
            packageName
        )
}