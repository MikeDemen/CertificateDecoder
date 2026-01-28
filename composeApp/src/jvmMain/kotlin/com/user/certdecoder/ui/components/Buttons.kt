package com.user.certdecoder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.user.certdecoder.ui.utils.decodeCertificate

@Composable
fun FunctionButtons(
    onDecode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = Modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        DecodeButton(onDecode = onDecode)
        ValidateButton()
    }
}

@Composable
fun DecodeButton(
    onDecode: () -> Unit,
    modifier: Modifier = Modifier
){
    Button(onClick = onDecode,
        modifier = modifier
    ){
        Text("Decode")
    }
}

@Composable
fun ValidateButton(modifier: Modifier = Modifier) {
    Button(onClick = { /* Validate */ }) {
        Text("Validate")
    }
}