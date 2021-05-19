package com.muslimApp.kalenderpuasasunnah

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
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.muslimApp.kalenderpuasasunnah.model.TanggalModel
import com.muslimApp.kalenderpuasasunnah.model.TanggalPuasa
import com.muslimApp.kalenderpuasasunnah.utils.AlarmReceiver
import com.muslimApp.kalenderpuasasunnah.utils.LegendAdapter
import com.muslimApp.kalenderpuasasunnah.utils.getJsonDataFromAsset
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.calendar_day_layout.view.*
import kotlinx.android.synthetic.main.layout_calendar_legend_header.view.*
import kotlinx.android.synthetic.main.layout_share.*
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.WeekFields
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val daysOfWeek = daysOfWeekFromLocale()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM", Locale("id", "ID"))
    private fun Long.startDateMillis(): Long = this * DateUtils.SECOND_IN_MILLIS
    private val puasaEvent: MutableList<TanggalPuasa> = mutableListOf()
    private var selectedDate: LocalDate? = null
    private var tanggalJson: List<TanggalModel>? = null
    private var monthList: MutableList<Int> = mutableListOf()
    private var alarmID: MutableList<Int> = mutableListOf(100, 101)
    private var monthSelected: Int = 0
    private var yearSelectDate = 0
    lateinit var adapter: LegendAdapter


    //    private var pendingIntent: PendingIntent? = null
    private val ALARM_REQUEST_CODE = 134

    //set interval notifikasi 10 detik
    private val interval_seconds = 10
    private val NOTIFICATION_ID = 1

    private val REQUEST_CODE = 100
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForUpdate()
        scMain.isNestedScrollingEnabled = false
        val jsonFileString = getJsonDataFromAsset(applicationContext, "puasa_2021.json")
        val gson = Gson()
        val tanggalType = object : TypeToken<List<TanggalModel>>() {}.type

        var tanggal: List<TanggalModel> = gson.fromJson(jsonFileString, tanggalType)
        this.tanggalJson = tanggal
        this.setUpList()
        this.setUpCalendar()
        btnShare.setOnClickListener {
            shareApp()
        }
