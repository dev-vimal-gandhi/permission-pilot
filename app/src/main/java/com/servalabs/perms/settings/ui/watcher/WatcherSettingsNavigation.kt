package com.servalabs.perms.settings.ui.watcher

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.navigation.NavigationEntry
import javax.inject.Inject

class WatcherSettingsNavigation @Inject constructor() : NavigationEntry {
    override fun EntryProviderScope<NavKey>.setup() {
        entry<Nav.Settings.Watcher> { WatcherSettingsScreenHost() }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class Mod {
        @Binds
        @IntoSet
        abstract fun bind(entry: WatcherSettingsNavigation): NavigationEntry
    }
}
