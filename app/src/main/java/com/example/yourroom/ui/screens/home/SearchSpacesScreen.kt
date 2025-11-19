package com.example.yourroom.ui.screens.home



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SearchSpacesScreen(
    navController: NavHostController
) {
    val focusRequester = remember { FocusRequester() }
  Box(
      modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .height(80.dp)

  ){

      IconButton(
          onClick = {navController.popBackStack() },
          modifier = Modifier.align(Alignment.CenterStart)
      ) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Volver",
              tint = Color.DarkGray
          )
      }
      OutlinedTextField(
          value = "",
          onValueChange = {},
          modifier =  Modifier
              .align(Alignment.CenterEnd)
              .fillMaxWidth(0.85f)
              .padding(end = 16.dp)
              .focusRequester(focusRequester),

          leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null)
          },
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
          enabled = true,
          colors = OutlinedTextFieldDefaults.colors(
              disabledContainerColor = Color.White,
              disabledBorderColor = Color.Gray,
              disabledLeadingIconColor = Color.DarkGray,
              disabledPlaceholderColor = Color.DarkGray
          )

      )
  }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
