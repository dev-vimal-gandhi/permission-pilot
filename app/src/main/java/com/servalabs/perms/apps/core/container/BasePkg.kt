package com.servalabs.perms.apps.core.container

import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.apps.core.features.Installed
import com.servalabs.perms.apps.core.features.ReadableApk

sealed class BasePkg : Pkg, ReadableApk, Installed

fun BasePkg.isOrHasProfiles() = twins.isNotEmpty() || this is SecondaryProfilePkg