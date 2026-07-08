package com.faiz.bumiloka

import android.graphics.*
import android.widget.ImageView

object BadgeVisualHelper {

    /**
     * Melukis lencana kotak hijau dengan inisial huruf putih tebal (Gaya Admin).
     */
    fun renderBadge(imageView: ImageView, name: String, level: Int = 1) {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        
        // 1. Warna Hijau Solid (Sesuai Gaya Admin BumiLoka)
        val bgColor = Color.parseColor("#38761D") 

        // 2. Gambar Kotak Rounded (Sedikit lebih kotak sesuai gambar request)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val rect = RectF(30f, 30f, size - 30f, size - 30f)
        canvas.drawRoundRect(rect, 80f, 80f, bgPaint)

        // 3. Logika Inisial Huruf (Contoh: "Kesatria Hijau" -> "KH")
        val cleanName = name.trim()
        val initial = when {
            cleanName.isEmpty() -> "?"
            cleanName.contains(" ") -> {
                val words = cleanName.split("\\s+".toRegex())
                if (words.size >= 2) {
                    (words[0].take(1) + words[1].take(1)).uppercase()
                } else {
                    words[0].take(2).uppercase()
                }
            }
            cleanName.length >= 2 -> cleanName.take(2).uppercase()
            else -> cleanName.uppercase()
        }

        // 4. Lukis Huruf Putih Tebal di Tengah
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = size * 0.45f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val fontMetrics = textPaint.fontMetrics
        val yPos = center - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(initial, center, yPos, textPaint)

        // Reset state ImageView agar bersih
        imageView.background = null
        imageView.imageTintList = null
        imageView.colorFilter = null
        imageView.setImageBitmap(bitmap)
    }
}
