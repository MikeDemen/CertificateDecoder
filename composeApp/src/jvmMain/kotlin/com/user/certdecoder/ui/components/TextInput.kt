package com.user.certdecoder.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card


@Composable
fun InputWindow(modifier: Modifier = Modifier) {

    Card(modifier = Modifier
        .fillMaxWidth(0.6f)
        .height(200.dp)){

        TextField(
            value = "",
            onValueChange = { /* do nothing yet */ },
            label = { Text("Paste certificate here") },
            maxLines = Int.MAX_VALUE,           // allows unlimited lines
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        )
    }


}