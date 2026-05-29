package com.egorroman.hopfield.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.egorroman.hopfield.R
import com.egorroman.hopfield.domain.NetworkConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var gridContentSizePx by remember { mutableStateOf(IntSize.Zero) }
    val sheetState = rememberModalBottomSheetState()
    val settingsSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(MainIntent.ShowSettingsSheet(true)) }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.btn_settings)
                        )
                    }
                    if (state.gridState.any { it == NetworkConfig.STATE_ACTIVE }) {
                        IconButton(onClick = { viewModel.onIntent(MainIntent.ClearGrid) }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.btn_clear_grid),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (state.learnedCount > 0) {
                        IconButton(onClick = {
                            viewModel.onIntent(MainIntent.ShowPatternsSheet(true))
                        }) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = stringResource(R.string.btn_view_patterns)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround,
                ) {
                    StatusItem(
                        label = stringResource(
                            R.string.grid_size_format,
                            state.gridRows,
                            state.gridCols
                        ),
                        icon = Icons.Default.Grid4x4
                    )
                    StatusItem(
                        label = stringResource(
                            R.string.patterns_learned_format,
                            state.learnedCount
                        ),
                        icon = Icons.Default.Psychology
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Container with BoxWithConstraints for optimal fitting
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val gridAspectRatio = state.gridCols.toFloat() / state.gridRows
                val containerAspectRatio = maxWidth / maxHeight

                val (gridWidth, gridHeight) = if (gridAspectRatio > containerAspectRatio) {
                    maxWidth to (maxWidth / gridAspectRatio)
                } else {
                    (maxHeight * gridAspectRatio) to maxHeight
                }

                Box(
                    modifier = Modifier
                        .size(gridWidth, gridHeight)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp)
                        .onGloballyPositioned { gridContentSizePx = it.size }
                        .pointerInput(state.gridRows, state.gridCols) {
                            var initialActivate = true
                            val touchedIndices = mutableSetOf<Int>()

                            detectDragGestures(
                                onDragStart = { offset ->
                                    touchedIndices.clear()
                                    val index = getCellIndex(
                                        offset,
                                        gridContentSizePx,
                                        state.gridRows,
                                        state.gridCols
                                    )
                                    if (index != -1) {
                                        initialActivate =
                                            state.gridState[index] != NetworkConfig.STATE_ACTIVE
                                        touchedIndices.add(index)
                                        viewModel.onIntent(
                                            MainIntent.ToggleCells(
                                                setOf(index),
                                                initialActivate
                                            )
                                        )
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val index =
                                        getCellIndex(
                                            change.position,
                                            gridContentSizePx,
                                            state.gridRows,
                                            state.gridCols
                                        )
                                    if (index != -1 && index !in touchedIndices) {
                                        touchedIndices.add(index)
                                        viewModel.onIntent(
                                            MainIntent.ToggleCells(
                                                setOf(index),
                                                initialActivate
                                            )
                                        )
                                    }
                                },
                                onDragEnd = {
                                    touchedIndices.clear()
                                },
                                onDragCancel = {
                                    touchedIndices.clear()
                                }
                            )
                        }
                        .pointerInput(state.gridRows, state.gridCols) {
                            detectTapGestures { offset ->
                                val index = getCellIndex(
                                    offset,
                                    gridContentSizePx,
                                    state.gridRows,
                                    state.gridCols
                                )
                                if (index != -1) {
                                    viewModel.onIntent(MainIntent.ToggleCell(index))
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (r in 0 until state.gridRows) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (c in 0 until state.gridCols) {
                                    val index = r * state.gridCols + c
                                    val cellState =
                                        state.gridState.getOrElse(index) { NetworkConfig.STATE_INACTIVE }

                                    val color by animateColorAsState(
                                        targetValue = if (cellState == NetworkConfig.STATE_ACTIVE)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                                        label = "CellColor"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.isRecognized) {
                Text(
                    text = stringResource(R.string.pattern_recognized),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Control Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionTile(
                    text = stringResource(R.string.btn_learn),
                    icon = Icons.Default.Save,
                    onClick = { viewModel.onIntent(MainIntent.LearnPattern) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
                ActionTile(
                    text = stringResource(R.string.btn_recognize),
                    icon = Icons.Default.AutoMode,
                    onClick = { viewModel.onIntent(MainIntent.RecognizePattern) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    }

    // Modal Bottom Sheet for Viewing Patterns
    if (state.showPatternsSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onIntent(MainIntent.ShowPatternsSheet(false)) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.patterns_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.learnedPatterns) { pattern ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f
                                )
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .height(120.dp)
                                    .aspectRatio(state.gridCols.toFloat() / state.gridRows)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                MiniGrid(pattern, state.gridRows, state.gridCols)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.onIntent(MainIntent.ResetMemory) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_reset_memory))
                }
            }
        }
    }

    // Modal Bottom Sheet for Settings
    if (state.showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onIntent(MainIntent.ShowSettingsSheet(false)) },
            sheetState = settingsSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            SettingsSheetContent(
                currentRows = state.gridRows,
                currentCols = state.gridCols,
                onApply = { rows, cols ->
                    viewModel.onIntent(MainIntent.ChangeGridSize(rows, cols))
                }
            )
        }
    }
}

@Composable
fun SettingsSheetContent(
    currentRows: Int,
    currentCols: Int,
    onApply: (Int, Int) -> Unit
) {
    var rows by remember { mutableStateOf(currentRows.toFloat()) }
    var cols by remember { mutableStateOf(currentCols.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.rows_label, rows.toInt()),
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = rows,
            onValueChange = { rows = it },
            valueRange = NetworkConfig.MIN_ROWS.toFloat()..NetworkConfig.MAX_ROWS.toFloat(),
            steps = if (NetworkConfig.MAX_ROWS > NetworkConfig.MIN_ROWS) NetworkConfig.MAX_ROWS - NetworkConfig.MIN_ROWS - 1 else 0
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.cols_label, cols.toInt()),
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = cols,
            onValueChange = { cols = it },
            valueRange = NetworkConfig.MIN_COLS.toFloat()..NetworkConfig.MAX_COLS.toFloat(),
            steps = if (NetworkConfig.MAX_COLS > NetworkConfig.MIN_COLS) NetworkConfig.MAX_COLS - NetworkConfig.MIN_COLS - 1 else 0
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.warning_clear_memory),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onApply(rows.toInt(), cols.toInt()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.apply_changes))
        }
    }
}

@Composable
fun MiniGrid(pattern: List<Int>, gridRows: Int, gridCols: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        for (row in 0 until gridRows) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                for (col in 0 until gridCols) {
                    val index = row * gridCols + col
                    val isActive =
                        if (index in pattern.indices) pattern[index] == NetworkConfig.STATE_ACTIVE else false
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
    }
}

private fun getCellIndex(offset: Offset, gridSizePx: IntSize, gridRows: Int, gridCols: Int): Int {
    if (gridSizePx.width <= 0 || gridSizePx.height <= 0) return -1

    val cellWidth = gridSizePx.width.toFloat() / gridCols
    val cellHeight = gridSizePx.height.toFloat() / gridRows

    val column = (offset.x / cellWidth).toInt()
    val row = (offset.y / cellHeight).toInt()

    if (column in 0 until gridCols && row in 0 until gridRows) {
        return row * gridCols + column
    }
    return -1
}

@Composable
fun StatusItem(label: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ActionTile(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColorFor(containerColor)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.titleSmall)
    }
}
