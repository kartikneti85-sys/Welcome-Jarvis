package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IntelNote
import com.example.ui.theme.JarvisBgDark
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JarvisIntelNotesView(
    notes: List<IntelNote>,
    onAddNote: (String) -> Unit,
    onDeleteNote: (IntelNote) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    var newNoteContent by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STARK INTEL DATABASE",
                    color = JarvisCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = { isAdding = !isAdding },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("toggle_add_note_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isAdding) "CANCEL" else "NEW INTEL", color = JarvisCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // Add Note Box
        if (isAdding) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = JarvisSurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RECORD NEW INTEL MEMO",
                        color = JarvisGold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        placeholder = { Text("Enter intelligence data...", color = JarvisTextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth().testTag("new_note_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderCyan
                        )
                    )
                    Button(
                        onClick = {
                            if (newNoteContent.isNotBlank()) {
                                onAddNote("Note: $newNoteContent")
                                newNoteContent = ""
                                isAdding = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End).testTag("save_note_button")
                    ) {
                        Text("COMMIT TO VAULT", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Notes List
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(JarvisSurfaceDark, RoundedCornerShape(10.dp))
                    .border(1.dp, JarvisBorderCyan, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No intelligence memos archived.\nSay \"Note: [content]\" or tap New Intel.",
                    color = JarvisTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { note ->
                    IntelNoteCard(
                        note = note,
                        dateStr = timeFormat.format(Date(note.timestamp)),
                        onDelete = { onDeleteNote(note) }
                    )
                }
            }
        }
    }
}

@Composable
fun IntelNoteCard(
    note: IntelNote,
    dateStr: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, JarvisBorderCyan, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = note.title,
                    color = JarvisCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = note.content,
                    color = JarvisTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Text(
                    text = dateStr,
                    color = JarvisTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp).testTag("delete_note_${note.id}")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JarvisCrimson)
            }
        }
    }
}
