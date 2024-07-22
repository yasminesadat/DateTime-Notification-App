@file:OptIn(ExperimentalMaterial3Api::class)

package com.ys.datetimenotificationapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
        var timeButton by remember { mutableStateOf("Choose a time") }
        var dateButton by remember { mutableStateOf("Choose a date") }
        var isTimePickerShown by remember { mutableStateOf(false) }
        var isDatePickerShown by remember { mutableStateOf(false) }
        var hour by remember { mutableIntStateOf(0) }
        var minute by remember { mutableIntStateOf(0) }
        val context = LocalContext.current
        var dateInMillis by remember { mutableLongStateOf(0) }

        if (isTimePickerShown) PickTime(onConfirm = {
            hour = it.hour
            minute = it.minute
            timeButton = "$hour:$minute"
            isTimePickerShown = false
        }, onDismiss = { isTimePickerShown = false })

        if (isDatePickerShown) PickDate(onConfirm = {

            val selectedDateMillis = it.selectedDateMillis ?: 0
            Log.d("trace","date from picker is $selectedDateMillis")

            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selectedDateMillis
            }

            // Create a Calendar instance for Africa/Cairo to convert to local time
            val localCalendar = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo")).apply {
                set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, utcCalendar.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, utcCalendar.get(Calendar.MINUTE))
                set(Calendar.SECOND, utcCalendar.get(Calendar.SECOND))
                set(Calendar.MILLISECOND, utcCalendar.get(Calendar.MILLISECOND))
            }

            dateInMillis = localCalendar.timeInMillis

            Log.d("trace","date after calendar is $dateInMillis")
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            dateButton = dateFormatter.format(localCalendar.time)
            isDatePickerShown = false
        }, onDismiss = { isDatePickerShown = false })

        OutlinedButton(onClick = { isTimePickerShown = true }) {
            Text(text = timeButton)
        }

        OutlinedButton(onClick = { isDatePickerShown = true }) {
            Text(text = dateButton)
        }

        OutlinedButton(onClick = {
            sendNotification(
                title = "Notification Scheduled",
                text = "Your notification is scheduled on $dateButton $timeButton",
                context = context
            )
            val timeInMillis = dateInMillis + minute * 60_000 + (hour) * 3_600_000
            Log.d("trace","$timeInMillis")
            scheduleNotification(context, timeInMillis)
        }) {
            Text(text = "Send notification")
        }

    }
}

@Composable
fun PickTime(onConfirm: (TimePickerState) -> Unit, onDismiss: () -> Unit) {
    val timePickerState = rememberTimePickerState(
        is24Hour = false
    )

    AlertDialog(onDismissRequest = { }, confirmButton = {
        TextButton(onClick = { onConfirm(timePickerState) }) {
            Text(text = "OK")
        }
    }, dismissButton = {
        TextButton(onClick = { onDismiss() }) {
            Text(text = "Cancel")
        }
    }, text = { TimePicker(state = timePickerState) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickDate(onConfirm: (DatePickerState) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    AlertDialog(onDismissRequest = {}, confirmButton = {
        TextButton(onClick = { onConfirm(datePickerState) }) {
            Text(text = "OK")
        }
    }, dismissButton = {
        TextButton(onClick = { onDismiss() }) {
            Text(text = "Cancel")
        }
    }, text = { DatePicker(state = datePickerState) })
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
    //Builder pattern : setter returns the object
    val builder = NotificationCompat.Builder(context, "1")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)

    try {
        NotificationManagerCompat.from(context).notify(99, builder.build())
    }
    catch(e: SecurityException){
        Log.d("trace","Error $e")
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