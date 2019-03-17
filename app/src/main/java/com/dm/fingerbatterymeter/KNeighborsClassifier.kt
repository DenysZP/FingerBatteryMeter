package com.dm.fingerbatterymeter

import android.content.Context
import java.util.*

internal class KNeighborsClassifier(
    context: Context,
    templateFileName: String,
    private val nNeighbors: Int
) {

    companion object {

        private const val CLASS_COUNT = 7
        private const val POWER = 2.0

        private fun compute(temp: DoubleArray, cand: DoubleArray, q: Double): Double {
            var dist = 0.0
            var diff: Double
            var i = 0
            val l = temp.size
            while (i < l) {
                diff = Math.abs(temp[i] - cand[i])
                if (q == 1.0) {
                    dist += diff
                } else if (q == 2.0) {
                    dist += diff * diff
                } else if (q == java.lang.Double.POSITIVE_INFINITY) {
                    if (diff > dist) {
                        dist = diff
                    }
                } else {
                    dist += Math.pow(diff, q)
                }
                i++
            }
            return if (q == 1.0 || q == java.lang.Double.POSITIVE_INFINITY) {
                dist
            } else if (q == 2.0) {
                Math.sqrt(dist)
            } else {
                Math.pow(dist, 1.0 / q)
            }
        }
    }

    private val X: Array<DoubleArray>
    private val y: IntArray
    private val nTemplates: Int

    init {
        val templateData = DataManager.loadJsonFromAsset(context, templateFileName) ?: ""
        val template = DataManager.parseJsonFromString(templateData)
        X = template.first
        y = template.second
        nTemplates = y.size
    }

    private class Neighbor(var clazz: Int, var dist: Double)

    fun predict(features: DoubleArray): Int {
        var classIdx = 0
        if (this.nNeighbors == 1) {
            var minDist = java.lang.Double.POSITIVE_INFINITY
            var curDist: Double
            for (i in 0 until this.nTemplates) {
                curDist = KNeighborsClassifier.compute(this.X[i], features, POWER)
                if (curDist <= minDist) {
                    minDist = curDist
                    classIdx = y[i]
                }
            }
        } else {
            val classes = IntArray(CLASS_COUNT)
            val dists = ArrayList<Neighbor>()
            for (i in 0 until this.nTemplates) {
                dists.add(
                    Neighbor(
                        y[i],
                        KNeighborsClassifier.compute(this.X[i], features, POWER)
                    )
                )
            }
            dists.sortWith(Comparator { n1, n2 -> n1.dist.compareTo(n2.dist) })
            for (neighbor in dists.subList(0, nNeighbors)) {
                classes[neighbor.clazz]++
            }
            for (i in 0 until CLASS_COUNT) {
                classIdx = if (classes[i] > classes[classIdx]) i else classIdx
            }
        }
        return classIdx
    }
}