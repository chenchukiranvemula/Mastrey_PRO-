package com.example.data.predictors

data class RankPredictionResult(
    val examTitle: String,
    val estimatedPercentile: String?,
    val estimatedRankRange: String,
    val eligibilitySummary: String,
    val possibleInstitutions: List<InstitutionOption>,
    val disclaimer: String
)

data class InstitutionOption(
    val name: String,
    val type: String, // IIT, NIT, IIIT, GFTI, AIIMS/GMC
    val branch: String,
    val indicativeBand: String
)

/**
 * Historical trend estimator for JEE Main, JEE Advanced, and NEET UG.
 * Clearly communicates uncertainty and never claims official NTA/JoSAA/MCC guarantees.
 */
object RankPredictorEngine {

    private val disclaimerText =
        "Important: Predictions are approximate educational estimates derived from broad multi-year historical trends. Actual percentiles, ranks, and JoSAA/MCC cutoffs vary every year by exam difficulty, total candidates, normalization, category, gender pool, and seat matrix. Always consult official JoSAA/CSAB/MCC portals for counseling decisions."

    fun predictJeeMain(
        marksOutOf300: Int,
        category: String,
        institutionFilter: String = "ALL",
        branchKeyword: String = ""
    ): RankPredictionResult {
        val m = marksOutOf300.coerceIn(-75, 300)
        val (pct, rankLow, rankHigh) = when {
            m >= 260 -> Triple("99.90% – 99.99%", 150, 1500)
            m >= 230 -> Triple("99.65% – 99.89%", 1500, 4800)
            m >= 200 -> Triple("99.10% – 99.60%", 4800, 12500)
            m >= 170 -> Triple("98.00% – 99.05%", 12500, 27000)
            m >= 140 -> Triple("96.20% – 97.95%", 27000, 52000)
            m >= 110 -> Triple("93.00% – 96.10%", 52000, 95000)
            m >= 80 -> Triple("87.00% – 92.80%", 95000, 180000)
            else -> Triple("Below 87%", 180000, 450000)
        }

        val catFactor = when (category) {
            "GEN-EWS", "OBC-NCL" -> 0.85
            "SC", "ST" -> 0.65
            else -> 1.0
        }
        val effectiveRank = (rankLow * catFactor).toInt()

        val options = buildList {
            if (effectiveRank <= 4000) {
                add(InstitutionOption("Top Tier NITs (Trichy / Surathkal / Warangal)", "NIT", "Computer Science / ECE", "Historically ~Top 4k CRL"))
                add(InstitutionOption("IIIT Allahabad / IIIT Hyderabad (JEE mode)", "IIIT", "IT / Electronics", "Historically ~Top 6k CRL"))
            }
            if (effectiveRank <= 18000) {
                add(InstitutionOption("Top NITs (Calicut / Rourkela / Allahabad)", "NIT", "Electrical / Mechanical / ECE", "Historically ~5k–20k CRL"))
                add(InstitutionOption("Mid-Tier IIITs (Gwalior / Jabalpur / Lucknow)", "IIIT", "CSE / IT", "Historically ~8k–22k CRL"))
            }
            if (effectiveRank <= 55000) {
                add(InstitutionOption("State & Regional NITs (Jaipur / Nagpur / Surat / Bhopal)", "NIT", "Civil / Chemical / Mechanical", "Historically ~20k–55k CRL"))
                add(InstitutionOption("Leading GFTIs (BIT Mesra / PEC Chandigarh)", "GFTI", "ECE / Electrical / Mech", "Historically ~25k–65k CRL"))
            }
            if (effectiveRank > 55000) {
                add(InstitutionOption("Newer IIITs & GFTIs (CSAB Special Rounds)", "GFTI", "Core Engineering Branches", "Varies widely by home state & category"))
            }
        }.filter { inst ->
            (institutionFilter == "ALL" || inst.type.equals(institutionFilter, ignoreCase = true)) &&
                (branchKeyword.isBlank() || inst.branch.contains(branchKeyword, ignoreCase = true) || inst.name.contains(branchKeyword, ignoreCase = true))
        }

        val jeeAdvEligible = when (category) {
            "GENERAL" -> m >= 95
            "GEN-EWS", "OBC-NCL" -> m >= 75
            else -> m >= 50
        }

        return RankPredictionResult(
            examTitle = "JEE Main ($category)",
            estimatedPercentile = pct,
            estimatedRankRange = "CRL ~${"%,d".format(rankLow)} – ${"%,d".format(rankHigh)}",
            eligibilitySummary = if (jeeAdvEligible) {
                "Indicatively above historical JEE Advanced qualification zone for $category (subject to yearly NTA cutoff)."
            } else {
                "Focus on boosting accuracy & mock scores to cross typical JEE Advanced cutoff range."
            },
            possibleInstitutions = options,
            disclaimer = disclaimerText
        )
    }

