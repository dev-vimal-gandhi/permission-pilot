package com.servalabs.perms.common.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.servalabs.perms.common.theming.ServaPermsTheme

@Composable
fun PreviewWrapper(content: @Composable () -> Unit) {
    ServaPermsTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
