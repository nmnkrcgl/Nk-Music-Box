package com.muzikdolabi.app

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BagimsizVeriYonetici(private val context: Context) {

    companion object {
        // Kendi sunucundaki JSON dosyasının tam adresi. Değiştirmezsen güncelleme denetimi atlanır.
        const val CANLI_JSON_URL = "https://SIZIN-ALAN-ADINIZ.com/muzik_dolabi.json"
        private const val YEREL_DOSYA = "muzik_dolabi_guncel.json"
        private const val ASSET_DOSYA = "muzik_dolabi.json"
    }

    private fun versiyon(metin: String): Int =
        try { JSONObject(metin).getInt("versiyon") } catch (e: Exception) { -1 }

    private fun assetOku(): String =
        context.assets.open(ASSET_DOSYA).bufferedReader().use { it.readText() }

    // Ekrana gidecek veri: indirilen ve APK içindeki dosyadan sürümü yüksek olan kazanır.
    // (APK güncellenip içindeki liste daha yeniyse eski indirme ezmez.)
    fun kutuphaneyiOku(): String {
        val apkVeri = assetOku()
        val dosya = File(context.filesDir, YEREL_DOSYA)
        val indirilen = if (dosya.exists()) try { dosya.readText() } catch (e: Exception) { null } else null
        return if (indirilen != null && versiyon(indirilen) > versiyon(apkVeri)) indirilen else apkVeri
    }

    // Sunucuda daha yeni sürüm varsa indirir; çalışan listeyi bir sonraki açılışta değiştirir.
    suspend fun sunucudanGuncellemeDenetle() = withContext(Dispatchers.IO) {
        if (CANLI_JSON_URL.contains("SIZIN-ALAN")) return@withContext
        try {
            val baglanti = URL(CANLI_JSON_URL).openConnection() as HttpURLConnection
            baglanti.connectTimeout = 8000
            baglanti.readTimeout = 8000
            val canliMetin = baglanti.inputStream.bufferedReader().use { it.readText() }
            val canli = JSONObject(canliMetin)
            // Bozuk veya eksik dosyayı kabul etme
            if (!canli.has("sekmeler")) return@withContext
            if (canli.getInt("versiyon") > versiyon(kutuphaneyiOku())) {
                val gecici = File(context.filesDir, "$YEREL_DOSYA.tmp")
                gecici.writeText(canliMetin)
                gecici.renameTo(File(context.filesDir, YEREL_DOSYA))
            }
        } catch (e: Exception) {
            // İnternet yok veya sunucu kapalı: mevcut listeyle devam
        }
    }
}
