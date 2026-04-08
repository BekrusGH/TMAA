package com.jiri.perspective

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import com.jiri.perspective.data.local.AppDatabase
import com.jiri.perspective.data.repository.SubscriptionRepository
import com.jiri.perspective.ui.screens.OverviewScreen
import com.jiri.perspective.ui.screens.SubscriptionListScreen
import com.jiri.perspective.ui.theme.PerspectiveTheme
import com.jiri.perspective.ui.viewmodel.OverviewViewModel
import com.jiri.perspective.ui.viewmodel.OverviewViewModelFactory
import com.jiri.perspective.ui.viewmodel.SubscriptionViewModel
import com.jiri.perspective.ui.viewmodel.SubscriptionViewModelFactory
import com.jiri.perspective.notifications.MonthlyReminderScheduler
import com.jiri.perspective.notifications.NotificationHelper

//Takový mozek celé operace, spustí apku při startu, nastaví notifikace, vytvoří databázi a repository
//viewmodel a přepíná mezi subscriptions a overview

private enum class AppScreen {  //interní typ pro přepínání obrazovek
    SUBSCRIPTIONS,
    OVERVIEW
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createMonthlyReminderChannel(this)   //vytvoříme notfikaci
        MonthlyReminderScheduler.scheduleNext(this) //naplánujeme další

        val database = AppDatabase.getDatabase(applicationContext)  //singleton databáze
        val repository = SubscriptionRepository(    //vytvoříme repository a předáme tomu dao
            database.subscriptionDao(),
            database.usageEntryDao()
        )

        val subscriptionViewModel = ViewModelProvider(  //vyvtoření viewmodelů
            this,
            SubscriptionViewModelFactory(repository)
        )[SubscriptionViewModel::class.java]

        val overviewViewModel = ViewModelProvider(
            this,
            OverviewViewModelFactory(repository)
        )[OverviewViewModel::class.java]

        setContent {
            PerspectiveTheme {  //máme vlastní theme
                var currentScreen by remember { mutableStateOf(AppScreen.SUBSCRIPTIONS) }   //tohleto drží akutální obrazovku, lokální compose stav

                Scaffold(       // Layout celé obrazovky
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.SUBSCRIPTIONS,
                                onClick = { currentScreen = AppScreen.SUBSCRIPTIONS },
                                label = { Text(stringResource(R.string.nav_subscriptions)) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.List,
                                        contentDescription = stringResource(R.string.nav_subscriptions_cd)
                                    )
                                }
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.OVERVIEW,
                                onClick = { currentScreen = AppScreen.OVERVIEW },
                                label = { Text(stringResource(R.string.nav_overview)) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Home,
                                        contentDescription = stringResource(R.string.nav_overview_cd)
                                    )
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.SUBSCRIPTIONS -> {
                            SubscriptionListScreen(
                                viewModel = subscriptionViewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.OVERVIEW -> {
                            OverviewScreen(
                                viewModel = overviewViewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}