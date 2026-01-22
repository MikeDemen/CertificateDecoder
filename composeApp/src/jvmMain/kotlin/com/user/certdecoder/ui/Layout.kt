package com.user.certdecoder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.user.certdecoder.ui.components.FileBrowser
import com.user.certdecoder.ui.components.InputWindow

@Composable
fun MainLayout() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        FileBrowser(modifier = Modifier.padding(24.dp))

        Spacer(Modifier.weight(0.1f))

        InputWindow(modifier = Modifier)

        // Future: output area, buttons, status, etc.
        Spacer(Modifier.weight(0.5f)) // push content up if needed
    }
}



