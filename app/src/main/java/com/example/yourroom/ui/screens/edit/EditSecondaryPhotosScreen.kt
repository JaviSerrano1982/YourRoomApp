
package com.example.yourroom.ui.screens.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.yourroom.viewmodel.EditSecondaryPhotosViewModel
import com.example.yourroom.viewmodel.thumbnailModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSecondaryPhotosScreen(
    navController: NavController,
    spaceId: Long,
    vm: EditSecondaryPhotosViewModel = hiltViewModel()
) {
    LaunchedEffect(spaceId) { vm.init(spaceId) }
    val ui = vm.ui
    val scope = rememberCoroutineScope()
    var replaceIndex by remember { mutableStateOf<Int?>(null) }

    val addPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris: List<Uri> -> vm.addPhotos(uris) }

    val replacePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val idx = replaceIndex
        if (uri != null && idx != null) vm.replaceAt(idx, uri)
        replaceIndex = null
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar fotos de la sala", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        enabled = !ui.isSaving,
                        onClick = { navController.popBackStack() }
                    ) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        val delta = vm.buildDelta()

                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            // IDs a borrar: usa LongArray (Bundle-safe)
                            set("secondaryPhotosToDelete", delta.toDeleteIds.toLongArray())

                            // Nuevas imágenes: guarda como strings (Bundle-safe)
                            set("secondaryPhotosNewUris", ArrayList(delta.newUris.map { it.toString() }))
                        }

                        navController.popBackStack()
                    }
                ) {
                    Text("Listo")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (ui.cells.size < 10) {
                    item {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFE8F0FA))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
                                .clickable {
                                    addPhotosLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("Añadir fotos", style = MaterialTheme.typography.bodyMedium)
                                Text("Máximo 10", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }

                itemsIndexed(ui.cells) { index, cell ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
                            .clickable {
                                replaceIndex = index
                                replacePhotoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        AsyncImage(
                            model = cell.thumbnailModel,
                            contentDescription = "Foto $index",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { vm.removeAt(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar", tint = Color.Black)
                        }
                    }
                }
            }

            ui.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}
