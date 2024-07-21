@file:OptIn(ExperimentalMaterial3Api::class)

package com.ys.datetimenotificationapp

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ys.datetimenotificationapp.ui.theme.DateTimeNotificationAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DateTimeNotificationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DateTime(Modifier.padding(innerPadding) .fillMaxSize())
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
        var year by remember { mutableIntStateOf(0) }
        var month by remember { mutableIntStateOf(0) }
        var day by remember { mutableIntStateOf(0) }


        if (isTimePickerShown) PickTime(onConfirm = {
            hour = it.hour
            minute = it.minute
            timeButton = "$hour:$minute"
            isTimePickerShown = false
        }, onDismiss = { isTimePickerShown = false })

        if (isDatePickerShown) PickDate(onConfirm = {
            val c = Calendar.getInstance()
            c.timeInMillis = it.selectedDateMillis ?: 0
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            dateButton = dateFormatter.format(c.time)
            isDatePickerShown = false
        }, onDismiss = { isDatePickerShown = false })

        OutlinedButton(onClick = { isTimePickerShown = true }) {
            Text(text = timeButton)
        }

        OutlinedButton(onClick = { isDatePickerShown = true }) {
            Text(text = dateButton)
        }

        OutlinedButton(onClick = { }) {
            Text(text = "Send notification")
        }

    }
}

@Composable
fun PickTime(onConfirm: (TimePickerState) -> Unit, onDismiss: () -> Unit) {
    val timePickerState = rememberTimePickerState(
        is24Hour = true
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

@Preview(showBackground = true)
@Composable
private fun DateTimePreview() {
    DateTime()
}