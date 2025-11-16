package com.example.yourroom.ui.screens.home

import android.R.attr.fontWeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@Composable
fun HomeScreen(
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // -------- CARD PRINCIPAL ARRIBA DE LA PANTALLA --------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color(0xFFF3F4FF),
            elevation = 0.dp,

        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Encuentra tu espacio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A1D37),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center

                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Un lugar pensado para ti\n" +
                            "y para tus prácticas saludables",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))

                SearchBox()
            }
        }
    }
}

@Composable
fun SearchBox() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Empieza a buscar") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}
