package net.ucoz.testcompose.presentation.widget.lazyList

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import net.ucoz.testcompose.presentation.widget.scroll.drawVerticalScrollbar
import kotlin.math.roundToInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import net.ucoz.testcompose.presentation.widget.scroll.fastSumBy
import net.ucoz.testcompose.presentation.widget.scroll.toDp
import kotlin.math.abs


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalMaterialApi
@Composable
fun LazyListWithFloatingButtonV3(
    bottomContent: @Composable () -> Unit,
    enabledIndicator: Boolean = false,
    state: LazyListState = rememberLazyListState(),
    thickness: Dp = 6.dp,
    topPaddingIndicator: Dp = 0.dp,
    bottomPaddingIndicator: Dp = 0.dp,
    endPaddingIndicator: Dp = 8.dp,
    topContent: LazyListScope.() -> Unit,
) {
    var padding by remember {
        mutableStateOf(0)
    }
    var offset by remember {
        mutableStateOf(0)
    }
    var height by remember {
        mutableStateOf(0)
    }

    var heightLazy by remember {
        mutableStateOf(0)
    }

    val localDensity = LocalDensity.current
    val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }

    val local = LocalDensity.current

    LaunchedEffect(true) {
        heightLazy = height
    }

    LaunchedEffect(state.isScrolledToTheEnd(), padding ) {
        if (padding > 0) {
            heightLazy = height - offset
        } else {
            heightLazy = height
        }

    }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val bottomBarHeight = offset.toDp().dp
                val bottomBarHeightPx = with(localDensity) { bottomBarHeight.roundToPx().toFloat() }

                val delta = available.y
                Log.d("Mytag", "delta = ${delta}")
                val newOffset = bottomBarOffsetHeightPx.value + delta
                bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                padding = abs(bottomBarOffsetHeightPx.value.roundToInt())
                Log.d("Mytag", "padding = ${padding}")
                Log.d("Mytag", "offset = ${offset}")
                return Offset.Zero
            }
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .onGloballyPositioned {
                height = it.size.height
            },
        bottomBar = {
            AnimatedVisibility(visible = state.isScrolledToTheEnd()) {
//            AnimatedVisibility(visible = padding != 0) {
                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            offset = it.size.height
                        }

                ) {
                    bottomContent()
                }
            }
        },
    ) {
        LazyColumn(
            userScrollEnabled = true,
            modifier = if (enabledIndicator) {
                Modifier
                    .drawVerticalScrollbar(
                        state,
                        thickness = thickness,
                        topPaddingIndicator = topPaddingIndicator,
                        bottomPaddingIndicator = bottomPaddingIndicator,
                        endPaddingIndicator = endPaddingIndicator,
                    )
                    .height(heightLazy.toDp().dp)
            } else {
                Modifier
                    .height(heightLazy.toDp().dp)
            },
            state = state,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            topContent()
        }
    }

    LaunchedEffect(state.layoutInfo.totalItemsCount) {
        if (state.isScrolledToTheEnd()) {
            state.scrollToItem(state.layoutInfo.totalItemsCount - 1)
        }
    }
}


fun LazyListState.isScrolledToTheEnd() =
    layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1