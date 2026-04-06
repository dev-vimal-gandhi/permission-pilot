package com.servalabs.perms.apps.ui.manifest

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

class ManifestViewerNavigation @Inject constructor() : NavigationEntry {
    override fun EntryProviderScope<NavKey>.setup() {
        entry<Nav.Details.AppManifest> { key -> ManifestViewerScreenHost(route = key) }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class Mod {
        @Binds
        @IntoSet
        abstract fun bind(entry: ManifestViewerNavigation): NavigationEntry
    }
}
