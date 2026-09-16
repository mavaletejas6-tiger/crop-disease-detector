package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import com.example.service.QrCodeGenerator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DocumentCategory
import com.example.model.FieldDocument
import com.example.ui.theme.FieldGreenDark
import com.example.ui.theme.FieldGreenLight
import com.example.ui.theme.FieldGreenPrimary
import com.example.ui.theme.LeafTeal
import com.example.ui.theme.SkyWeatherBlue
import com.example.ui.theme.SoilBrown
import com.example.ui.theme.WheatGold
import com.example.ui.theme.WheatGoldLight
import com.example.viewmodel.GrainScanUiState
import com.example.viewmodel.GrainScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    uiState: GrainScanUiState,
    viewModel: GrainScanViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Filter documents based on Category and Search Query
    val filteredDocs = remember(uiState.documents, uiState.selectedDocumentCategory, uiState.documentSearchQuery) {
        uiState.documents.filter { doc ->
            val matchesCategory = uiState.selectedDocumentCategory == DocumentCategory.ALL || doc.category == uiState.selectedDocumentCategory
            val matchesSearch = if (uiState.documentSearchQuery.isBlank()) {
                true
            } else {
                val q = uiState.documentSearchQuery.trim().lowercase()
                doc.title.lowercase().contains(q) ||
                        doc.referenceCode.lowercase().contains(q) ||
                        doc.summary.lowercase().contains(q) ||
                        doc.author.lowercase().contains(q) ||
                        doc.tags.any { it.lowercase().contains(q) }
            }
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("document_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Document & Protocol Hub",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Official Grain Certificates, USDA Standards & IPM Field Manuals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { viewModel.setShowCreateDocumentDialog(true) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(FieldGreenPrimary.copy(alpha = 0.12f))
                        .testTag("button_add_custom_document")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Document Note",
                        tint = FieldGreenPrimary
                    )
                }
            }
        }

        // Action Banner: Generate Certificate from Active Scan
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = FieldGreenDark
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_generate_inspection_cert")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(WheatGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Official Certificate",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val activeLength = uiState.measurement.getMeasuredLengthMm(uiState.referenceScale)
                        val activeWidth = uiState.measurement.getMeasuredWidthMm(uiState.referenceScale)
                        val activeGrade = uiState.selectedCrop.classifyGrain(activeLength, activeWidth)
                        Text(
                            text = "Generate Inspection Certificate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Create certified report for ${uiState.selectedCrop.commonName} (${String.format("%.2f", activeLength)} mm, $activeGrade)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.generateInspectionCertificateFromScan() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WheatGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("button_generate_active_cert")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Issue Certificate Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.documentSearchQuery,
                onValueChange = { viewModel.setDocumentSearchQuery(it) },
                placeholder = { Text("Search standards, certificates, chemical safety...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (uiState.documentSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setDocumentSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("document_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentCategory.values().forEach { cat ->
                    val isSelected = uiState.selectedDocumentCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setDocumentCategory(cat) },
                        label = { Text(cat.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FieldGreenPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_category_${cat.name.lowercase()}")
                    )
                }
            }
        }

        // Count indicator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredDocs.size} Documents Available",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap to read & inspect",
                    style = MaterialTheme.typography.bodySmall,
                    color = FieldGreenLight
                )
            }
        }

        // Documents list
        if (filteredDocs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No documents match criteria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try clearing search or picking another category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredDocs, key = { it.id }) { doc ->
                DocumentCardItem(
                    document = doc,
                    onOpen = { viewModel.selectDocument(doc) },
                    onApprove = { viewModel.openApprovalDialog(doc) },
                    onViewQr = { viewModel.openQrVerificationDialog(doc) },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, doc.title)
                            putExtra(Intent.EXTRA_TEXT, "${doc.title}\n\n${doc.fullContent}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Document via"))
                    },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(doc.title, doc.fullContent)
                        clipboard.setPrimaryClip(clip)
                    },
                    onDelete = if (doc.category == DocumentCategory.CUSTOM_NOTES || doc.isOfficialCertificate) {
                        { viewModel.deleteDocument(doc.id) }
                    } else null
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal: Full Document Viewer
    if (uiState.isViewingDocument && uiState.selectedDocument != null) {
        DocumentViewerDialog(
            document = uiState.selectedDocument,
            onDismiss = { viewModel.selectDocument(null) },
            onApprove = { viewModel.openApprovalDialog(uiState.selectedDocument) },
            onViewQr = { viewModel.openQrVerificationDialog(uiState.selectedDocument) },
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, uiState.selectedDocument.title)
                    putExtra(Intent.EXTRA_TEXT, "${uiState.selectedDocument.title}\n\n${uiState.selectedDocument.fullContent}")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Document via"))
            },
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(uiState.selectedDocument.title, uiState.selectedDocument.fullContent)
                clipboard.setPrimaryClip(clip)
            }
        )
    }

    // Modal: Viewer Approval Dialog
    if (uiState.showApprovalDialog && uiState.documentToApprove != null) {
        ApprovalDialog(
            document = uiState.documentToApprove,
            onDismiss = { viewModel.closeApprovalDialog() },
            onConfirmApproval = { name, role, notes ->
                viewModel.approveDocument(uiState.documentToApprove.id, name, role, notes)
            }
        )
    }

    // Modal: Verification QR Code Dialog
    if (uiState.showQrVerificationDialog && uiState.verifiedDocumentForQr != null) {
        VerificationQrDialog(
            document = uiState.verifiedDocumentForQr,
            onDismiss = { viewModel.closeQrVerificationDialog() }
        )
    }

    // Modal: Create Custom Document Note
    if (uiState.showCreateDocumentDialog) {
        CreateDocumentDialog(
            onDismiss = { viewModel.setShowCreateDocumentDialog(false) },
            onConfirm = { title, cat, content, author ->
                viewModel.addCustomDocument(title, cat, content, author)
            }
        )
    }
}

