package io.github.onlyashd.hukiawards.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.util.colors
import io.github.onlyashd.hukiawards.util.typography

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoriesManagementSubScreen(
    categories: List<Category>,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    onReorder: (List<String>) -> Unit
) {
    var list by remember(categories) { mutableStateOf(categories) }
    val state = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Gerenciar Categorias", style = typography().headlineSmall)

            val tooltipState = rememberTooltipState()
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text("Categorias definem os grupos de votação. Usuários verão as categorias na ordem definida aqui.")
                    }
                },
                state = tooltipState
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = colors().onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
        Text(
            "Arraste pelo ícone de menu para reordenar a prioridade visual",
            style = typography().bodySmall,
            color = colors().onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (list.isEmpty()) {
            Text(
                text = "Nenhuma categoria criada. Clique no botão + para adicionar.",
                style = typography().bodyMedium,
                color = colors().onSurfaceVariant
            )
        } else {
            LazyColumn(
                state = state,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(list, key = { _, cat -> cat.id!! }) { index, category ->
                    var isDragging by remember { mutableStateOf(false) }
                    var accumulatedDrag by remember { mutableStateOf(0f) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                scaleX = if (isDragging) 1.02f else 1.0f
                                scaleY = if (isDragging) 1.02f else 1.0f
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDragging) colors().surfaceVariant else colors().surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Reordenar",
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .pointerInput(category.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                isDragging = true
                                                accumulatedDrag = 0f
                                            },
                                            onDragEnd = {
                                                isDragging = false
                                                onReorder(list.mapNotNull { it.id })
                                            },
                                            onDragCancel = { isDragging = false },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                accumulatedDrag += dragAmount.y
                                                val dragThreshold = 50f

                                                val i = list.indexOfFirst { it.id == category.id }
                                                if (i != -1) {
                                                    if (accumulatedDrag > dragThreshold && i < list.size - 1) {
                                                        val newList = list.toMutableList()
                                                        val item = newList.removeAt(i)
                                                        newList.add(i + 1, item)
                                                        list = newList
                                                        accumulatedDrag = 0f
                                                    } else if (accumulatedDrag < -dragThreshold && i > 0) {
                                                        val newList = list.toMutableList()
                                                        val item = newList.removeAt(i)
                                                        newList.add(i - 1, item)
                                                        list = newList
                                                        accumulatedDrag = 0f
                                                    }
                                                }
                                            }
                                        )
                                    },
                                tint = colors().onSurfaceVariant
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, style = typography().bodyLarge)
                                if (category.description.isNotBlank()) {
                                    Text(
                                        text = category.description,
                                        style = typography().bodySmall,
                                        color = colors().onSurfaceVariant
                                    )
                                }
                            }

                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = { PlainTooltip { Text("Editar Categoria") } },
                                state = rememberTooltipState()
                            ) {
                                IconButton(onClick = { onEdit(category.id!!) }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = colors().secondary
                                    )
                                }
                            }

                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = { PlainTooltip { Text("Excluir Categoria") } },
                                state = rememberTooltipState()
                            ) {
                                IconButton(onClick = { onDelete(category.id!!) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = colors().error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