    fun predictJeeAdvanced(
        marksOutOf360: Int,
        category: String
    ): RankPredictionResult {
        val m = marksOutOf360.coerceIn(-40, 360)
        val (rankLow, rankHigh) = when {
            m >= 260 -> 1 to 250
            m >= 210 -> 250 to 1200
            m >= 170 -> 1200 to 3500
            m >= 135 -> 3500 to 8000
            m >= 100 -> 8000 to 16000
            else -> 16000 to 35000
        }

        val options = buildList {
            if (rankLow <= 1500) {
                add(InstitutionOption("Old IITs (Bombay / Delhi / Madras / Kanpur / Kharagpur)", "IIT", "CSE / Electrical / MnC", "Historically ~Top 1.5k"))
            }
            if (rankLow <= 6000) {
                add(InstitutionOption("Old & 2nd Gen IITs (Roorkee / Guwahati / Hyderabad / BHU)", "IIT", "Mechanical / Chemical / Aerospace / ECE", "Historically ~1.5k–7k"))
            }
            if (rankLow <= 16000) {
                add(InstitutionOption("2nd & 3rd Gen IITs (Indore / Patna / Mandi / Jodhpur / Tirupati)", "IIT", "Core & Interdisciplinary Branches", "Historically ~6k–16k"))
            }
        }

        return RankPredictionResult(
            examTitle = "JEE Advanced ($category)",
            estimatedPercentile = null,
            estimatedRankRange = "Indicative CRL ~${"%,d".format(rankLow)} – ${"%,d".format(rankHigh)}",
            eligibilitySummary = if (m >= 100) {
                "Competitive zone for IIT seat counseling depending on yearly paper difficulty."
            } else {
                "Paper difficulty shifts JEE Advanced cutoffs dramatically each year; aim for steady partial-marking accuracy."
            },
            possibleInstitutions = options,
            disclaimer = disclaimerText
        )
    }

    fun predictNeetUg(
        marksOutOf720: Int,
        category: String
    ): RankPredictionResult {
        val m = marksOutOf720.coerceIn(-180, 720)
        val (pct, rankLow, rankHigh) = when {
            m >= 690 -> Triple("99.95%+", 1, 900)
            m >= 660 -> Triple("99.50% – 99.94%", 900, 12000)
            m >= 630 -> Triple("98.50% – 99.49%", 12000, 38000)
            m >= 590 -> Triple("96.00% – 98.49%", 38000, 85000)
            m >= 540 -> Triple("92.00% – 95.99%", 85000, 155000)
            m >= 450 -> Triple("84.00% – 91.99%", 155000, 320000)
            else -> Triple("Below 84%", 320000, 900000)
        }

        val options = buildList {
            if (m >= 680) {
                add(InstitutionOption("AIIMS & Premier Central Institutes", "AIIMS/GMC", "MBBS (All India Quota)", "Historically ~680+ marks"))
            }
            if (m >= 635) {
                add(InstitutionOption("Government Medical Colleges (AIQ 15% & State Quota)", "AIIMS/GMC", "MBBS", "Depends strongly on state cutoff & category"))
            }
            if (m >= 560) {
                add(InstitutionOption("State Quota GMCs / Government Dental (BDS)", "AIIMS/GMC", "MBBS / BDS (State-dependent)", "Varies substantially across states"))
            }
            if (m < 560) {
                add(InstitutionOption("BDS / AYUSH / State Counseling Options", "AIIMS/GMC", "BDS / BAMS / BHMS", "Consult MCC & State counseling bulletins"))
            }
        }

        return RankPredictionResult(
            examTitle = "NEET UG ($category)",
            estimatedPercentile = pct,
            estimatedRankRange = "Indicative AIR ~${"%,d".format(rankLow)} – ${"%,d".format(rankHigh)}",
            eligibilitySummary = "Score $m / 720 (${"%.1f".format((m.coerceAtLeast(0) / 720.0) * 100)}%). NEET score inflation varies significantly by exam year.",
            possibleInstitutions = options,
            disclaimer = disclaimerText
        )
    }
}
