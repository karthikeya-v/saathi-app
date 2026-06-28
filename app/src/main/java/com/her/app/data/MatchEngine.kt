package com.her.app.data

import kotlin.math.abs

object MatchEngine {

    private val goldenPairs = setOf(
        "INTJ" to "ENFP", "ENFP" to "INTJ",
        "INTP" to "ENFJ", "ENFJ" to "INTP",
        "ENTJ" to "INFP", "INFP" to "ENTJ",
        "ENTP" to "INFJ", "INFJ" to "ENTP",
        "ISTJ" to "ESFP", "ESFP" to "ISTJ",
        "ISFJ" to "ESTP", "ESTP" to "ISFJ",
        "ESTJ" to "ISFP", "ISFP" to "ESTJ",
        "ESFJ" to "ISTP", "ISTP" to "ESFJ"
    )

    private fun baseMbti(type: String) = type.take(4).uppercase()

    private fun isGoldenPair(a: String, b: String) =
        (baseMbti(a) to baseMbti(b)) in goldenPairs

    private fun sharedAxesCount(a: String, b: String): Int {
        val aa = baseMbti(a)
        val bb = baseMbti(b)
        if (aa.length < 4 || bb.length < 4) return 0
        var shared = 0
        if (aa[1] == bb[1]) shared++ // N/S
        if (aa[2] == bb[2]) shared++ // T/F
        if (aa[3] == bb[3]) shared++ // J/P
        return shared
    }

    private fun getRole(type: String): String {
        val base = baseMbti(type)
        if (base.length < 4) return ""
        return when {
            base[1] == 'N' && base[2] == 'T' -> "Analyst"
            base[1] == 'N' && base[2] == 'F' -> "Diplomat"
            base[1] == 'S' && base[3] == 'J' -> "Sentinel"
            else -> "Explorer"
        }
    }

    fun match(profile: UserProfile): List<Personality> {
        val all = Personalities.all

        val genderFiltered = when (profile.interestedIn) {
            "Men"   -> all.filter { it.gender == "Male" }
            "Women" -> all.filter { it.gender == "Female" }
            else    -> all
        }

        val userMbti = profile.mbtiType

        return genderFiltered.sortedByDescending { p ->
            var score = 0

            if (userMbti.length >= 4) {
                if (isGoldenPair(userMbti, p.mbtiType)) {
                    score += 15
                }

                val shared = sharedAxesCount(userMbti, p.mbtiType)
                score += when (shared) {
                    1 -> 10
                    2 -> 7
                    0 -> 5
                    else -> 4
                }

                if (getRole(userMbti) == getRole(p.mbtiType)) score += 3
            }

            val ageDiff = abs(p.age - profile.age)
            score += when {
                ageDiff <= 2 -> 5
                ageDiff <= 5 -> 3
                ageDiff <= 8 -> 1
                else -> 0
            }

            score
        }.take(6)
    }

    fun compatibilityLabel(userMbti: String, personalityMbti: String): String {
        if (userMbti.length < 4) return ""
        return when {
            isGoldenPair(userMbti, personalityMbti) -> "✦ Golden match"
            sharedAxesCount(userMbti, personalityMbti) == 1 -> "✦ Complementary"
            sharedAxesCount(userMbti, personalityMbti) == 2 -> "Similar energy"
            getRole(userMbti) == getRole(personalityMbti) -> "Same tribe"
            else -> "Opposite worlds"
        }
    }
}
