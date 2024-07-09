package com.example.androidlauncher

import AppListManager
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidlauncher.ui.theme.AndroidLauncherTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.example.androidlauncher.utils.hasPromptedForDefaultLauncher
import com.example.androidlauncher.utils.hasUsageStatsPermission
import com.example.androidlauncher.utils.isDefaultLauncher
import com.example.androidlauncher.utils.requestUsageStatsPermission
import com.example.androidlauncher.utils.setPromptedForDefaultLauncher
import com.example.androidlauncher.utils.showDefaultLauncherDialog
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var appListManager: AppListManager;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appListManager = AppListManager(this);

        lifecycleScope.launch {
            appListManager.loadApps()
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        registerReceiver(packageChangeReceiver, filter)

        if (!hasUsageStatsPermission(this)) {
            requestUsageStatsPermission(this)
        }
        if (!isDefaultLauncher(this) && !hasPromptedForDefaultLauncher(this)) {
            AlertDialog.Builder(this)
                .setTitle("Set as Default Launcher")
                .setMessage("Select NamasteScreen to boost your productivity by 5x !")
                .setPositiveButton("Yes") { _, _ ->
                    showDefaultLauncherDialog(this)
                    setPromptedForDefaultLauncher(this)
                }
                .setNegativeButton("No") { _, _ ->
                    setPromptedForDefaultLauncher(this)
                }
                .show()
        }


        setContent {
            AndroidLauncherTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(30.dp))
                        HomeScreenLauncher(context = this@MainActivity, appListManager = appListManager)
                    }

                }
            }
        }

    }


    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            lifecycleScope.launch {
                appListManager.refreshAppList()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageChangeReceiver)
    }

}

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable
)


fun Modifier.swipeGesture(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {}
): Modifier = this
    .then(
        Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                when {
                    dragAmount < -50 -> onSwipeLeft()
                    dragAmount > 50 -> onSwipeRight()
                }
            }
        }
    )
    .then(
        Modifier.pointerInput(Unit) {
            detectVerticalDragGestures { _, dragAmount ->
                when {
                    dragAmount < -50 -> onSwipeUp()
                    dragAmount > 50 -> onSwipeDown()
                }
            }
        }
    )