@Composable
fun DocumentCardItem(
    document: FieldDocument,
    onOpen: () -> Unit,
    onApprove: () -> Unit,
    onViewQr: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (document.isApproved) {
                FieldGreenPrimary.copy(alpha = 0.09f)
            } else if (document.isOfficialCertificate) {
                FieldGreenPrimary.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (document.isApproved) {
            androidx.compose.foundation.BorderStroke(1.dp, FieldGreenPrimary.copy(alpha = 0.4f))
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("document_card_${document.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category & Reference Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = getCategoryColor(document.category).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(document.category),
                            contentDescription = null,
                            tint = getCategoryColor(document.category),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = document.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = getCategoryColor(document.category)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (document.isApproved) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = FieldGreenPrimary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = FieldGreenPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("VERIFIED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold, color = FieldGreenPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = document.referenceCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Author & Date
            Text(
                text = "${document.author} • ${document.dateFormatted}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = document.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Verified Banner on Card
            if (document.isApproved) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FieldGreenPrimary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FieldGreenPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Approved", tint = FieldGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Approved by ${document.approvedBy ?: "Viewer"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldGreenDark
                                )
                                Text(
                                    text = "${document.approvedRole ?: "Inspector"} • ${document.approvedTimestamp?.take(10) ?: ""}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(
                            onClick = onViewQr,
                            modifier = Modifier.testTag("button_card_qr_${document.id}")
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = FieldGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR Code", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FieldGreenPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tags row
            if (document.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    document.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onOpen,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FieldGreenPrimary
                        ),
                        modifier = Modifier.testTag("button_read_doc_${document.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Read")
                    }

                    if (!document.isApproved) {
                        OutlinedButton(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SoilBrown
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoilBrown.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("button_card_approve_${document.id}")
                        ) {
                            Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve")
                        }
                    } else {
                        Button(
                            onClick = onViewQr,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FieldGreenPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("button_view_qr_main_${document.id}")
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verify QR")
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Document",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Document",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Document",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentViewerDialog(
    document: FieldDocument,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onViewQr: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    var copiedFeedback by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("document_viewer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (document.isApproved) Icons.Default.Verified else if (document.isOfficialCertificate) Icons.Default.Verified else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (document.isApproved) FieldGreenPrimary else if (document.isOfficialCertificate) WheatGold else FieldGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = document.referenceCode,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                color = FieldGreenPrimary
                            )
                        }
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Issued by ${document.author} • ${document.dateFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Verification Status Banner
                if (document.isApproved) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FieldGreenPrimary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FieldGreenPrimary.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = FieldGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "OFFICIALLY VERIFIED & APPROVED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FieldGreenDark
                                    )
                                    Text(
                                        text = "Approved by: ${document.approvedBy ?: "Viewer"} (${document.approvedRole ?: "Auditor"})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Signature: ${document.verificationToken?.take(16) ?: ""}...",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onViewQr,
                                colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("button_viewer_view_qr_code")
                            ) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View QR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoilBrown.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoilBrown.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.FactCheck, contentDescription = null, tint = SoilBrown, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "PENDING VIEWER APPROVAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SoilBrown
                                    )
                                    Text(
                                        text = "Review content and click Approve to generate the official verification QR.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(containerColor = SoilBrown),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("button_viewer_approve_doc")
                            ) {
                                Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Scrollable Content Box with monospace/card formatting
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = document.fullContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onCopy()
                                copiedFeedback = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FieldGreenPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (copiedFeedback) "Copied!" else "Copy Text")
                        }

                        OutlinedButton(
                            onClick = onShare,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }

                        if (document.isApproved) {
                            OutlinedButton(
                                onClick = onViewQr,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FieldGreenPrimary),
                                modifier = Modifier.testTag("button_bottom_viewer_qr")
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QR Code")
                            }
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalDialog(
    document: FieldDocument,
    onDismiss: () -> Unit,
    onConfirmApproval: (approverName: String, approverRole: String, notes: String) -> Unit
) {
    var approverName by remember { mutableStateOf("Dr. Robert Vance, Senior Auditor") }
    var approverRole by remember { mutableStateOf("Regional Agricultural Inspector") }
    var notes by remember { mutableStateOf("Verified grain metrics, dimensions, and moisture grade comply with commercial trading thresholds.") }
    var checkedMetrics by remember { mutableStateOf(true) }
    var checkedCompliance by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HowToReg, contentDescription = null, tint = FieldGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Viewer Document Approval", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reference: ${document.referenceCode} • ${document.category.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = FieldGreenPrimary
                        )
                    }
                }

                Text(
                    text = "As an authorized viewer, approving this document locks the audit trail and generates a scannable Verification QR Code with a cryptographic token.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = approverName,
                    onValueChange = {
                        approverName = it
                        showError = false
                    },
                    label = { Text("Approver / Viewer Name *") },
                    placeholder = { Text("e.g., Inspector John Doe") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_approver_name"),
                    singleLine = true,
                    isError = showError && approverName.isBlank()
                )

                OutlinedTextField(
                    value = approverRole,
                    onValueChange = {
                        approverRole = it
                        showError = false
                    },
                    label = { Text("Designation / Authority *") },
                    placeholder = { Text("e.g., Chief Quality Assessor") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_approver_role"),
                    singleLine = true,
                    isError = showError && approverRole.isBlank()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Approval Remarks & Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("input_approver_notes"),
                    maxLines = 4
                )

                // Checkboxes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checkedMetrics = !checkedMetrics }
                ) {
                    Checkbox(
                        checked = checkedMetrics,
                        onCheckedChange = { checkedMetrics = it },
                        colors = CheckboxDefaults.colors(checkedColor = FieldGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "I have reviewed sample grain metrics and verified findings.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checkedCompliance = !checkedCompliance }
                ) {
                    Checkbox(
                        checked = checkedCompliance,
                        onCheckedChange = { checkedCompliance = it },
                        colors = CheckboxDefaults.colors(checkedColor = FieldGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Issue official verification seal and QR code.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (showError) {
                    Text(
                        text = "Please fill in approver name and role, and confirm both verification checks.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (approverName.isBlank() || approverRole.isBlank() || !checkedMetrics || !checkedCompliance) {
                        showError = true
                    } else {
                        onConfirmApproval(approverName.trim(), approverRole.trim(), notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary),
                modifier = Modifier.testTag("button_confirm_approval_issue_qr")
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Approve & Issue QR")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("button_dismiss_approval")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun VerificationQrDialog(
    document: FieldDocument,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copiedFeedback by remember { mutableStateOf(false) }
    var scanSimulated by remember { mutableStateOf(false) }

    val qrPayload = remember(document) {
        document.verificationQrPayload ?: run {
            val approver = document.approvedBy ?: "Certified Viewer"
            val role = document.approvedRole ?: "Grain Quality Inspector"
            val time = document.approvedTimestamp ?: document.dateFormatted
            val token = document.verificationToken ?: FieldDocument.generateVerificationToken(document.id, document.referenceCode, approver, time)
            FieldDocument.generateQrPayload(document, approver, role, time, token)
        }
    }

    val qrBitmap = remember(qrPayload) {
        QrCodeGenerator.generateQrBitmap(qrPayload, sizePx = 512)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("dialog_verification_qr")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = FieldGreenPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = FieldGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VERIFICATION CERTIFICATE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cryptographically Signed & Approved",
                                style = MaterialTheme.typography.labelSmall,
                                color = FieldGreenPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_close_qr_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // QR Code Container Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(2.dp, FieldGreenPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Verification QR Code for ${document.title}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .testTag("qr_code_image")
                            )
                        } else {
                            Text(
                                text = "Generating QR Code...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Verification Badge & Document Summary
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ref: ${document.referenceCode} • ${document.category.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Approved By:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(document.approvedBy ?: "Dr. Robert Vance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FieldGreenDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Authority / Role:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(document.approvedRole ?: "Quality Inspector", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Certified At:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(document.approvedTimestamp ?: document.dateFormatted, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (!document.approvalNotes.isNullOrBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Inspector Note:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(document.approvalNotes, style = MaterialTheme.typography.labelSmall, maxLines = 2, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cryptographic Token preview
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SHA-256 VERIFICATION HASH",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (copiedFeedback) "COPIED!" else "TAP TO COPY",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (copiedFeedback) FieldGreenPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Verification Token", document.verificationToken ?: "")
                                    clipboard.setPrimaryClip(clip)
                                    copiedFeedback = true
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = document.verificationToken ?: "TOKEN-NOT-GENERATED",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Scan Simulation Feedback Banner
                if (scanSimulated) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FieldGreenPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FieldGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FieldGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SCAN SUCCESSFUL: 100% AUTHENTIC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldGreenDark
                                )
                                Text(
                                    text = "Signature matching registry certificate #REF-${document.referenceCode}. Approved by ${document.approvedBy ?: "Viewer"}.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Interactive Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { scanSimulated = !scanSimulated },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("button_test_qr_scan")
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (scanSimulated) "Hide Scan" else "Simulate Scan", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Verification Certificate: ${document.title}")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "OFFICIAL GRAIN INSPECTION VERIFICATION\n\n" +
                                            "Document: ${document.title}\n" +
                                            "Reference: ${document.referenceCode}\n" +
                                            "Category: ${document.category.displayName}\n" +
                                            "Approved By: ${document.approvedBy} (${document.approvedRole})\n" +
                                            "Certified Date: ${document.approvedTimestamp}\n" +
                                            "Verification Token: ${document.verificationToken}\n" +
                                            "Status: VERIFIED & COMPLIANT\n\n" +
                                            "Scan the attached QR code in the Field Quality app to inspect full telemetry."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share QR Verification via"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("button_share_qr_verification")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share QR", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateDocumentDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: DocumentCategory, content: String, author: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DocumentCategory.CUSTOM_NOTES) }
    var author by remember { mutableStateOf("Lead Agronomist") }
    var content by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EditNote, contentDescription = null, tint = FieldGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Field Note / Dossier")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        isError = false
                    },
                    label = { Text("Document / Note Title *") },
                    placeholder = { Text("e.g., Plot 4 Harvest Assessment") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError && title.isBlank(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author / Inspector Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DocumentCategory.values().filter { it != DocumentCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        isError = false
                    },
                    label = { Text("Document Content / Observations *") },
                    placeholder = { Text("Enter field inspection details, agronomic findings, or management directives...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    isError = isError && content.isBlank(),
                    maxLines = 8
                )
                if (isError) {
                    Text(
                        text = "Title and Content cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(title, selectedCategory, content, author)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary)
            ) {
                Text("Save Document")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getCategoryIcon(category: DocumentCategory): ImageVector = when (category) {
    DocumentCategory.ALL -> Icons.Default.Description
    DocumentCategory.CERTIFICATES -> Icons.Default.Verified
    DocumentCategory.GRADING_STANDARDS -> Icons.AutoMirrored.Filled.Assignment
    DocumentCategory.AGRONOMY_GUIDES -> Icons.Default.Eco
    DocumentCategory.STORAGE_PROTOCOLS -> Icons.Default.Storage
    DocumentCategory.CUSTOM_NOTES -> Icons.AutoMirrored.Filled.Notes
}

fun getCategoryColor(category: DocumentCategory): Color = when (category) {
    DocumentCategory.ALL -> FieldGreenPrimary
    DocumentCategory.CERTIFICATES -> WheatGold
    DocumentCategory.GRADING_STANDARDS -> SkyWeatherBlue
    DocumentCategory.AGRONOMY_GUIDES -> LeafTeal
    DocumentCategory.STORAGE_PROTOCOLS -> SoilBrown
    DocumentCategory.CUSTOM_NOTES -> FieldGreenLight
}
