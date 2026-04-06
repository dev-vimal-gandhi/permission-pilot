package com.servalabs.perms.settings.ui.index

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.ColorLens
import androidx.compose.material.icons.twotone.FormatListNumbered
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material.icons.twotone.Notifications
import androidx.compose.material.icons.automirrored.twotone.HelpOutline
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.servalabs.perms.R
import com.servalabs.perms.common.BuildConfigWrap
import com.servalabs.perms.common.compose.Preview2
import com.servalabs.perms.common.compose.PreviewWrapper
import com.servalabs.perms.common.navigation.LocalNavigationController
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.settings.SettingsBaseItem
import com.servalabs.perms.common.settings.SettingsCategoryHeader
import com.servalabs.perms.common.settings.SettingsDivider

@Composable
fun SettingsIndexScreenHost() {
    val navCtrl = LocalNavigationController.current
    val vm: SettingsIndexViewModel = hiltViewModel()

    SettingsIndexScreen(
        onBack = { navCtrl?.up() },
        onGeneralSettings = { navCtrl?.goTo(Nav.Settings.General) },
        onWatcherSettings = { navCtrl?.goTo(Nav.Settings.Watcher) },
        onChangelog = { vm.openChangelog() },
        onSupport = { navCtrl?.goTo(Nav.Settings.Support) },
        onAcknowledgements = { navCtrl?.goTo(Nav.Settings.Acknowledgements) },
        onPrivacyPolicy = { vm.openPrivacyPolicy() },
    )
}

@Composable
fun SettingsIndexScreen(
    onBack: () -> Unit,
    onGeneralSettings: () -> Unit = {},
    onWatcherSettings: () -> Unit = {},
    onChangelog: () -> Unit,
    onSupport: () -> Unit,
    onAcknowledgements: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    versionSubtitle: String = BuildConfigWrap.VERSION_DESCRIPTION,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_page_label)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // SettingsBaseItem(
            //     title = stringResource(R.string.general_settings_label),
            //     subtitle = stringResource(R.string.general_settings_desc),
            //     icon = Icons.TwoTone.ColorLens,
            //     onClick = onGeneralSettings,
            // )
            // SettingsDivider()
            SettingsBaseItem(
                title = stringResource(R.string.watcher_settings_label),
                subtitle = stringResource(R.string.watcher_settings_desc),
                icon = Icons.TwoTone.Notifications,
                onClick = onWatcherSettings,
            )

            // SettingsCategoryHeader(text = stringResource(R.string.settings_category_other_label))

            // SettingsBaseItem(
            //     title = stringResource(R.string.settings_support_label),
            //     subtitle = "\u00AF\\_(ツ)_/\u00AF",
            //     icon = Icons.AutoMirrored.TwoTone.HelpOutline,
            //     onClick = onSupport,
            // )
            // SettingsDivider()
            // SettingsBaseItem(
            //     title = stringResource(R.string.changelog_label),
            //     subtitle = versionSubtitle,
            //     icon = Icons.TwoTone.FormatListNumbered,
            //     onClick = onChangelog,
            // )
            // SettingsDivider()
            // SettingsBaseItem(
            //     title = stringResource(R.string.settings_acknowledgements_label),
            //     subtitle = stringResource(R.string.general_thank_you_label),
            //     icon = Icons.TwoTone.Favorite,
            //     onClick = onAcknowledgements,
            // )
            // SettingsDivider()
            // SettingsBaseItem(
            //     title = stringResource(R.string.settings_privacy_policy_label),
            //     subtitle = stringResource(R.string.settings_privacy_policy_desc),
            //     icon = Icons.TwoTone.Info,
            //     onClick = onPrivacyPolicy,
            // )
        }
    }
}

@Preview2
@Composable
private fun SettingsIndexScreenPreview() = PreviewWrapper {
    SettingsIndexScreen(onBack = {}, onGeneralSettings = {}, onWatcherSettings = {}, onChangelog = {}, onSupport = {}, onAcknowledgements = {}, onPrivacyPolicy = {})
}
