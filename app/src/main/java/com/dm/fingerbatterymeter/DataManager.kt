package com.dm.fingerbatterymeter

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.nio.charset.Charset

object DataManager {

    private const val X_FEATURE = "x"
    private const val Y_FEATURE = "y"
    private const val CHARGE_LEVEL = "Сharge level"

    fun loadJsonFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    fun parseJsonFromString(templateData: String): Pair<Array<DoubleArray>, IntArray> {
        try {
            val jsonArray = JSONArray(templateData)
            val length = jsonArray.length()
            val xArray = mutableListOf<DoubleArray>()
            val y = IntArray(length)
            for (i in 0 until length) {
                val jsonObject = jsonArray.getJSONObject(i)
                val xFeature = jsonObject.getDouble(X_FEATURE)
                val yFeature = jsonObject.getDouble(Y_FEATURE)
                val chargeLevel = jsonObject.getInt(CHARGE_LEVEL)

                xArray.add(doubleArrayOf(xFeature, yFeature))
                y[i] = chargeLevel
            }
            return Pair(xArray.toTypedArray(), y)
        } catch (e: JSONException) {
            e.printStackTrace()
            return Pair(emptyArray(), intArrayOf())
        }
    }
}