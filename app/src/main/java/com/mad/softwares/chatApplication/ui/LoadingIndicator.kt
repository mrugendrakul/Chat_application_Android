package com.mad.softwares.chatApplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(
    isLoading:Boolean,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center
){
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .size(30.dp)
                .alpha(0.5f)
                .background(color = Color.White)
                .clickable (enabled = false){ }
//                .background(alpha = 0.5f,brush = Brush.verticalGradient(colors = listOf(Color.White)))
//                .background(brush = Brush.verticalGradient(),alpha  = 0f)
            ,
            contentAlignment = contentAlignment
        ) {
            CircularProgressIndicator(
//                color = Color.Black, // You can set your desired color here
                strokeWidth = 5.dp ,// You can adjust the stroke width as needed,
            )
        }
    }
}

@Composable
fun LinearLoadingIndicator(
    isLoading:Boolean,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center
){
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .size(30.dp)
                .alpha(0.5f)
                .background(color = Color.White)
                .clickable (enabled = false){ }
//                .background(alpha = 0.5f,brush = Brush.verticalGradient(colors = listOf(Color.White)))
//                .background(brush = Brush.verticalGradient(),alpha  = 0f)
            ,
            contentAlignment = contentAlignment
        ) {
            LinearProgressIndicator(
//                isLoading = isLoading
//                color = Color.Black, // You can set your desired color here
//                strokeWidth = 5.dp ,// You can adjust the stroke width as needed,
            )
        }
    }
}

@Composable
@Preview
fun PreviewLiner(){
    LinearLoadingIndicator(isLoading = true)

}