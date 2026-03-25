package com.decli.codehelper.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Helper for detecting MIUI / HyperOS and guiding users to enable
 * the "通知类短信" (notification SMS / Service_SMS) permission.
 *
 * MIUI splits the standard READ_SMS permission into two independent
 * permissions:
 *   1. 普通短信 – granted via standard runtime permission flow
 *   2. 通知类短信 – MIUI-private, **off by default**, cannot be
 *      requested programmatically
 *
 * Platform messages (e.g. 菜鸟驿站) are classified as "通知类短信",
 * so they are invisible to third-party apps unless the user manually
 * enables this permission in MIUI Settings.
 */
object MiuiPermissionHelper {

    /** Returns `true` when running on a MIUI / HyperOS ROM. */
    fun isMiui(): Boolean =
        getSystemProperty("ro.miui.ui.version.name").isNotBlank()

    /**
     * Build an [Intent] that opens the MIUI per-app permission editor.
     *
     * Three targets are attempted (different MIUI versions use different
     * Activity class names).  The first resolvable intent is returned;
     * if none resolves, falls back to the standard Android application
     * details screen.
     */
    fun buildPermissionEditorIntent(context: Context): Intent {
        val pkg = context.packageName

        val candidates = listOf(
            // MIUI 10+ / HyperOS
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity",
                )
                putExtra("extra_pkgname", pkg)
            },
            // Older MIUI versions
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity",
                )
                putExtra("extra_pkgname", pkg)
            },
        )

        val resolved = candidates.firstOrNull { intent ->
            intent.resolveActivity(context.packageManager) != null
        }

        return resolved ?: Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", pkg, null)
        }
    }

    private fun getSystemProperty(key: String): String =
        runCatching {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            method.invoke(null, key, "") as? String
        }.getOrNull().orEmpty()
}
