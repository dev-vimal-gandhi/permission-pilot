package com.servalabs.perms.common.coil

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.servalabs.perms.common.BuildConfigWrap
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.Logging
import com.servalabs.perms.common.debug.logging.asLog
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import javax.inject.Provider
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CoilModule {

    @Provides
    fun imageLoader(
        @ApplicationContext context: Context,
        appIconFetcherFactory: AppIconFetcher.Factory,
        permissionIconFetcher: PermissionIconFetcher.Factory,
        usesPermissionIconFetcher: UsesPermissionIconFetcher.Factory,
        dispatcherProvider: DispatcherProvider,
    ): ImageLoader = ImageLoader.Builder(context).apply {

        if (BuildConfigWrap.DEBUG) {
            val logger = object : Logger {
                override var level: Int = Log.VERBOSE
                override fun log(tag: String, priority: Int, message: String?, throwable: Throwable?) {
                    val mappedPriority = Logging.Priority.fromAndroid(priority).let {
                        if (it <= Logging.Priority.INFO) Logging.Priority.VERBOSE else it
                    }
                    log("Coil:$tag", mappedPriority) { "$message ${throwable?.asLog()}" }
                }
            }
            logger(logger)
        }
        components {
            add(appIconFetcherFactory)
            add(permissionIconFetcher)
            add(usesPermissionIconFetcher)
        }
        dispatcher(dispatcherProvider.Default)
    }.build()

    @Singleton
    @Provides
    fun imageLoaderFactory(imageLoaderSource: Provider<ImageLoader>): ImageLoaderFactory = ImageLoaderFactory {
        log(TAG) { "Preparing imageloader factory" }
        imageLoaderSource.get()
    }

    companion object {
        private val TAG = logTag("Coil", "Module")
    }
}
