package com.lutech.calendarv2

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
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


    @SuppressLint("NewApi")
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

        // Update the month title
        updateMonthTitle(currentMonth)

        // Day View Container
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView: TextView = view.findViewById(R.id.calendarDayText)
        }

        // Bind data to day views
        calendarView.dayBinder =
            object : com.kizitonwose.calendar.view.MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)

                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    val currentDate = LocalDate.now()
                    val isToday = day.date == currentDate

                    container.textView.text = day.date.dayOfMonth.toString()

                    // Apply the rounded background programmatically
                    val backgroundDrawable = createRoundedBackground(
                        if (isToday) Color.parseColor("#149ffe") else Color.TRANSPARENT
                    )
                    container.textView.background = backgroundDrawable

                    // Set text color based on the condition
                    container.textView.setTextColor(
                        if (isToday) Color.WHITE else Color.BLACK
                    )
                    container.textView.visibility = View.VISIBLE
                }
            }

        toggleButton.setOnClickListener {
            isShowingWeek = !isShowingWeek
            if (isShowingWeek) {
                // Show only the current week and collapse the calendar
                val currentDate = LocalDate.now()
                val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
                val startOfWeek = currentDate.with(firstDayOfWeek)
                val endOfWeek = startOfWeek.plusDays(6)


                // Update calendar to show only the current week
                calendarView.setup(
                    YearMonth.from(startOfWeek),
                    YearMonth.from(startOfWeek),
                    firstDayOfWeek
                )
                calendarView.scrollToDate(startOfWeek)

                // Set day visibility to show only the current week
                calendarView.dayBinder =
                    object : com.kizitonwose.calendar.view.MonthDayBinder<DayViewContainer> {
                        override fun create(view: View) = DayViewContainer(view)
                        override fun bind(container: DayViewContainer, day: CalendarDay) {
                            val currentDate = LocalDate.now()
                            val isToday = day.date == currentDate
                            if (day.date.isAfter(startOfWeek.minusDays(1)) && day.date.isBefore(
                                    endOfWeek.plusDays(1)
                                )
                            ) {
                                container.textView.visibility = View.VISIBLE
                                container.textView.text = day.date.dayOfMonth.toString()
                                container.textView.setBackgroundColor(
                                    if (isToday) Color.parseColor("#149ffe") else Color.TRANSPARENT
                                )
                                container.textView.setTextColor(
                                    if (isToday) Color.WHITE else Color.BLACK
                                )
                            } else {
                                container.textView.visibility = View.GONE
                            }
                        }
                    }
            } else {
                // Show the full month
                calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
                calendarView.scrollToMonth(YearMonth.now())

                // Restore the full calendar view
                calendarView.dayBinder =
                    object : com.kizitonwose.calendar.view.MonthDayBinder<DayViewContainer> {
                        override fun create(view: View) = DayViewContainer(view)
                        override fun bind(container: DayViewContainer, day: CalendarDay) {
                            val currentDate = LocalDate.now()
                            val isToday = day.date == currentDate
                            container.textView.text = day.date.dayOfMonth.toString()
                            container.textView.setBackgroundColor(
                                if (isToday) Color.parseColor("#149ffe") else Color.TRANSPARENT
                            )
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

    @SuppressLint("NewApi")
    private fun updateMonthTitle(yearMonth: YearMonth) {
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val year = yearMonth.year
        monthTitle.text = "$month/$year"
    }

    private fun createRoundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(color)
        }
    }
}
