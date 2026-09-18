package com.artrubadur.teno.agent.tools.integrations.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import com.artrubadur.teno.agent.tools.integrations.search.expandSearchQueries
import com.artrubadur.teno.agent.tools.integrations.search.matchesSearchQuery
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun Context.launchableApplications(): List<ApplicationInfo> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return packageManager.queryIntentActivities(intent, 0)
        .map { it.activityInfo.applicationInfo }
        .distinctBy(ApplicationInfo::packageName)
        .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }
}

internal fun Context.appMetadata(packageName: String): JsonObject {
    val application = launchableApplications()
        .firstOrNull { it.packageName == packageName }
        ?: error("Launchable application not found: $packageName")
    return metadataJson(application)
}

internal fun Context.searchApplications(query: String): JsonArray {
    val searchQueries = expandSearchQueries(listOf(query))
    return JsonArray(
        launchableApplications()
            .filter { application ->
                val packageName = application.packageName
                val label = packageManager.getApplicationLabel(application).toString()
                searchQueries.any { searchQuery ->
                    packageName.matchesSearchQuery(searchQuery) || label.matchesSearchQuery(
                        searchQuery
                    )
                }
            }
            .map { summaryJson(it) }
    )
}

internal fun Context.openApplication(packageName: String): JsonObject {
    if (launchableApplications().none { it.packageName == packageName }) {
        error("Launchable application not found: $packageName")
    }
    val intent = packageManager.getLaunchIntentForPackage(packageName)
        ?: error("Application has no launch activity: $packageName")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    return buildJsonObject {
        put("ok", true)
        put("package_name", packageName)
    }
}

private fun Context.applicationVersion(packageName: String): Pair<String?, Long> {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return packageInfo.versionName to packageInfo.longVersionCode
}

internal fun Context.summaryJson(application: ApplicationInfo): JsonObject =
    buildJsonObject {
        put("package_name", application.packageName)
        put("name", packageManager.getApplicationLabel(application).toString())
    }

private fun Context.metadataJson(application: ApplicationInfo): JsonObject {
    val (versionName, versionCode) = applicationVersion(application.packageName)
    return buildJsonObject {
        summaryJson(application).forEach { (key, value) -> put(key, value) }
        put("version_name", versionName)
        put("version_code", versionCode)
        put("enabled", application.enabled)
    }
}
