package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object NavigationHelper {

    fun openMapLocation(
        context: Context,
        destinationQuery: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        try {
            val uri = if (latitude != null && longitude != null) {
                val encodedQuery = URLEncoder.encode(destinationQuery, "UTF-8")
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedQuery)")
            } else {
                val encodedQuery = URLEncoder.encode(destinationQuery, "UTF-8")
                Uri.parse("geo:0,0?q=$encodedQuery")
            }

            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Browser fallback
                val encoded = URLEncoder.encode(destinationQuery, "UTF-8")
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")
                val browserIntent = Intent(Intent.ACTION_VIEW, browserUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open map: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openTurnByTurnNavigation(
        context: Context,
        destinationQuery: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        try {
            val navUri = if (latitude != null && longitude != null) {
                Uri.parse("google.navigation:q=$latitude,$longitude")
            } else {
                val encoded = URLEncoder.encode(destinationQuery, "UTF-8")
                Uri.parse("google.navigation:q=$encoded")
            }

            val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (navIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(navIntent)
            } else {
                // Fallback to general geo map
                openMapLocation(context, destinationQuery, latitude, longitude)
            }
        } catch (e: Exception) {
            openMapLocation(context, destinationQuery, latitude, longitude)
        }
    }
}
