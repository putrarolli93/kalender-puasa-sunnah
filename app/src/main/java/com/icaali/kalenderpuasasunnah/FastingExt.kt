package com.icaali.kalenderpuasasunnah

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class PuasaTanggal(
    val code: Int,
    val tanggal: Long,
    val masehi: String
)

data class TanggalModel(
    val month: String,
    val month_number: Int,
    val puasa_code: List<PuasaCode>,
    val tanggal_puasa: List<TanggalPuasa>
)

data class PuasaCode(
    val code: Int
)

data class TanggalPuasa(
    val code: Int,
    val tanggal: Long,
    val masehi: String
)

@RequiresApi(Build.VERSION_CODES.O)
fun generateTanggalModel(year: Int): List<TanggalModel> {

    val zone = ZoneId.of("Asia/Jakarta")
    val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale("id", "ID"))

    val groupedByMonth = mutableMapOf<Int, MutableList<TanggalPuasa>>()

    val start = LocalDate.of(year, 1, 1)
    val end = LocalDate.of(year, 12, 31)

    var date = start

    // 🔥 reuse calendar (lebih ringan)
    val cal = UmmalquraCalendar()

    while (!date.isAfter(end)) {

        val epoch = date.atStartOfDay(zone).toEpochSecond()

        cal.time = Date.from(date.atStartOfDay(zone).toInstant())

        val hijriMonth = cal.get(UmmalquraCalendar.MONTH) + 1
        val hijriDay = cal.get(UmmalquraCalendar.DAY_OF_MONTH)

        val monthList = groupedByMonth.getOrPut(date.monthValue) {
            mutableListOf()
        }

        fun add(code: Int) {
            monthList.add(
                TanggalPuasa(
                    code,
                    epoch,
                    date.format(formatter)
                )
            )
        }

        /*
         PRIORITY:
         Ramadan > Arafah > Asyura > Ayyamul Bidh > Senin Kamis
        */

        when {

            // Ramadan
            hijriMonth == 9 -> add(3)

            // Arafah
            hijriMonth == 12 && hijriDay == 9 -> add(4)

            // Asyura
            hijriMonth == 1 && hijriDay == 10 -> add(5)

            // Ayyamul Bidh
            hijriDay in 13..15 -> add(2)

            // Senin Kamis
            date.dayOfWeek == DayOfWeek.MONDAY ||
                    date.dayOfWeek == DayOfWeek.THURSDAY -> add(1)
        }

        // 🔥 INI YANG TADI KURANG
        date = date.plusDays(1)
    }

    return groupedByMonth.map { (monthNumber, tanggalList) ->

        val codes = tanggalList
            .map { it.code }
            .distinct()
            .map { PuasaCode(it) }

        TanggalModel(
            month = Month.of(monthNumber)
                .getDisplayName(TextStyle.FULL, Locale("id", "ID")),
            month_number = monthNumber,
            puasa_code = codes,
            tanggal_puasa = tanggalList.sortedBy { it.tanggal }
        )
    }.sortedBy { it.month_number }
}