@Composable
fun RightSwipeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        Text(
            "Right Swipe Screen",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        // Add more content as needed
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onTaskAdded: (String) -> Unit) {
    var taskName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            TextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Task Name") }
            )
        },
        confirmButton = {
            Button(onClick = {
                if (taskName.isNotBlank()) {
                    onTaskAdded(taskName)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Function to save tasks to local storage
fun saveTasks(context: Context, tasks: List<String>) {
    val sharedPrefs = context.getSharedPreferences("TodoList", Context.MODE_PRIVATE)
    with(sharedPrefs.edit()) {
        putString("tasks", tasks.joinToString("|"))
        apply()
    }
}

// Function to load tasks from local storage
fun loadTasks(context: Context): List<String> {
    val sharedPrefs = context.getSharedPreferences("TodoList", Context.MODE_PRIVATE)
    val tasksString = sharedPrefs.getString("tasks", "") ?: ""
    return if (tasksString.isNotEmpty()) tasksString.split("|") else emptyList()
}

@Composable
fun HomeScreenLauncher(context: Context, appListManager: AppListManager) {

    var currentScreen by remember { mutableStateOf(0) }
    var isAppListVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var tasks by remember { mutableStateOf(loadTasks(context)) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .swipeGesture(
                    onSwipeUp = { if (!isAppListVisible) isAppListVisible = true },
                    onSwipeDown = { if (isAppListVisible) isAppListVisible = false }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .swipeGesture(
                            onSwipeLeft = { if (currentScreen == 0) currentScreen = 1 },
                            onSwipeRight = { if (currentScreen == 1) currentScreen = 0 }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(
                                x = if (currentScreen == 0) 0.dp else (-200).dp.times(
                                    currentScreen
                                )
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CurrentTimeDisplay(context = context)


                            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                            Box(
                                modifier = Modifier
                                    .width(screenWidth * 0.85f)
                                    .height(screenHeight * 0.8f)
                                    .weight(0.8f) // 80% of remaining space
                                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(5.dp))
                                    .padding(8.dp)


                            ) {
                                Column {

                                    if (tasks.isNotEmpty()) {
                                        Text(
                                            "Keep going !",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    } else {
                                        Text(
                                            "No tasks due",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        items(tasks) { task ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = task,
                                                    color = Color.White,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        tasks = tasks - task
                                                        saveTasks(context, tasks)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Delete Task",
                                                        tint = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = { showAddTaskDialog = true },
                                        modifier = Modifier.align(Alignment.End),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Task",
                                            tint = Color.Black
                                        )
                                    }
                                }
                            }

                        }
                    }



                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = if (currentScreen == 1) 0.dp else 200.dp.times(1 - currentScreen))
                    ) {
                        RightSwipeScreen()
                    }
                }
            }




            Spacer(modifier = Modifier.height(screenHeight * 0.05f))
            // "All Apps" text with icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.DarkGray.copy(alpha = 0.5f))
                    .clickable { isAppListVisible = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "All Apps",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All Apps",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Full-screen app list overlay with custom gesture detection
        if (isAppListVisible) {
            var offsetY by remember { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .offset(y = offsetY.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (offsetY > 100) {
                                        isAppListVisible = false
                                    }
                                    offsetY = 0f
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetY = 0f
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (dragAmount.y > 0) {
                                    offsetY += dragAmount.y
                                }
                            }
                        )
                    }
            ) {
                AppListWithSearch(context, appListManager = appListManager)
            }
        }


        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onTaskAdded = { newTask ->
                    tasks = tasks + newTask
                    saveTasks(context, tasks)
                    showAddTaskDialog = false
                }
            )
        }



    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListWithSearch(context: Context, appListManager: AppListManager) {

    val apps by appListManager.apps
    var searchQuery by remember { mutableStateOf("") }


    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

//    var searchQuery by remember { mutableStateOf("") }
//    val apps = remember {
//        val intent = Intent(Intent.ACTION_MAIN, null).apply {
//            addCategory(Intent.CATEGORY_LAUNCHER)
//        }
//        val packageManager = context.packageManager
//        val allApps = packageManager.queryIntentActivities(intent, 0)
//        allApps.map { ri ->
//            AppInfo(
//                label = ri.loadLabel(packageManager).toString(),
//                packageName = ri.activityInfo.packageName,
//                icon = ri.activityInfo.loadIcon(packageManager)
//            )
//        }.sortedBy { it.label.lowercase() }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Search bar
        Spacer(modifier = Modifier.height(screenHeight * 0.05f))
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Black)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(20.dp)
                )
            ,
            placeholder = { Text("Search apps") },

            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Black,
                textColor = Color.White,
                cursorColor = Color.White,
                placeholderColor = Color.Gray,

                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,

            )
        )

        // Filtered app list
        val filteredApps = apps.filter {
            it.label.contains(searchQuery, ignoreCase = true)
        }

        LazyColumn {
            val groupedApps = filteredApps.groupBy { it.label.first().uppercase() }
            groupedApps.forEach { (letter, appsInGroup) ->
                item {
                    Text(
                        text = letter,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp, 8.dp)
                    )
                }
                items(appsInGroup) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val packageManager: PackageManager = context.packageManager
                                try {
                                    val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (launchIntent != null) {
                                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(launchIntent)
                                    } else {
                                        // Handle the case where the app doesn't have a launch intent
                                        Toast.makeText(context, "Unable to launch the app", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    // Handle any exceptions (e.g., app not installed)
                                    Toast.makeText(context, "Error launching the app: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp, 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon.toBitmap().asImageBitmap(),
                            contentDescription = "App icon",
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = app.label,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CurrentTimeDisplay(context: Context) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var screenTime by remember { mutableStateOf("00:00") }

    fun getScreenTime(): String {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            cal.timeInMillis,
            System.currentTimeMillis()
        )
        val totalTimeInForeground = queryUsageStats.sumOf { it.totalTimeInForeground }
        val hours = totalTimeInForeground / (1000 * 60 * 60)
        val minutes = (totalTimeInForeground % (1000 * 60 * 60)) / (1000 * 60)
        return String.format("%02d:%02d", hours, minutes)
    }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            currentTime = timeFormat.format(now.time)
            currentDate = dateFormat.format(now.time)
            screenTime = getScreenTime()
            println("SCREEN TIME ${screenTime}")
            delay(60000) // Update every minute
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time in big size
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        // Date in small font and greyish color
        Text(
            text = currentDate,
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Screen time with border radius
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Screen time: $screenTime",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}