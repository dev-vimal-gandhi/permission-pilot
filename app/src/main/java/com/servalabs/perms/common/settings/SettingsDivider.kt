package com.servalabs.perms.common.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.servalabs.perms.common.compose.Preview2
import com.servalabs.perms.common.compose.PreviewWrapper

@Composable
fun SettingsDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.padding(start = 72.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    )
}

@Preview2
@Composable
private fun SettingsDividerPreview() = PreviewWrapper {
    SettingsDivider()
}
