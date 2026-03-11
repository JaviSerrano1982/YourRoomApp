package com.example.yourroom.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.yourroom.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // -------- SECCIÓN SUPERIOR A TODO EL ANCHO --------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFF0FC))      // Color completo arriba
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Encuentra tu espacio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A1D37),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Un lugar pensado para ti\ny para tus prácticas saludables",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A1D37),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                )

                Spacer(modifier = Modifier.height(25.dp))

                SearchBox(
                    onClick = {
                        navController.navigate("search_spaces")
                    }
                )
            }
        }
        // -------- GRID DE 6 FOTOS --------

        PhotoGrid()
    }
}


@Composable
fun SearchBox(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = { Text("Empieza a buscar") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color.White,
                disabledBorderColor = Color.Gray,
                disabledLeadingIconColor = Color.DarkGray,
                disabledPlaceholderColor = Color.DarkGray
            )

        )
    }
}

@Composable
fun PhotoGrid() {

    val images = listOf(
        R.drawable.hotelgym,
        R.drawable.sala_espejos,
        R.drawable.sala_masajes,
        R.drawable.sala_artes_marciales,
        R.drawable.sala_multiusos,
        R.drawable.sala_fisioterapia,
    )

    val titles = listOf(
        "Gimnasio",
        "Sala con Espejos",
        "Sala de Masajes",
        "Artes Marciales",
        "Multiusos",
        "Fisioterapia"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .height(600.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = true
    ) {
        items(images.size) { index ->

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(top = if (index < 2) 16.dp else 0.dp)
            ) {
                Image(
                    painter = painterResource(id = images[index]),
                    contentDescription = titles[index],
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                bottomStart = 12.dp,
                                bottomEnd = 12.dp
                            )
                        )
                        .background(Color.Black.copy(alpha = 0.28f))
                        .padding(vertical = 8.dp, horizontal = 8.dp)

                ) {
                    Text(
                        text = titles[index],
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}



