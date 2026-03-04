package com.example.tmaadu2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.i("LIFE", "onCreate")

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    AppNav()
                }
            }
        }
    }

    // Lifecycle logy (kvůli zadání)
    override fun onStart() { super.onStart(); Log.i("LIFE", "onStart") }
    override fun onResume() { super.onResume(); Log.i("LIFE", "onResume") }
    override fun onPause() { Log.i("LIFE", "onPause"); super.onPause() }
    override fun onStop() { Log.i("LIFE", "onStop"); super.onStop() }
    override fun onRestart() { super.onRestart(); Log.i("LIFE", "onRestart") }
    override fun onDestroy() { Log.i("LIFE", "onDestroy"); super.onDestroy() }
}


@Serializable object WelcomeRoute
@Serializable object FormRoute
@Serializable data class SummaryRoute(
    val name: String,
    val birth: String,
    val agreed: Boolean,
    val extra: String,
    val color: String
)


@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = WelcomeRoute) {

        composable<WelcomeRoute> { entry ->
            val agreed by entry.savedStateHandle
                .getStateFlow("agreed", false)
                .collectAsState()

            WelcomeScreen(
                agreed = agreed,
                onNext = { nav.navigate(FormRoute) }
            )
        }

        composable<FormRoute> {
            FormScreen(
                onDone = { name, birth, agreed, extra, color ->
                    nav.navigate(SummaryRoute(name, birth, agreed, extra, color))
                }
            )
        }

        composable<SummaryRoute> { entry ->
            val data = entry.toRoute<SummaryRoute>()

            SummaryScreen(
                data = data,
                onBackToWelcome = {
                    nav.getBackStackEntry(WelcomeRoute)
                        .savedStateHandle["agreed"] = data.agreed
                    nav.popBackStack(WelcomeRoute, inclusive = false)
                }
            )
        }
    }
}

@Composable
fun WelcomeScreen(agreed: Boolean, onNext: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Vítej!", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text(if (agreed) "Souhlas byl zaškrtnut" else "Souhlas zatím ne")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNext) { Text("Pokračovat") }
    }
}

@Composable
fun FormScreen(onDone: (String, String, Boolean, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var birth by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }
    var extra by remember { mutableStateOf("") }


    var color by remember { mutableStateOf("Červená") }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Formulář", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(name, { name = it }, label = { Text("Jméno") }, singleLine = true)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(birth, { birth = it }, label = { Text("Datum narození (YYYY-MM-DD)") }, singleLine = true)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(extra, { extra = it }, label = { Text("Extra údaj") }, singleLine = true)
        Spacer(Modifier.height(12.dp))

        Text("Barva:")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Červená", "Modrá", "Zelená", "Fialová").forEach { c ->
                OutlinedButton(
                    onClick = { color = c },
                    enabled = color != c
                ) { Text(c) }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(agreed, { agreed = it })
            Text("Souhlasím")
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onDone(name, birth, agreed, extra, color) },
            enabled = name.isNotBlank() && birth.isNotBlank()
        ) { Text("Dokončit") }
    }
}

@Composable
fun SummaryScreen(data: SummaryRoute, onBackToWelcome: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Souhrn", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Text("Jméno: ${data.name}")
        Text("Datum: ${data.birth}")
        Text("Souhlas: ${if (data.agreed) "ANO" else "NE"}")
        Text("Extra: ${data.extra}")
        Text("Barva: ${data.color}")

        Spacer(Modifier.height(24.dp))
        Button(onClick = onBackToWelcome) { Text("Zpět na první") }
    }
}