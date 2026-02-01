package com.example.climahoy.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.climahoy.R

class PermissionManager private constructor(
    private val context: Context
) {

    companion object {
        fun with(activity: AppCompatActivity): PermissionManager {
            return PermissionManager(activity)
        }

        fun with(fragment: Fragment): PermissionManager {
            return PermissionManager(fragment.requireContext())
        }
    }

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    fun hasLocationPermission(): Boolean {
        return locationPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Verifica si se debe mostrar una explicación de por qué se necesita el permiso
     */
    fun shouldShowLocationRationale(activity: AppCompatActivity): Boolean {
        return locationPermissions.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }
    }

    /**
     * Crea un ActivityResultLauncher para solicitar permisos
     */
    fun createPermissionLauncher(
        activity: AppCompatActivity,
        onGranted: () -> Unit,
        onDenied: (Boolean) -> Unit // Boolean indica si fue denegado permanentemente
    ): ActivityResultLauncher<Array<String>> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }

            if (allGranted) {
                onGranted()
            } else {
                val permanentlyDenied = permissions.any { entry ->
                    !entry.value && !activity.shouldShowRequestPermissionRationale(entry.key)
                }
                onDenied(permanentlyDenied)
            }
        }
    }

    /**
     * Muestra un diálogo explicando por qué se necesita el permiso
     */
    fun showLocationRationaleDialog(
        activity: AppCompatActivity,
        onContinue: () -> Unit,
        onCancel: () -> Unit = {}
    ) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_rationale_title))
            .setMessage(activity.getString(R.string.permission_rationale_message))
            .setPositiveButton(activity.getString(R.string.continue_text)) { _, _ ->
                onContinue()
            }
            .setNegativeButton(activity.getString(R.string.cancel)) { _, _ ->
                onCancel()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Muestra un diálogo cuando el permiso fue denegado permanentemente
     */
    fun showPermissionPermanentlyDeniedDialog(activity: AppCompatActivity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_required_title))
            .setMessage(activity.getString(R.string.permission_permanently_denied_message))
            .setPositiveButton(activity.getString(R.string.open_settings)) { _, _ ->
                openAppSettings(activity)
            }
            .setNegativeButton(activity.getString(R.string.cancel), null)
            .setCancelable(false)
            .show()
    }

    /**
     * Abre la configuración de la aplicación
     */
    private fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    }
}