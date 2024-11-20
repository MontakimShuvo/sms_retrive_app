package com.example.smsretrive

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.smsretrive.ui.theme.SmsRetriveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmsRetriveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SMSReaderScreen(
                        contentResolver = contentResolver
                    )
                }
            }
        }
    }

    @Composable
    fun SMSReaderScreen(contentResolver: ContentResolver) {
        val context = LocalContext.current
        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        var smsList by remember { mutableStateOf(emptyList<String>()) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasPermission = isGranted
            if (isGranted) {
                smsList = readSms(contentResolver)
            }
        }

        LaunchedEffect(Unit) {
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.READ_SMS)
            } else {
                smsList = readSms(contentResolver)
            }
        }

        if (hasPermission) {
            SMSListView(smsList = smsList)
        } else {
            Text(
                text = "Please grant SMS read permission to view messages.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Composable
    fun SMSListView(smsList: List<String>) {
        LazyColumn {
            items(smsList) { sms ->
                Text(
                    text = sms,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    fun readSms(contentResolver: ContentResolver): List<String> {
        val smsList = mutableListOf<String>()
        val cursor: Cursor? = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    smsList.add("Sender: $address\nMessage: $body")
                } while (it.moveToNext())
            }
        }
        return smsList
    }
}


