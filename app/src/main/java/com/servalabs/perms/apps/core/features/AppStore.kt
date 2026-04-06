package com.servalabs.perms.apps.core.features

import com.servalabs.perms.apps.core.Pkg

interface AppStore : Pkg {

    val urlGenerator: ((Pkg.Id) -> String)?
        get() = null
}