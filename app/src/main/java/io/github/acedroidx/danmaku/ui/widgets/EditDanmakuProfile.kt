package io.github.acedroidx.danmaku.ui.widgets

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.model.DanmakuMode
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.model.EmoticonGroup
import io.github.acedroidx.danmaku.ui.theme.AppTheme
import kotlinx.coroutines.flow.asStateFlow

object EditDanmakuProfile {
    @Composable
    fun Profile(
        profile: DanmakuConfig,
        emoticonVM: EmoticonViewModel = hiltViewModel(),
        onChange: ((DanmakuConfig) -> Unit)
    ) {
        AppTheme {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(label = { Text(text = "房间号") },
                        value = profile.roomid.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { it1 -> profile.copy(roomid = it1) }
                                ?.let { it2 -> onChange(it2) }
                        })
                }
                OutlinedTextField(label = { Text(text = "弹幕内容") },
                    value = profile.msg,
                    onValueChange = { onChange(profile.copy(msg = it)) })
                if (profile.msgMode == DanmakuMode.EMOTION) {
                    EmoticonPicker(emoticonVM, profile, onChange)
                }
                OutlinedTextField(label = { Text(text = "发送间隔") },
                    value = profile.interval.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { it1 -> profile.copy(interval = it1) }
                            ?.let { it2 -> onChange(it2) }
                    })
                MsgModeComposable(profile, onChange)
                ShootModeComposable(profile, onChange)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MsgModeComposable(profile: DanmakuConfig, onChange: ((DanmakuConfig) -> Unit)) {
        var expanded by remember { mutableStateOf(false) }
        val msgModes = DanmakuMode.entries
        // We want to react on tap/press on TextField to show menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = profile.msgMode.desc,
                onValueChange = {},
                label = { Text("弹幕模式") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                // colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                msgModes.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.desc) },
                        onClick = {
                            onChange(profile.copy(msgMode = selectionOption))
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShootModeComposable(profile: DanmakuConfig, onChange: ((DanmakuConfig) -> Unit)) {
        var expanded by remember { mutableStateOf(false) }
        val shootModes = DanmakuShootMode.entries
        // We want to react on tap/press on TextField to show menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = profile.shootMode.desc,
                onValueChange = {},
                label = { Text("发送模式") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                // colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                shootModes.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.desc) },
                        onClick = {
                            onChange(profile.copy(shootMode = selectionOption))
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EmoticonPickerRaw(
        profile: DanmakuConfig,
        emoticonGroups: List<EmoticonGroup>,
        onChange: ((DanmakuConfig) -> Unit)
    ) {
        var selectedEmoticonGroup by remember { mutableStateOf<EmoticonGroup?>(null) }
        Column {
            LazyRow {
                items(emoticonGroups) { item ->
                    if (item.emoticons.isNotEmpty()) {
                        Button(onClick = {
                            selectedEmoticonGroup = item
                        }) {
                            AsyncImage(
                                model = item.current_cover.replace("http://", "https://"),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(text = item.pkg_name)
                        }
                    }
                }
            }
            selectedEmoticonGroup?.let {
                LazyColumn {
                    items(it.emoticons) { item ->
                        if (item.perm == 1) {
                            Button(onClick = {
                                val emoctionmsg = if (profile.msg.isEmpty()) {
                                    item.emoticon_unique
                                } else {
                                    profile.msg + "\n${item.emoticon_unique}"
                                }
                                onChange(profile.copy(msg = emoctionmsg))
                            }) {
                                AsyncImage(
                                    model = item.url.replace("http://", "https://"),
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp)
                                )
                                Text(text = item.emoji)
                            }
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EmoticonPicker(
        emoticonVM: EmoticonViewModel,
        profile: DanmakuConfig,
        onChange: ((DanmakuConfig) -> Unit)
    ) {
        val emoticonGroups by emoticonVM.emoticonRepository.emoticonGroups.collectAsState()
        val isPrepare by emoticonVM.emoticonRepository.isPrepare.collectAsState()
        var showBottomSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )
        Button(onClick = {
            emoticonVM.getEmoticonGroups(profile.roomid)
            showBottomSheet = true
        }) {
            Text(text = "表情包")
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                if (isPrepare) {
                    EmoticonPickerRaw(profile, emoticonGroups, onChange)
                }
            }
        }
    }
}
