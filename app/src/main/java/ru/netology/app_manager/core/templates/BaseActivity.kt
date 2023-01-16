package ru.netology.app_manager.core.templates

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.PopupMenu
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import ru.netology.app_manager.R
import ru.netology.app_manager.utils.AndroidUtils


abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater) -> VB
    private var popupMenu: PopupMenu? = null
    protected val mainNavController: NavController? by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                as NavHostFragment).navController
    }
    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = requireNotNull(_binding) as VB

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater).also { setContentView(it.root) }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    protected fun showToast(message: String?, isLong: Boolean = false) {
        AndroidUtils.showToast(this, message, isLong)
    }

    protected fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(getString(msgResId), isLong)
    }

    protected fun openUrl(url: String?) {
        AndroidUtils.openUrl(this, url)
    }


    protected fun clearKeyboard(editText: EditText? = null) {
        AndroidUtils.hideKeyboard(this)
        AndroidUtils.clearEditText(editText)
    }

    protected fun showKeyboard(mEtSearch: EditText) {
        AndroidUtils.showKeyboard(mEtSearch, this)
    }

    protected fun showPopupMenu(inflater: () -> PopupMenu) {
        popupMenu = inflater.invoke()
        popupMenu?.show()
    }

    protected fun showSnackbar(text: String, isLong: Boolean = false) = AndroidUtils.showShackbar(
        this,
        binding.root,
        text,
        isLong
    )


    protected fun Int.dpTpPx(): Int = AndroidUtils.dpToPx(this@BaseActivity, this)

    fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    fun uninstallApp(packageName: String) {
        Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
            startActivity(this)
        }
    }
}