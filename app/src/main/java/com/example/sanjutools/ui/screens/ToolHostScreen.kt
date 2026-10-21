package com.example.sanjutools.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.example.sanjutools.model.ToolCategory
import com.example.sanjutools.model.ToolRegistry
import com.example.sanjutools.viewmodel.MainViewModel

@Composable
fun ToolHostScreen(
    toolId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val tool = ToolRegistry.getToolById(toolId) ?: run {
        onBack()
        return
    }

    when (tool.category) {
        ToolCategory.PHOTO -> {
            PhotoToolsScreen(tool = tool, onBack = onBack)
        }
        ToolCategory.QR_BARCODE -> {
            QrBarcodeScreen(tool = tool, onBack = onBack)
        }
        ToolCategory.PDF -> {
            PdfToolsScreen(tool = tool, onBack = onBack)
        }
        ToolCategory.NOTES -> {
            NotesScreen(tool = tool, viewModel = viewModel, onBack = onBack)
        }
        ToolCategory.CALCULATOR -> {
            CalculatorScreen(tool = tool, onBack = onBack)
        }
        ToolCategory.FILE_TEXT -> {
            FileTextScreen(tool = tool, onBack = onBack)
        }
        ToolCategory.DAILY -> {
            DailyToolsScreen(tool = tool, viewModel = viewModel, onBack = onBack)
        }
        ToolCategory.DISCOVER -> {
            DiscoverToolsScreen(
                tool = tool,
                viewModel = viewModel,
                onOpenTool = { id -> viewModel.openTool(id) },
                onBack = onBack
            )
        }
        else -> {
            PhotoToolsScreen(tool = tool, onBack = onBack)
        }
    }
}
