package com.muzikdolabi.app

import android.content.Context
import android.content.Intent
import android.net.Uri

class PlayerService(private val context: Context) {

    // Şarkıya tıklandığında duraksamadan doğrudan çalar/yönlendirir
    fun playSong(artist: String, title: String, spotifyUrl: String?, youtubeUrl: String?) {
        val query = Uri.encode("$artist $title")
        
        if (!spotifyUrl.isNull_or_Empty()) {
            openApp(spotifyUrl)
        } else if (!youtubeUrl.isNull_or_Empty()) {
            openApp(youtubeUrl)
        } else {
            // Varsayılan olarak Spotify veya YouTube arama bağlantısına doğrudan yönlendirir
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:$query"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query"))
                ytIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(ytIntent)
            }
        }
    }

    private fun openApp(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