//        stopAlarmManager()
//        runAlarm()
//        showDialogTimePrayerReminder()
        alarmID.forEach {
            alarmCihuy(it)
        }
    }

    private fun showDialogTimePrayerReminder() {
        val view = layoutInflater.inflate(R.layout.layout_time_prayer_reminder, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun alarmCihuy(id: Int) {
        // Quote in Morning at 08:32:00 AM
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("GMT+07:00")
        when (id) {
            100 -> {
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            101 -> {
                calendar.set(Calendar.HOUR_OF_DAY, 21)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }

        val cur = Calendar.getInstance()

        if (cur.after(calendar)) {
            calendar.add(Calendar.DATE, 1)
        }

        val myIntent = Intent(this, AlarmReceiver::class.java)
//        val ALARM1_ID = 10000
        myIntent.putExtra("id", id.toString())
        val pendingIntent = PendingIntent.getBroadcast(
            this, id, myIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager =
            this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun runAlarm() {
        val alarmIntent = Intent(this@MainActivity, AlarmReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(this@MainActivity, ALARM_REQUEST_CODE, alarmIntent, 0)

        //set waktu sekarang berdasarkan interval
        //set waktu sekarang berdasarkan interval

        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("GMT+07:00")
        calendar.set(Calendar.HOUR_OF_DAY, 11)
        calendar.set(Calendar.MINUTE, 2)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val manager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //set alarm manager dengan memasukkan waktu yang telah dikonversi menjadi milliseconds
        //set alarm manager dengan memasukkan waktu yang telah dikonversi menjadi milliseconds
        manager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingIntent
        Toast.makeText(this, "AlarmManager Start.", Toast.LENGTH_SHORT).show()
    }

    //Stop/Cancel alarm manager
    fun stopAlarmManager() {
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this@MainActivity, AlarmReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(this@MainActivity, ALARM_REQUEST_CODE, alarmIntent, 0)
        manager.cancel(pendingIntent)
        //close existing/current notifications
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        //jika app ini mempunyai banyak notifikasi bisa di cancelAll()
        //notificationManager.cancelAll();
        Toast.makeText(this, "AlarmManager Stopped by User.", Toast.LENGTH_SHORT).show()
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

    private fun setAlarm() {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("GMT+07:00")
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 20)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val intent1 = Intent(this@MainActivity, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this@MainActivity,
            0,
            intent1,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val am =
            this@MainActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun setUpList() {
        rvLegendLayout.layoutManager = LinearLayoutManager(this)
        adapter = LegendAdapter()
        rvLegendLayout.adapter = adapter
    }

    private fun setUpCalendar() {

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)
        onSetHeaderBinder()
        onMonthScrollListener()

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.calendarDayText
            val textArabicNumber = view.calendarArabicText
            val marker = view.dotMarker
        }

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
                container.textArabicNumber.text = convertToArabicNumber(day.date)
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

                    val isEvent = puasaEvent.find {
                        convertLocalTimeToLong(day.date.toString()) == convertDateGMT(it.tanggal)
                    }
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
//                        textView.setTextColor(
//                            ContextCompat.getColor(
//                                this@MainActivity,
//                                R.color.colorBlackImage
//                            )
//                        )
//                        textArabicNumber.setTextColor(
//                            ContextCompat.getColor(
//                                this@MainActivity,
//                                R.color.colorBlackImage
//                            )
//                        )
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                    textArabicNumber.visibility = View.INVISIBLE
                    container.marker.visibility = View.INVISIBLE
                }

            }
        }

//        calendarView.post {
//            puasaSyawal.forEach {
//                val date: LocalDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
//                selectedDate = date
//                selectedDate?.let { selected ->
//                    calendarView.notifyCalendarChanged()
//                }
//            }
//        }
    }

    private fun onSetHeaderBinder() {
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.legendLayout
        }
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.M)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }
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
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    private fun onMonthScrollListener() {
        this.tanggalJson?.forEach {
            it.tanggalPuasa.forEach {
                this.puasaEvent.add(it)
            }
        }
        calendarView.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            exMonthYearText.text = title
            this.monthSelected = month.month
            this.yearSelectDate = month.yearMonth.year
            var startDateHijriah = convertHijriah(getFirstdateOfTheMonth(month.month))
            var lastDateHijriah = convertHijriah(getLastdateOfTheMonth(month.month))
            tvMonthHijri.text = startDateHijriah + " - " + lastDateHijriah
            tanggalJson?.let {
                adapter.updateMonthLegend(it[month.month - 1])
            }
//            calendarView.notifyMonthChanged(month.yearMonth)
        }


        exNextMonthImage.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.scrollToMonth(it.yearMonth.next)
            }
        }

        exPreviousMonthImage.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.scrollToMonth(it.yearMonth.previous)
            }
        }
    }

    private fun convertDateGMT(date: Long): Long {
        var newDate = Calendar.getInstance()
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

    private fun convertDateUTC(date: Long): Long {
        var newDate = Calendar.getInstance()
        newDate.timeInMillis = date.startDateMillis()
        return newDate.convertCalendarFormatUTC().timeInMillis.millisToSeconds()
    }

    private fun getDateByTimestampUTC(orderDate: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = orderDate * DateUtils.SECOND_IN_MILLIS
        return getDateByCalendar(calendar.convertCalendarFormatUTC())
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

    fun convertToArabicNumber(date: LocalDate): String {
        val gregorianCalendar = GregorianCalendar(date.year, date.monthValue - 1, date.dayOfMonth)
        val cal = UmmalquraCalendar()
        val dateFormat = SimpleDateFormat("", Locale.ENGLISH)

        cal.time = gregorianCalendar.time
        cal.get(Calendar.YEAR)        // 1436
        cal.get(Calendar.MONTH)        // 5 <=> Jumada al-Akhirah
        cal.get(Calendar.DAY_OF_MONTH) // 14
        dateFormat.calendar = cal
        dateFormat.applyPattern("d MMMM, y");
        dateFormat.format(cal.time);
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
        cal.get(Calendar.YEAR)        // 1436
        cal.get(Calendar.MONTH)        // 5 <=> Jumada al-Akhirah
        cal.get(Calendar.DAY_OF_MONTH) // 14
        dateFormat.calendar = cal
        dateFormat.applyPattern("d MMMM, y")
        dateFormat.format(cal.time)
        return dateFormat.format(cal.time)
    }

    fun convertToLocalDate(number: Long): LocalDate {
        val date =
            Instant.ofEpochMilli(number)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        return date
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

    private fun checkForUpdate() {

        val appVersion: String = getAppVersion(this)
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetch(0)

        val minVersion =
            remoteConfig.getString("min_version_of_app")
        val currentVersion =
            remoteConfig.getString("latest_version_of_app")
        if (!TextUtils.isEmpty(minVersion) && !TextUtils.isEmpty(appVersion) && checkMandateVersionApplicable(
                getAppVersionWithoutAlphaNumeric(minVersion),
                getAppVersionWithoutAlphaNumeric(appVersion)
            )
        ) {
            onUpdateNeeded(true)
        } else if (!TextUtils.isEmpty(currentVersion) && !TextUtils.isEmpty(appVersion) && !TextUtils.equals(
                currentVersion,
                appVersion
            )
        ) {
            onUpdateNeeded(false)
        } else {
            moveForward()
        }
    }

    private fun checkMandateVersionApplicable(
        minVersion: String,
        appVersion: String
    ): Boolean {
        return try {
            val minVersionInt = minVersion.toInt()
            val appVersionInt = appVersion.toInt()
            minVersionInt > appVersionInt
        } catch (exp: NumberFormatException) {
            false
        }
    }

    private fun getAppVersion(context: Context): String {
        var result: String? = ""
        try {
            result = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("TAG", e.message)
        }
        return result?:""
    }

    private fun getAppVersionWithoutAlphaNumeric(result: String): String {
        var version_str = ""
        version_str = result.replace(".", "")
        return version_str
    }

    private fun onUpdateNeeded(isMandatoryUpdate: Boolean) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            .setTitle(getString(R.string.update_app))
            .setCancelable(false)
            .setMessage(if (isMandatoryUpdate) getString(R.string.dialog_update_available_message) else "A new version is found on Play store, please update for better usage.")
            .setPositiveButton(getString(R.string.update_now))
            { dialog, which ->
                openAppOnPlayStore(this, null)
            }

        if (!isMandatoryUpdate) {
            dialogBuilder.setNegativeButton(getString(R.string.later)) { dialog, which ->
                moveForward()
                dialog?.dismiss()
            }.create()
        }
        val dialog: AlertDialog = dialogBuilder.create()
        dialog.show()
    }

    private fun moveForward() {
//        Toast.makeText(this, "Next Page Intent", Toast.LENGTH_SHORT).show()
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

}


