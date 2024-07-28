package com.ys.datetimenotificationapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ys.datetimenotificationapp.ui.theme.DarkCyan
import com.ys.datetimenotificationapp.ui.theme.DateTimeNotificationAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DateTimeNotificationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    createNotificationChannel(LocalContext.current)
                    DateTime(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun DateTime(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        val currentCalendar = Calendar.getInstance()
        var timeButton by remember { mutableStateOf("Choose a time") }
        var dateButton by remember { mutableStateOf("Choose a date") }
        var hour by remember { mutableIntStateOf(currentCalendar.get(Calendar.HOUR_OF_DAY)) }
        var minute by remember { mutableIntStateOf(currentCalendar.get(Calendar.MINUTE)) }
        val context = LocalContext.current
        var dateInMillis by remember { mutableLongStateOf(0) }

        OutlinedButton(onClick = {
            showTimePickerDialog(context, hour, minute) { selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                timeButton = "$hour:${if (minute < 10) "0$minute" else "$minute"}"
            }
        }) {
            Text(text = timeButton, color = DarkCyan)
        }

        OutlinedButton(onClick = {
            val year = currentCalendar.get(Calendar.YEAR)
            val month = currentCalendar.get(Calendar.MONTH)
            val day = currentCalendar.get(Calendar.DAY_OF_MONTH)
            showDatePickerDialog(context, year, month, day) {
                dateInMillis = it
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                dateButton = dateFormatter.format(dateInMillis)
            }
        }) {
            Text(text = dateButton, color = DarkCyan)
        }

        OutlinedButton(onClick = {
            sendNotification(
                title = "Notification Scheduled",
                text = "Your notification is scheduled on $dateButton $timeButton",
                context = context
            )
            val timeInMillis = dateInMillis + minute * 60_000 + (hour) * 3_600_000
            scheduleNotification(context, timeInMillis)
        }) {
            Text(text = "Send notification", color = DarkCyan)
        }

    }
}

fun showTimePickerDialog(
    context: Context,
    initialHour: Int,
    initialMinute: Int,
    onTimeSet: (Int, Int) -> Unit
) {
    val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        onTimeSet(hourOfDay, minute)
    }
    val timePickerDialog =
        TimePickerDialog(context, timeSetListener, initialHour, initialMinute, true)
    timePickerDialog.show()
}

fun showDatePickerDialog(
    context: Context,
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onDateSet: (Long) -> Unit
) {
    val dateSetListener = OnDateSetListener { _, year, month, dayOfMonth ->
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        onDateSet(calendar.timeInMillis)
    }
    val datePickerDialog =
        DatePickerDialog(context, dateSetListener, initialYear, initialMonth, initialDay)
    datePickerDialog.show()
}

private fun createNotificationChannel(context: Context) {
    val name = "DateTime"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel("1", name, importance)
    channel.description = "DateTime Scheduled Notification"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}

fun sendNotification(title: String, text: String, context: Context) {
    val builder = NotificationCompat.Builder(context, "1")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)

    try {
        NotificationManagerCompat.from(context).notify(99, builder.build())
    } catch (e: SecurityException) {
        Log.d("trace", "Error $e")
    }
}

fun scheduleNotification(context: Context, timeInMillis: Long) {
    val i = Intent(context, NotificationReceiver::class.java)
    i.putExtra("title", "New Notification")
    i.putExtra("text", "Your notification has arrived successfully!")
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        200,
        i,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    try {
        manager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    } catch (e: SecurityException) {
        Log.d("trace", "Error $e")
    }
}


@Preview(showBackground = true)
@Composable
private fun DateTimePreview() {
    DateTime()
}