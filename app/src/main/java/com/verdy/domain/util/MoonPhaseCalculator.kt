package com.verdy.domain.util

import java.time.LocalDate

data class MoonPhase(
    val name: String,
    val emoji: String,
    val gardeningTip: String,
    val dayOfCycle: Double
)

object MoonPhaseCalculator {

    // Reference new moon: January 6, 2000 12:24 UTC → Julian Day 2451549.5
    private const val REFERENCE_NEW_MOON_JD = 2451549.5
    private const val SYNODIC_PERIOD = 29.53058867 // days

    fun currentPhase(date: LocalDate = LocalDate.now()): MoonPhase {
        val jd = toJulianDay(date)
        val elapsed = (jd - REFERENCE_NEW_MOON_JD) % SYNODIC_PERIOD
        val dayOfCycle = if (elapsed < 0) elapsed + SYNODIC_PERIOD else elapsed
        return phaseFromDays(dayOfCycle)
    }

    private fun toJulianDay(date: LocalDate): Double {
        val y = date.year
        val m = date.monthValue
        val d = date.dayOfMonth
        val a = (14 - m) / 12
        val yr = y + 4800 - a
        val mo = m + 12 * a - 3
        return d + (153 * mo + 2) / 5 + 365L * yr + yr / 4 - yr / 100 + yr / 400 - 32045.0
    }

    private fun phaseFromDays(days: Double): MoonPhase = when {
        days < 1.85 || days >= 27.68 -> MoonPhase(
            name = "Luna Nueva",
            emoji = "🌑",
            gardeningTip = "Ideal para preparar el suelo, compostar y planificar siembras.",
            dayOfCycle = days
        )
        days < 7.38 -> MoonPhase(
            name = "Creciente Inicial",
            emoji = "🌒",
            gardeningTip = "Buen momento para sembrar cereales, flores y plantas de hoja.",
            dayOfCycle = days
        )
        days < 9.22 -> MoonPhase(
            name = "Cuarto Creciente",
            emoji = "🌓",
            gardeningTip = "Favorable para plantar frutas y tubérculos. Riega con generosidad.",
            dayOfCycle = days
        )
        days < 14.77 -> MoonPhase(
            name = "Creciente Gibosa",
            emoji = "🌔",
            gardeningTip = "Excelente para trasplantar, fertilizar y tratar enfermedades.",
            dayOfCycle = days
        )
        days < 16.61 -> MoonPhase(
            name = "Luna Llena",
            emoji = "🌕",
            gardeningTip = "Máxima energía de la planta. Ideal para cosechar y propagar.",
            dayOfCycle = days
        )
        days < 22.15 -> MoonPhase(
            name = "Menguante Gibosa",
            emoji = "🌖",
            gardeningTip = "Tiempo de poda, control de plagas y tratamientos foliares.",
            dayOfCycle = days
        )
        days < 24.0 -> MoonPhase(
            name = "Cuarto Menguante",
            emoji = "🌗",
            gardeningTip = "Período de descanso. Evita trasplantes y siembras importantes.",
            dayOfCycle = days
        )
        else -> MoonPhase(
            name = "Menguante Final",
            emoji = "🌘",
            gardeningTip = "Preparación del sustrato, compostaje y limpieza del jardín.",
            dayOfCycle = days
        )
    }
}
