package com.her.app.data

import kotlin.math.abs

object MatchEngine {

    private val vibeScores = mapOf(
        "Explore connection"     to setOf("INFJ", "ENFJ", "INFP", "ENFP"),
        "Practice vulnerability" to setOf("INFJ", "ISFJ", "INFP", "ISFP"),
        "Find what I want"       to setOf("ENTJ", "INTJ", "ENTP", "ESTP"),
        "Just curious"           to setOf("INTP", "ENTP", "ENFP", "ISTP")
    )

    fun match(profile: UserProfile): List<Personality> {
        val all = Personalities.all

        val genderFiltered = when (profile.interestedIn) {
            "Men"   -> all.filter { it.gender == "Male" }
            "Women" -> all.filter { it.gender == "Female" }
            else    -> all
        }

        val preferredTypes = vibeScores[profile.vibe] ?: emptySet()

        return genderFiltered.sortedByDescending { p ->
            var score = 0
            if (p.mbtiType in preferredTypes) score += 10
            val ageDiff = abs(p.age - profile.age)
            score += when {
                ageDiff <= 2 -> 5
                ageDiff <= 5 -> 3
                ageDiff <= 8 -> 1
                else         -> 0
            }
            score
        }.take(6)
    }
}
