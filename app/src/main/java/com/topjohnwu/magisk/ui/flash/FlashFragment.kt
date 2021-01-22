package com.topjohnwu.magisk.ui.flash

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.navigation.NavDeepLinkBuilder
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.arch.BaseUIActivity
import com.topjohnwu.magisk.arch.BaseUIFragment
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.cmp
import com.topjohnwu.magisk.databinding.FragmentFlashMd2Binding
import com.topjohnwu.magisk.ui.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.topjohnwu.magisk.MainDirections.Companion.actionFlashFragment as toFlash

class FlashFragment : BaseUIFragment<FlashViewModel, FragmentFlashMd2Binding>() {

    override val layoutRes = R.layout.fragment_flash_md2
    override val viewModel by viewModel<FlashViewModel> {
        parametersOf(FlashFragmentArgs.fromBundle(requireArguments()))
    }

    private var defaultOrientation = -1

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
        activity.setTitle(R.string.flash_screen_title)

        viewModel.subtitle.observe(this) {
            activity.supportActionBar?.setSubtitle(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_flash, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return viewModel.onMenuItemClicked(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
    }

    @SuppressLint("WrongConstant")
    override fun onDestroyView() {
        if (defaultOrientation != -1) {
            activity.requestedOrientation = defaultOrientation
        }
        super.onDestroyView()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return when(event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> true
            else -> false
        }
    }

    override fun onBackPressed(): Boolean {
        if (viewModel.loading) return true
        return super.onBackPressed()
    }

    override fun onPreBind(binding: FragmentFlashMd2Binding) = Unit

    companion object {

        private fun createIntent(context: Context, args: FlashFragmentArgs) =
            NavDeepLinkBuilder(context)
                .setGraph(R.navigation.main)
                .setComponentName(MainActivity::class.java.cmp(context.packageName))
                .setDestination(R.id.flashFragment)
                .setArguments(args.toBundle())
                .createPendingIntent()

        private fun flashType(isSecondSlot: Boolean) =
            if (isSecondSlot) Const.Value.FLASH_INACTIVE_SLOT else Const.Value.FLASH_MAGISK

        /* Flashing is understood as installing / flashing magisk itself */

        fun flash(isSecondSlot: Boolean) = toFlash(
            action = flashType(isSecondSlot)
        ).let { BaseUIActivity.postDirections(it) }

        /* Patching is understood as injecting img files with magisk */

        fun patch(uri: Uri) = toFlash(
            action = Const.Value.PATCH_FILE,
            additionalData = uri
        ).let { BaseUIActivity.postDirections(it) }

        /* Uninstalling is understood as removing magisk entirely */

        fun uninstall() = toFlash(
            action = Const.Value.UNINSTALL
        ).let { BaseUIActivity.postDirections(it) }

        /* Installing is understood as flashing modules / zips */

        fun installIntent(context: Context, file: Uri, id: Int = -1) = FlashFragmentArgs(
            action = Const.Value.FLASH_ZIP,
            additionalData = file,
            dismissId = id
        ).let { createIntent(context, it) }

        fun install(file: Uri, id: Int) = toFlash(
            action = Const.Value.FLASH_ZIP,
            additionalData = file,
            dismissId = id
        ).let { BaseUIActivity.postDirections(it) }
    }

}
