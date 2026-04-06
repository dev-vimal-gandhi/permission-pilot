package com.servalabs.perms.apps.ui.list

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

class AppsNavigation @Inject constructor() : NavigationEntry {
    override fun EntryProviderScope<NavKey>.setup() {
        entry<Nav.Tab.Apps> { AppsScreenHost() }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class Mod {
        @Binds
        @IntoSet
        abstract fun bind(entry: AppsNavigation): NavigationEntry
    }
}
