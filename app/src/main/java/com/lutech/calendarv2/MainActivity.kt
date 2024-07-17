package com.lutech.calendarv2

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
import com.lutech.calendarv2.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var toggleButton: Button
    private lateinit var monthTitle: TextView
    private var isShowingWeek = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        toggleButton = findViewById(R.id.toggleButton)
        monthTitle = findViewById(R.id.monthTitle)

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        updateMonthTitle(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView: TextView = view.findViewById(R.id.calendarDayText)
        }

        calendarView.dayBinder =
            object : com.kizitonwose.calendar.view.MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)

                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    val currentDate = LocalDate.now()
                    val isToday = day.date == currentDate

                    container.textView.text = day.date.dayOfMonth.toString()
                    val backgroundDrawable = createRoundedBackground(
                        if (isToday) Color.parseColor("#149ffe") else Color.TRANSPARENT
                    )
                    container.textView.background = backgroundDrawable

                    container.textView.setTextColor(
                        if (isToday) Color.WHITE else Color.BLACK
                    )
                    container.textView.visibility = View.VISIBLE
                }
            }

        toggleButton.setOnClickListener {
            isShowingWeek = !isShowingWeek
            if (isShowingWeek) {
                val currentDate = LocalDate.now()
                val startOfWeek = currentDate.with(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
                val startOfMonth = currentDate.withDayOfMonth(1)

                // Calculate the visible range
                val endOfMonth = startOfWeek.minusDays(1)
                val startMonth = startOfMonth

                calendarView.setup(
                    YearMonth.from(startMonth),
                    YearMonth.from(endOfMonth),
                    WeekFields.of(Locale.getDefault()).firstDayOfWeek
                )
                calendarView.scrollToMonth(YearMonth.from(startMonth))

                calendarView.dayBinder =
                    object : com.kizitonwose.calendar.view.MonthDayBinder<DayViewContainer> {
                        override fun create(view: View) = DayViewContainer(view)

                        override fun bind(container: DayViewContainer, day: CalendarDay) {
                            val isToday = day.date == LocalDate.now()

                            if (day.date.isBefore(startMonth) || day.date.isAfter(endOfMonth)) {
                                container.textView.visibility = View.GONE
                            } else {
                                container.textView.visibility = View.VISIBLE
                                container.textView.text = day.date.dayOfMonth.toString()
                                val backgroundDrawable = createRoundedBackground(
                                    if (isToday) Color.parseColor("#149ffe") else Color.TRANSPARENT
                                )
                                container.textView.background = backgroundDrawable

                                container.textView.setTextColor(
                                    if (isToday) Color.WHITE else Color.BLACK
                                )
                            }
                        }
                    }
            } else {
                // Restore the full month view
                calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
                calendarView.scrollToMonth(YearMonth.now())

                calendarView.dayBinder =
                    object : com.kizitonwose.calendar.view.MonthDayBinder<DayViewContainer> {
                        override fun create(view: View) = DayViewContainer(view)

                        override fun bind(container: DayViewContainer, day: CalendarDay) {
                            val isToday = day.date == LocalDate.now()
                            container.textView.text = day.date.dayOfMonth.toString()
                            val backgroundDrawable = createRoundedBackground(
                                if (isToday) Color.parseColor("#149ffe") else Color.TRANSPARENT
                            )
                            container.textView.background = backgroundDrawable

                            container.textView.setTextColor(
                                if (isToday) Color.WHITE else Color.BLACK
                            )
                            container.textView.visibility = View.VISIBLE
                        }
                    }
            }
        }

        calendarView.monthScrollListener = {
            updateMonthTitle(it.yearMonth)
        }
    }

    private fun createRoundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(color)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMonthTitle(yearMonth: YearMonth) {
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val year = yearMonth.year
        monthTitle.text = "$month/$year"
    }
}
