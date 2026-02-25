package com.icaali.kalenderpuasasunnah

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.icaali.kalenderpuasasunnah.databinding.ActivityMainBinding
import com.icaali.kalenderpuasasunnah.databinding.CalendarDayLayoutBinding
import com.icaali.kalenderpuasasunnah.databinding.LayoutCalendarLegendHeaderBinding
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.icaali.kalenderpuasasunnah.detail.DetailPuasaActivity
import com.icaali.kalenderpuasasunnah.utils.LegendAdapter
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.WeekFields
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), LegendAdapter.OnLegendedListener {

    private val daysOfWeek = daysOfWeekFromLocale()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM", Locale("id", "ID"))
    private fun Long.startDateMillis(): Long = this * DateUtils.SECOND_IN_MILLIS
    private val puasaEvent: MutableList<TanggalPuasa> = mutableListOf()

    // FIX 3: HashMap untuk lookup O(1) di dayBinder
    private val puasaEventMap: HashMap<Long, TanggalPuasa> = HashMap()

    // FIX 2: Cache hasil konversi Arabic number agar tidak re-create object tiap render cell
    private val arabicNumberCache = HashMap<LocalDate, String>()

    private var selectedDate: LocalDate? = null
    private var tanggalJson: List<TanggalModel>? = null
    private var monthSelected: Int = 0
    private var yearSelectDate = 0
    lateinit var adapter: LegendAdapter
    val nowYear = LocalDate.now().year
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlackImage)
        binding.scMain.isNestedScrollingEnabled = false
        this.tanggalJson = generateMultiYear(
            nowYear - 1,
            nowYear + 1
        )

        // FIX 1: Populate puasaEvent & puasaEventMap SEKALI di sini,
        // bukan di dalam monthScrollListener yang dipanggil tiap scroll bulan
        this.tanggalJson?.forEach { tanggal ->
            tanggal.tanggal_puasa.forEach { puasa ->
                this.puasaEvent.add(puasa)
                val key = convertDateGMT(puasa.tanggal)
                puasaEventMap[key] = puasa
            }
        }

        this.setUpList()
        this.setUpCalendar()
        binding.iShare.btnShare.setOnClickListener {
            shareApp()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateMultiYear(startYear: Int, endYear: Int): List<TanggalModel> {
        val result = mutableListOf<TanggalModel>()
        for (year in startYear..endYear) {
            result.addAll(generateTanggalModel(year)!!)
        }
        return result
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Kalender Puasa Sunnah")
        var shareMessage =
            "\nAssalamualaikum, ini ada aplikasi android bagus, yaitu Kalender Puasa Sunnah. Jika berkenan bisa install di HP dengan klik\n"
        shareMessage =
            "${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".trimIndent() + "\n" + " Terima kasih"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "choose one"))
    }

    private fun setUpList() {
        binding.rvLegendLayout.layoutManager = LinearLayoutManager(this)
        adapter = LegendAdapter(this)
        binding.rvLegendLayout.adapter = adapter
        binding.rvLegendLayout.isNestedScrollingEnabled = false
    }

    private fun setUpCalendar() {

        val currentMonth = YearMonth.now()
        val firstMonth = YearMonth.of(nowYear - 1, 1)   // Januari tahun lalu
        val lastMonth = YearMonth.of(nowYear + 1, 12)    // Desember tahun depan
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        binding.calendarView.setup(firstMonth, lastMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)
        onSetHeaderBinder()
        onMonthScrollListener()

        class DayViewContainer(view: View) : ViewContainer(view) {
            private val binding = CalendarDayLayoutBinding.bind(view)

            val textView = binding.calendarDayText
            val textArabicNumber = binding.calendarArabicText
            val marker = binding.dotMarker
        }

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
                // FIX 2: Gunakan cached version
                container.textArabicNumber.text = convertToArabicNumberCached(day.date)
                val textView = container.textView
                val textArabicNumber = container.textArabicNumber

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    textArabicNumber.visibility = View.VISIBLE
                    container.marker.visibility = View.INVISIBLE
                    when (day.date) {
                        selectedDate -> {
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorRed
                                )
                            )
                            container.marker.visibility = View.INVISIBLE
                        }
                        else -> {
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorBlack
                                )
                            )
                            textArabicNumber.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorBlack
                                )
                            )
                            textView.background = null
                            container.marker.visibility = View.INVISIBLE
                        }
                    }

                    if (day.date.dayOfWeek == DayOfWeek.SUNDAY) {
                        textView.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.colorPrimaryred
                            )
                        )
                        textArabicNumber.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.colorBlack
                            )
                        )
                        container.marker.visibility = View.INVISIBLE
                    }
                    if (day.date.dayOfWeek == DayOfWeek.FRIDAY) {
                        textView.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.colorPrimaryGreen
                            )
                        )
                        textArabicNumber.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.colorBlack
                            )
                        )
                        container.marker.visibility = View.INVISIBLE
                    }

                    // FIX 3: Lookup O(1) pakai HashMap, bukan linear search find{}
                    val key = convertLocalTimeToLong(day.date.toString())
                    val isEvent = puasaEventMap[key]

                    when (isEvent?.code) {
                        2 -> {
                            textView.background = getDrawable(R.drawable.bg_ayamul_bidh)
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            textArabicNumber.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            container.marker.visibility = View.INVISIBLE
                        }
                        3 -> {
                            textView.background = getDrawable(R.drawable.bg_ramadhan)
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            textArabicNumber.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            container.marker.visibility = View.INVISIBLE
                        }
                        4 -> {
                            textView.background = getDrawable(R.drawable.bg_arafah)
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            textArabicNumber.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            container.marker.visibility = View.INVISIBLE
                        }
                        5 -> {
                            textView.background = getDrawable(R.drawable.bg_asyura)
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            textArabicNumber.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            container.marker.visibility = View.INVISIBLE
                        }
                        8 -> {
                            textView.background = getDrawable(R.drawable.bg_haram_puasa)
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            textArabicNumber.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorWhite
                                )
                            )
                            container.marker.visibility = View.INVISIBLE
                        }
                        else -> {
                            if (day.date.dayOfWeek == DayOfWeek.MONDAY) {
                                textView.background = getDrawable(R.drawable.bg_senin_kamis)
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.colorBlack
                                    )
                                )
                                textArabicNumber.setTextColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.colorBlack
                                    )
                                )
                                container.marker.visibility = View.INVISIBLE
                            } else if (day.date.dayOfWeek == DayOfWeek.THURSDAY) {
                                textView.background = getDrawable(R.drawable.bg_senin_kamis)
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.colorBlack
                                    )
                                )
                                textArabicNumber.setTextColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.colorBlack
                                    )
                                )
                                container.marker.visibility = View.INVISIBLE
                            } else {
                                container.marker.visibility = View.INVISIBLE
                            }
                        }
                    }
                    if (day.date == today) {
                        textView.setTypeface(null, Typeface.BOLD)
                        val s = day.date.dayOfMonth.toString()
                        container.textView.text = Html.fromHtml("<u>$s</u>")
                        container.marker.visibility = View.VISIBLE
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                    textArabicNumber.visibility = View.INVISIBLE
                    container.marker.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun onSetHeaderBinder() {
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val binding = LayoutCalendarLegendHeaderBinding.bind(view)
        }
        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.M)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                val legendLayout = container.binding.legendLayout
                if (legendLayout.tag == null) {
                    legendLayout.tag = month.yearMonth
                    legendLayout.children.map { it as TextView }
                        .forEachIndexed { index, tv ->
                            tv.text = daysOfWeek[index].getDisplayName(
                                TextStyle.SHORT,
                                Locale("id", "ID")
                            ).toString()
                            if (index == 6) {
                                tv.text = "Ahad"
                                tv.setBackgroundColor(getColor(R.color.colorPrimaryred))
                            }
                        }
                }
            }
        }
    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = DayOfWeek.MONDAY
        var daysOfWeek = DayOfWeek.values()
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    private fun onMonthScrollListener() {
        // FIX 1: Loop populate puasaEvent DIHAPUS dari sini
        // Sudah dipindah ke onCreate agar hanya dijalankan sekali

        binding.calendarView.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            binding.exMonthYearText.text = title
            this.monthSelected = month.month
            this.yearSelectDate = month.yearMonth.year
            val startDateHijriah = convertHijriah(getFirstdateOfTheMonth(month.month))
            val lastDateHijriah = convertHijriah(getLastdateOfTheMonth(month.month))
            binding.tvMonthHijri.text = "$startDateHijriah - $lastDateHijriah"
            // Tambah property di class
            val nowYear = LocalDate.now().year

            // Di monthScrollListener:
            tanggalJson?.let { list ->
                val yearOffset = (month.yearMonth.year - (nowYear - 1)) * 12
                val index = yearOffset + (month.month - 1)
                if (index in list.indices) {
                    adapter.updateMonthLegend(list[index])
                }
            }
        }

        binding.exNextMonthImage.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.scrollToMonth(it.yearMonth.next)
            }
        }

        binding.exPreviousMonthImage.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.scrollToMonth(it.yearMonth.previous)
            }
        }
    }

    private fun convertDateGMT(date: Long): Long {
        val newDate = Calendar.getInstance()
        newDate.timeInMillis = date.startDateMillis()
        return newDate.convertCalendarFormatGMT7().timeInMillis.millisToSeconds()
    }

    private fun Calendar.convertCalendarFormatGMT7(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("GMT+07:00")
        calendar.set(Calendar.YEAR, this.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, this.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, this.get(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    private fun Long.millisToSeconds(): Long {
        return this / 1000L
    }

    private fun getDateByCalendar(calendar: Calendar): String {
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

    private fun Calendar.convertCalendarFormatUTC(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        calendar.set(Calendar.YEAR, this.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, this.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, this.get(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    fun convertLocalTimeToLong(date: String): Long {
        val l = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
    }

    // FIX 2: Wrapper cached — cukup hit cache untuk tanggal yang sudah pernah di-render
    fun convertToArabicNumberCached(date: LocalDate): String {
        return arabicNumberCache.getOrPut(date) {
            convertToArabicNumber(date)
        }
    }

    fun convertToArabicNumber(date: LocalDate): String {
        val gregorianCalendar = GregorianCalendar(date.year, date.monthValue - 1, date.dayOfMonth)
        val cal = UmmalquraCalendar()
        val dateFormat = SimpleDateFormat("", Locale.ENGLISH)

        cal.time = gregorianCalendar.time
        cal.get(Calendar.YEAR)
        cal.get(Calendar.MONTH)
        cal.get(Calendar.DAY_OF_MONTH)
        dateFormat.calendar = cal
        dateFormat.applyPattern("d MMMM, y")
        dateFormat.format(cal.time)
        val nf: NumberFormat = NumberFormat.getInstance(Locale("ar", "EG"))

        return nf.format(cal.get(Calendar.DAY_OF_MONTH))
    }

    fun convertHijriah(date: Calendar): String {
        val gregorianCalendar = GregorianCalendar(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH)
        )
        val cal = UmmalquraCalendar()
        val dateFormat = SimpleDateFormat("", Locale.ENGLISH)

        cal.time = gregorianCalendar.time
        cal.get(Calendar.YEAR)
        cal.get(Calendar.MONTH)
        cal.get(Calendar.DAY_OF_MONTH)
        dateFormat.calendar = cal
        dateFormat.applyPattern("d MMMM, y")
        dateFormat.format(cal.time)
        return dateFormat.format(cal.time)
    }

    private fun getFirstdateOfTheMonth(month: Int = 0): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (yearSelectDate != 0) {
            calendar.set(Calendar.YEAR, yearSelectDate)
        }
        return calendar.convertCalendarFormatUTC()
    }

    private fun getLastdateOfTheMonth(month: Int = 0): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (yearSelectDate != 0) {
            calendar.set(Calendar.YEAR, yearSelectDate)
        }
        return calendar.convertCalendarFormatUTC()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun openAppOnPlayStore(ctx: Context, package_name: String?) {
        var package_name = package_name
        if (package_name == null) {
            package_name = ctx.packageName
        }
        val uri = Uri.parse("market://details?id=$package_name")
        openURI(ctx, uri, "Play Store not found in your device")
    }

    private fun openURI(
        ctx: Context,
        uri: Uri?,
        error_msg: String?
    ) {
        val i = Intent(Intent.ACTION_VIEW, uri)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        if (ctx.packageManager.queryIntentActivities(i, 0).size > 0) {
            ctx.startActivity(i)
        } else if (error_msg != null) {
            Toast.makeText(this, error_msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLegendClick(code: Int) {
        gotoDetail(code)
    }

    private fun gotoDetail(code: Int) {
        val intent = Intent(this, DetailPuasaActivity::class.java)
        intent.putExtra("code", code)
        startActivity(intent)
    }
}


