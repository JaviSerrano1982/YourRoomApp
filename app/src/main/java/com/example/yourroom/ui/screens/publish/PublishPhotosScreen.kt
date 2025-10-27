// PublishPhotosScreen.kt
package com.example.yourroom.ui.screens.publish

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.example.yourroom.R
import com.example.yourroom.viewmodel.PublishPhotosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishPhotosScreen(
    navController: NavController,
    spaceId: Long,
    vm: PublishPhotosViewModel = hiltViewModel()
) {
    val ui = vm.ui
    val scope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }
    var replaceIndex by remember { mutableStateOf<Int?>(null) }

    val max = 10
    val remaining = (max - ui.selected.size).coerceAtLeast(0)

    // SINGLE: cuando queda 1 hueco
    val addOnePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) vm.addPhotos(listOf(uri))
    }

    // MULTIPLE: cuando quedan 2 o más huecos
    val addMultiplePhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            remaining.coerceAtLeast(2)
        )
    ) { uris: List<Uri> ->
        vm.addPhotos(uris)
    }

    val replacePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val idx = replaceIndex
        if (uri != null && idx != null) vm.replaceAt(idx, uri)
        replaceIndex = null
    }

    // Contenido principal
    PublishPhotosContent(
        thumbnails = ui.selected,
        isSaving = ui.isSaving,
        error = ui.error,
        remaining = remaining,
        addOnePhotoLauncher = addOnePhotoLauncher,
        addMultiplePhotosLauncher = addMultiplePhotosLauncher,
        onClickBack = { navController.popBackStack() },
        onClickFinish = {
            scope.launch {
                val ok = vm.saveAllAwaitAndPublish()
                if (ok) {
                    navController.navigate("success_publish") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            }
        },
        onClickCancel = { showCancelDialog = true },
        onClickRemoveAt = vm::removeAt,
        onClickReplaceAt = { idx ->
            replaceIndex = idx
            replacePhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        isEnabledFinish = ui.selected.isNotEmpty() && !ui.isSaving
    )

    // Diálogo de cancelar publicación
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar publicación") },
            text = { Text("Se eliminará el borrador de esta sala. ¿Seguro que quieres cancelar y borrar?") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    vm.cancelAndDelete {
                        navController.navigate(PublishRoutes.home()) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                }) { Text("Sí, borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Seguir editando")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublishPhotosContent(
    thumbnails: List<Any>,
    isSaving: Boolean,
    error: String?,
    remaining: Int,
    addOnePhotoLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    addMultiplePhotosLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, List<Uri>>,
    onClickBack: () -> Unit,
    onClickFinish: () -> Unit,
    onClickCancel: () -> Unit,
    onClickRemoveAt: (Int) -> Unit,
    onClickReplaceAt: (Int) -> Unit,
    isEnabledFinish: Boolean
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sube las fotos de tu sala", fontSize = 18.sp, textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onClickCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar y borrar borrador")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onClickBack, enabled = !isSaving) {
                    Text("Atrás")
                }
                Button(
                    onClick = onClickFinish,
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabledFinish) Color(0xFFA5C6E2) else Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Finalizar")
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icono_photos),
                    contentDescription = "Ilustración de publicación",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(40.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Botón "Subir fotos"
                if (thumbnails.size < 10) {
                    item {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFE8F0FA))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
                                .clickable {
                                    if (remaining <= 0) return@clickable
                                    if (remaining == 1) {
                                        addOnePhotoLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } else {
                                        addMultiplePhotosLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("Subir fotos", style = MaterialTheme.typography.bodyMedium)
                                Text("Máximo 10", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // Miniaturas
                itemsIndexed(thumbnails) { index, model ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
                            .clickable { onClickReplaceAt(index) }
                    ) {
                        AsyncImage(
                            model = model,
                            contentDescription = "Foto $index",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onClickRemoveAt(index) },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Quitar", tint = Color.Red)
                        }
                    }
                }
            }

            error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}
