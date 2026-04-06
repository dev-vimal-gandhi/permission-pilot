package com.servalabs.perms.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import com.servalabs.perms.common.compose.LucideRadar
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.servalabs.perms.R
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.navigation.NavigationController
import com.servalabs.perms.common.navigation.NavigationEntry

@Composable
fun MainScreen(
    backStack: NavBackStack<NavKey>,
    navCtrl: NavigationController,
    navigationEntries: Set<NavigationEntry>,
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    val currentEntry = backStack.lastOrNull()
    val isTabScreen = currentEntry is Nav.Tab

    Column(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            modifier = Modifier
                .weight(1f)
                .consumeWindowInsets(WindowInsets.navigationBars),
            backStack = backStack,
            onBack = {
                if (!navCtrl.up()) {
                    activity?.finish()
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                navigationEntries.forEach { entry ->
                    entry.apply { setup() }
                }
            },
        )

        if (isTabScreen) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
            ) {
                NavigationBarItem(
                    selected = currentEntry is Nav.Tab.Overview,
                    onClick = { navCtrl.replace(Nav.Tab.Overview) },
                    icon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null) },
                    label = { Text(stringResource(R.string.overview_page_label)) },
                )
                NavigationBarItem(
                    selected = currentEntry is Nav.Tab.Apps,
                    onClick = { navCtrl.replace(Nav.Tab.Apps) },
                    icon = { Icon(Icons.Filled.Apps, contentDescription = null) },
                    label = { Text(stringResource(R.string.apps_page_label)) },
                )
                NavigationBarItem(
                    selected = currentEntry is Nav.Tab.Permissions,
                    onClick = { navCtrl.replace(Nav.Tab.Permissions) },
                    icon = { Icon(Icons.Filled.Security, contentDescription = null) },
                    label = { Text(stringResource(R.string.permissions_page_label)) },
                )
            }
        }
    }
}
