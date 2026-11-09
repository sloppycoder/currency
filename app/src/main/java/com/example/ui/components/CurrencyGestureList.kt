package com.example.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.model.CurrencyItem
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CurrencyGestureList(
    currencies: List<CurrencyItem>,
    isEditMode: Boolean,
    onMove: (String, Int) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (CurrencyItem, @Composable () -> Unit) -> Unit
) {
    val colors = AppTheme.colors
    val listState = rememberLazyListState()
    val currentMove by rememberUpdatedState(onMove)
    val editing by rememberUpdatedState(isEditMode)
    var draggingCode by remember { mutableStateOf<String?>(null) }
    var dragTop by remember { mutableFloatStateOf(0f) }
    var dragHeight by remember { mutableIntStateOf(0) }
    var openedCode by remember { mutableStateOf<String?>(null) }
    val edgeSize = with(LocalDensity.current) { 64.dp.toPx() }
    val maxScrollPerFrame = with(LocalDensity.current) { 12.dp.toPx() }

    fun startDrag(code: String) {
        if (!editing) return
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == code } ?: return
        openedCode = null
        draggingCode = code
        dragTop = item.offset.toFloat()
        dragHeight = item.size
    }

    fun moveOverItem() {
        if (!editing) return
        val code = draggingCode ?: return
        val visible = listState.layoutInfo.visibleItemsInfo
        val dragged = visible.firstOrNull { it.key == code } ?: return
        val center = (dragTop + dragHeight / 2f).coerceIn(
            visible.first().offset.toFloat(),
            (visible.last().offset + visible.last().size - 1).toFloat()
        )
        val target = visible.firstOrNull {
            it.key != code && center >= it.offset && center < it.offset + it.size &&
                ((it.index > dragged.index && center > it.offset + it.size / 2f) ||
                    (it.index < dragged.index && center < it.offset + it.size / 2f))
        } ?: return
        currentMove(code, target.index)
    }

    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            draggingCode = null
            openedCode = null
        }
    }

    // Keep scrolling while a held row is near either edge, even when the finger stops moving.
    LaunchedEffect(draggingCode, isEditMode) {
        if (!isEditMode || draggingCode == null) return@LaunchedEffect
        while (true) {
            withFrameNanos { }
            val layout = listState.layoutInfo
            val scroll = when {
                dragTop < layout.viewportStartOffset + edgeSize ->
                    -maxScrollPerFrame * ((layout.viewportStartOffset + edgeSize - dragTop) / edgeSize).coerceIn(0f, 1f)
                dragTop + dragHeight > layout.viewportEndOffset - edgeSize ->
                    maxScrollPerFrame * ((dragTop + dragHeight - layout.viewportEndOffset + edgeSize) / edgeSize).coerceIn(0f, 1f)
                else -> 0f
            }
            if (scroll != 0f) listState.scrollBy(scroll)
            moveOverItem()
        }
    }

    LazyColumn(
        state = listState,
        userScrollEnabled = draggingCode == null,
        modifier = modifier.fillMaxWidth().testTag("currency_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(currencies, key = { it.code }) { item ->
            val dragging = draggingCode == item.code
            val position = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == item.code }?.offset ?: 0
            val index = currencies.indexOf(item)
            val actions = buildList {
                if (!isEditMode) return@buildList
                if (index > 0) add(CustomAccessibilityAction("Move up") { currentMove(item.code, index - 1); true })
                if (index < currencies.lastIndex) add(CustomAccessibilityAction("Move down") { currentMove(item.code, index + 1); true })
                if (currencies.size > 1) add(CustomAccessibilityAction("Delete") { onDelete(item.code); true })
            }
            Box(
                Modifier
                    .zIndex(if (dragging) 1f else 0f)
                    .offset { IntOffset(0, if (dragging) (dragTop - position).roundToInt() else 0) }
                    .animateItem(placementSpec = if (dragging) null else androidx.compose.animation.core.spring())
                    .shadow(if (dragging) 8.dp else 0.dp)
                    .background(colors.background)
                    .semantics { customActions = actions }
                    .pointerInput(item.code, isEditMode) {
                        if (!isEditMode) return@pointerInput
                        detectDragGesturesAfterLongPress(
                            onDragStart = { startDrag(item.code) },
                            onDragEnd = { draggingCode = null },
                            onDragCancel = { draggingCode = null },
                            onDrag = { change, amount ->
                                change.consume()
                                dragTop += amount.y
                                moveOverItem()
                            }
                        )
                    }
            ) {
                SwipeCurrencyRow(
                    code = item.code,
                    canDelete = isEditMode && currencies.size > 1,
                    enabled = isEditMode && draggingCode == null,
                    isOpen = openedCode == item.code,
                    onOpen = { openedCode = item.code },
                    onDelete = { onDelete(item.code) }
                ) {
                    Column {
                        content(item) {
                            if (isEditMode) Icon(
                                Icons.Default.DragHandle,
                                contentDescription = "Drag to reorder ${item.code}",
                                tint = colors.textSecondary,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("drag_handle_${item.code}")
                                    .draggable(
                                        orientation = Orientation.Vertical,
                                        state = rememberDraggableState { delta ->
                                            dragTop += delta
                                            moveOverItem()
                                        },
                                        onDragStarted = { startDrag(item.code) },
                                        onDragStopped = { draggingCode = null }
                                    )
                            )
                        }
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeCurrencyRow(
    code: String,
    canDelete: Boolean,
    enabled: Boolean,
    isOpen: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    var offset by remember { mutableFloatStateOf(0f) }
    var width by remember { mutableIntStateOf(0) }
    val revealWidth = with(LocalDensity.current) { 88.dp.toPx() }
    val scope = rememberCoroutineScope()
    var settling by remember { mutableStateOf<Job?>(null) }
    val currentDelete by rememberUpdatedState(onDelete)
    val deletionEnabled by rememberUpdatedState(enabled && canDelete)

    LaunchedEffect(isOpen, enabled, canDelete) {
        if (!isOpen || !enabled || !canDelete) {
            settling?.cancel()
            animate(offset, 0f) { value, _ -> offset = value }
        }
    }

    Box(Modifier.fillMaxWidth().clipToBounds().onSizeChanged { width = it.width }) {
        if (offset < 0f && canDelete) {
            Box(Modifier.matchParentSize().background(Color(0xFFFF453A)), contentAlignment = Alignment.CenterEnd) {
                Column(
                    modifier = Modifier.width(88.dp).fillMaxSize()
                        .clickable(enabled = enabled, onClick = currentDelete)
                        .testTag("delete_$code"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Text("Delete", color = Color.White)
                }
            }
        }
        Box(
            Modifier
                .offset { IntOffset(offset.roundToInt(), 0) }
                .background(colors.background)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = enabled && canDelete,
                    state = rememberDraggableState { delta -> offset = (offset + delta).coerceIn(-width.toFloat(), 0f) },
                    onDragStarted = {
                        settling?.cancel()
                        onOpen()
                    },
                    onDragStopped = {
                        settling = scope.launch {
                            val delete = width > 0 && -offset >= width * 0.75f
                            val target = when {
                                delete -> -width.toFloat()
                                -offset >= revealWidth / 2f -> -revealWidth
                                else -> 0f
                            }
                            animate(offset, target) { value, _ -> offset = value }
                            if (delete && deletionEnabled) currentDelete()
                        }
                    }
                )
        ) { content() }
    }
}
