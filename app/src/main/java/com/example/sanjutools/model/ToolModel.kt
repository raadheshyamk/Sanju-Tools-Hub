package com.example.sanjutools.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val title: String, val description: String) {
    ALL("All Tools", "Browse complete utility collection"),
    PHOTO("Photo Tools", "Convert, resize, crop, filters, collage & PDF"),
    QR_BARCODE("QR & Barcode", "Generate, style, customize & scan QR and barcodes"),
    PDF("PDF Tools", "Images to PDF, split, merge, compress & watermark"),
    NOTES("Notes & Writing", "Smart notes, checklists, text utilities & TTS"),
    CALCULATOR("Calculator", "Standard, scientific, converters & age/date math"),
    FILE_TEXT("File & Text", "File checker, base64, hash, json, password & diff"),
    DAILY("Daily Tools", "Clock, timer, stopwatch, tasks, pomodoro & expenses"),
    DISCOVER("Discover", "AI tool finder, suggestions & upcoming roadmap")
}

data class ToolItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val isPopular: Boolean = false,
    val badge: String? = null,
    val shortTitle: String = title
)

fun ToolItem.getIcon(): ImageVector = when (id) {
    // PHOTO
    "photo_jpg_to_png" -> Icons.Default.Transform
    "photo_png_to_jpg" -> Icons.Default.SwapCalls
    "photo_webp_to_png" -> Icons.Default.SyncAlt
    "photo_png_to_webp" -> Icons.Default.Public
    "photo_compress" -> Icons.Default.Compress
    "photo_resize" -> Icons.Default.AspectRatio
    "photo_crop" -> Icons.Default.Crop
    "photo_rotate" -> Icons.Default.RotateRight
    "photo_flip" -> Icons.Default.Flip
    "photo_remove_bg" -> Icons.Default.AutoFixHigh
    "photo_transparent_bg" -> Icons.Default.LayersClear
    "photo_solid_bg" -> Icons.Default.ColorLens
    "photo_black_white" -> Icons.Default.Contrast
    "photo_blur" -> Icons.Default.BlurOn
    "photo_add_text" -> Icons.Default.TextFields
    "photo_watermark" -> Icons.Default.BrandingWatermark
    "photo_merge" -> Icons.Default.CallMerge
    "photo_collage" -> Icons.Default.GridView
    "photo_passport" -> Icons.Default.Badge
    "photo_to_pdf" -> Icons.Default.PictureAsPdf
    "photo_extract_pdf" -> Icons.Default.FileOpen
    "photo_info" -> Icons.Default.Straighten
    "photo_format_converter" -> Icons.Default.Shuffle
    "photo_quality_settings" -> Icons.Default.Tune

    // QR & BARCODE
    "qr_text" -> Icons.Default.QrCode
    "qr_url" -> Icons.Default.Link
    "qr_wifi" -> Icons.Default.Wifi
    "qr_contact" -> Icons.Default.ContactPhone
    "qr_email" -> Icons.Default.Email
    "qr_phone" -> Icons.Default.Phone
    "qr_whatsapp" -> Icons.Default.Chat
    "qr_payment" -> Icons.Default.Payment
    "qr_customize_colors" -> Icons.Default.Palette
    "qr_logo" -> Icons.Default.InsertEmoticon
    "qr_download" -> Icons.Default.Download
    "qr_read_image" -> Icons.Default.QrCodeScanner
    "barcode_generate" -> Icons.Default.ViewWeek
    "barcode_decode" -> Icons.Default.DocumentScanner

    // PDF TOOLS
    "pdf_images_to_pdf" -> Icons.Default.PictureAsPdf
    "pdf_to_images" -> Icons.Default.Image
    "pdf_merge" -> Icons.Default.MergeType
    "pdf_split" -> Icons.Default.CallSplit
    "pdf_compress" -> Icons.Default.Compress
    "pdf_rotate" -> Icons.Default.RotateRight
    "pdf_reorder" -> Icons.Default.Reorder
    "pdf_delete_pages" -> Icons.Default.Delete
    "pdf_extract_pages" -> Icons.Default.ContentCut
    "pdf_page_numbers" -> Icons.Default.FormatListNumbered
    "pdf_watermark" -> Icons.Default.BrandingWatermark
    "pdf_pages_to_images" -> Icons.Default.Collections
    "pdf_info" -> Icons.Default.Info
    "pdf_download" -> Icons.Default.Download

    // NOTES & WRITING
    "notes_create" -> Icons.Default.PostAdd
    "notes_edit_delete" -> Icons.Default.EditNote
    "notes_auto_save" -> Icons.Default.Save
    "notes_search" -> Icons.Default.Search
    "notes_pin" -> Icons.Default.PushPin
    "notes_folders" -> Icons.Default.Folder
    "notes_tags" -> Icons.Default.Label
    "notes_todo_list" -> Icons.Default.TaskAlt
    "notes_checklist" -> Icons.Default.Checklist
    "text_word_counter" -> Icons.Default.Numbers
    "text_character_counter" -> Icons.Default.Abc
    "text_case_converter" -> Icons.Default.FormatSize
    "text_remove_spaces" -> Icons.Default.SpaceBar
    "text_remove_duplicates" -> Icons.Default.FilterList
    "text_sort_lines" -> Icons.Default.SortByAlpha
    "text_to_speech" -> Icons.Default.VolumeUp
    "speech_to_text" -> Icons.Default.Mic
    "notes_export_txt" -> Icons.Default.Description
    "notes_export_pdf" -> Icons.Default.PictureAsPdf
    "notes_copy_one_click" -> Icons.Default.ContentCopy

    // CALCULATOR & CONVERSION
    "calc_standard" -> Icons.Default.Calculate
    "calc_scientific" -> Icons.Default.Functions
    "calc_percentage" -> Icons.Default.Percent
    "calc_age" -> Icons.Default.Cake
    "calc_date_diff" -> Icons.Default.CalendarToday
    "calc_time" -> Icons.Default.Schedule
    "conv_unit_universal" -> Icons.Default.SwapHoriz
    "conv_length" -> Icons.Default.Straighten
    "conv_weight" -> Icons.Default.FitnessCenter
    "conv_temperature" -> Icons.Default.Thermostat
    "conv_area" -> Icons.Default.CropFree
    "conv_volume" -> Icons.Default.LocalDrink
    "conv_speed" -> Icons.Default.Speed
    "conv_data_storage" -> Icons.Default.SdStorage
    "conv_number_system" -> Icons.Default.Pin
    "conv_currency" -> Icons.Default.CurrencyExchange

    // FILE & TEXT
    "file_size_checker" -> Icons.Default.Storage
    "file_extension_checker" -> Icons.Default.Extension
    "file_info_viewer" -> Icons.Default.FindInPage
    "file_zip_creator" -> Icons.Default.FolderZip
    "text_base64" -> Icons.Default.Key
    "text_url_encode" -> Icons.Default.Link
    "text_html_entity" -> Icons.Default.Code
    "text_json_formatter" -> Icons.Default.DataObject
    "tool_password_gen" -> Icons.Default.VpnKey
    "tool_random_number" -> Icons.Default.Shuffle
    "tool_uuid_gen" -> Icons.Default.Fingerprint
    "tool_lorem_ipsum" -> Icons.Default.Notes
    "text_comparison" -> Icons.Default.Difference
    "text_to_pdf_util" -> Icons.Default.PictureAsPdf
    "text_markdown_preview" -> Icons.Default.Preview
    "color_picker_palette" -> Icons.Default.Palette
    "color_converter_hex_rgb" -> Icons.Default.Colorize
    "color_image_picker" -> Icons.Default.ColorLens

    // DAILY TOOLS
    "daily_digital_clock" -> Icons.Default.AccessTime
    "daily_stopwatch" -> Icons.Default.Timer
    "daily_countdown" -> Icons.Default.HourglassBottom
    "daily_calendar" -> Icons.Default.CalendarMonth
    "daily_world_clock" -> Icons.Default.Public
    "daily_qr_scanner_cam" -> Icons.Default.CameraAlt
    "daily_aspect_ratio_calc" -> Icons.Default.FitScreen
    "daily_random_name_picker" -> Icons.Default.EmojiEvents
    "daily_decision_picker" -> Icons.Default.Casino
    "daily_pomodoro" -> Icons.Default.HourglassTop
    "daily_task_planner" -> Icons.Default.CheckCircle
    "daily_expense_tracker" -> Icons.Default.AttachMoney

    // DISCOVER
    "discover_ai_finder" -> Icons.Default.AutoAwesome
    "discover_suggest_tool" -> Icons.Default.AddComment
    "discover_roadmap" -> Icons.Default.Map

    else -> when (category) {
        ToolCategory.PHOTO -> Icons.Default.Photo
        ToolCategory.QR_BARCODE -> Icons.Default.QrCode
        ToolCategory.PDF -> Icons.Default.PictureAsPdf
        ToolCategory.NOTES -> Icons.Default.EditNote
        ToolCategory.CALCULATOR -> Icons.Default.Calculate
        ToolCategory.FILE_TEXT -> Icons.Default.Code
        ToolCategory.DAILY -> Icons.Default.AccessTime
        else -> Icons.Default.AutoAwesome
    }
}


object ToolRegistry {
    val tools: List<ToolItem> = listOf(
        // PHOTO TOOLS (1 - 24)
        ToolItem("photo_jpg_to_png", "Convert JPG to PNG", "Convert lossy JPG images to transparent PNG format with high fidelity", ToolCategory.PHOTO, isPopular = true),
        ToolItem("photo_png_to_jpg", "Convert PNG to JPG", "Convert PNG photos to compressed JPG with custom quality", ToolCategory.PHOTO),
        ToolItem("photo_webp_to_png", "Convert WEBP to PNG", "Convert modern WEBP web images to standard lossless PNG format", ToolCategory.PHOTO),
        ToolItem("photo_png_to_webp", "Convert PNG to WEBP", "Modern web-optimized WEBP conversion for fast loading", ToolCategory.PHOTO),
        ToolItem("photo_compress", "Compress Image", "Significantly reduce file size with adjustable quality slider", ToolCategory.PHOTO, isPopular = true, badge = "Top"),
        ToolItem("photo_resize", "Resize Image", "Scale dimensions by exact pixels or percentage scale", ToolCategory.PHOTO),
        ToolItem("photo_crop", "Crop Image", "Crop to 1:1 square, 4:3, 16:9, or passport ratios", ToolCategory.PHOTO, isPopular = true),
        ToolItem("photo_rotate", "Rotate Image", "Rotate 90°, 180°, or 270° clockwise and counterclockwise", ToolCategory.PHOTO),
        ToolItem("photo_flip", "Flip Image", "Mirror photo horizontally or vertically", ToolCategory.PHOTO),
        ToolItem("photo_remove_bg", "Remove Background", "Chroma key / color threshold background removal", ToolCategory.PHOTO, badge = "AI/Auto"),
        ToolItem("photo_transparent_bg", "Transparent Background", "Turn white or uniform background into transparent alpha", ToolCategory.PHOTO),
        ToolItem("photo_solid_bg", "Solid Color Background", "Add a vibrant background color behind your cutout image", ToolCategory.PHOTO),
        ToolItem("photo_black_white", "Black & White Filter", "Professional grayscale and high-contrast B&W conversion", ToolCategory.PHOTO),
        ToolItem("photo_blur", "Blur Image", "Apply subtle or heavy aesthetic blur effects", ToolCategory.PHOTO),
        ToolItem("photo_add_text", "Add Text to Image", "Overlay custom text with fonts, sizes, and positioning", ToolCategory.PHOTO),
        ToolItem("photo_watermark", "Add Watermark", "Protect images with diagonal repeating or single watermark text", ToolCategory.PHOTO),
        ToolItem("photo_merge", "Merge Multiple Images", "Stitch two or more photos horizontally, vertically, or side by side", ToolCategory.PHOTO),
        ToolItem("photo_collage", "Make Photo Collage", "Create a neat multi-photo grid layout with rounded borders", ToolCategory.PHOTO, isPopular = true),
        ToolItem("photo_passport", "Passport Size Photo", "Create official 35x45mm passport photo sheets ready to print", ToolCategory.PHOTO, isPopular = true, badge = "Popular"),
        ToolItem("photo_to_pdf", "Convert Image to PDF", "Turn single or multiple photos into standard A4 PDF document", ToolCategory.PHOTO, isPopular = true, badge = "Hot"),
        ToolItem("photo_extract_pdf", "Extract Images from PDF", "Render and extract high-resolution pages from PDF files", ToolCategory.PHOTO),
        ToolItem("photo_info", "Image Dimensions Checker", "Analyze width, height, aspect ratio, color depth, and file size", ToolCategory.PHOTO),
        ToolItem("photo_format_converter", "Universal Image Converter", "Switch between JPG, PNG, and WEBP with one tap", ToolCategory.PHOTO),
        ToolItem("photo_quality_settings", "Photo Quality Inspector", "Real-time interactive compression preview slider", ToolCategory.PHOTO),

        // QR CODE & BARCODE TOOLS (1 - 14)
        ToolItem("qr_text", "QR from Text", "Generate standard QR code from any plain text message", ToolCategory.QR_BARCODE, isPopular = true),
        ToolItem("qr_url", "QR from Website URL", "Create scannable QR code linking to any web page", ToolCategory.QR_BARCODE, isPopular = true, badge = "Top"),
        ToolItem("qr_wifi", "QR for Wi-Fi Network", "Generate quick Wi-Fi login QR code with SSID and password", ToolCategory.QR_BARCODE, isPopular = true, badge = "Smart"),
        ToolItem("qr_contact", "QR Contact (vCard)", "Share contact info (name, phone, email, company) via QR", ToolCategory.QR_BARCODE),
        ToolItem("qr_email", "QR for Email", "Generate mailto QR with recipient, subject, and body prefilled", ToolCategory.QR_BARCODE),
        ToolItem("qr_phone", "QR for Phone Number", "Instant tap-to-call QR code for any phone number", ToolCategory.QR_BARCODE),
        ToolItem("qr_whatsapp", "QR for WhatsApp Chat", "Direct WhatsApp link QR code with optional start message", ToolCategory.QR_BARCODE, isPopular = true),
        ToolItem("qr_payment", "QR for Payment / UPI", "Create UPI and payment links for instant scanning", ToolCategory.QR_BARCODE, badge = "Pay"),
        ToolItem("qr_customize_colors", "Customize QR Colors", "Personalize QR foreground and background with brand colors", ToolCategory.QR_BARCODE),
        ToolItem("qr_logo", "QR Code with Logo Center", "Embed custom logo or symbol in the center of the QR matrix", ToolCategory.QR_BARCODE),
        ToolItem("qr_download", "Download QR (PNG)", "Save generated QR codes in high-resolution PNG image format", ToolCategory.QR_BARCODE),
        ToolItem("qr_read_image", "Scan QR from Image", "Upload any photo or screenshot to extract and decode QR data", ToolCategory.QR_BARCODE, isPopular = true),
        ToolItem("barcode_generate", "Generate Barcodes", "Create Code-128, EAN-13, and UPC-A standard barcodes", ToolCategory.QR_BARCODE),
        ToolItem("barcode_decode", "Decode Barcode from Image", "Scan and identify barcode digits from uploaded photos", ToolCategory.QR_BARCODE),

        // PDF TOOLS (1 - 14)
        ToolItem("pdf_images_to_pdf", "Images to PDF", "Combine multiple pictures into an organized PDF document", ToolCategory.PDF, isPopular = true, badge = "Hot"),
        ToolItem("pdf_to_images", "PDF to Images", "Render each page of a PDF document into PNG images", ToolCategory.PDF, isPopular = true),
        ToolItem("pdf_merge", "Merge PDF Files", "Stitch pages and documents together into a single master PDF", ToolCategory.PDF),
        ToolItem("pdf_split", "Split PDF Pages", "Separate specific pages or ranges from any PDF document", ToolCategory.PDF),
        ToolItem("pdf_compress", "Compress PDF", "Optimize and compress PDF document size for easy sharing", ToolCategory.PDF),
        ToolItem("pdf_rotate", "Rotate PDF Pages", "Fix orientation by rotating PDF pages 90°, 180°, or 270°", ToolCategory.PDF),
        ToolItem("pdf_reorder", "Reorder PDF Pages", "Rearrange the sequence of pages within a PDF file", ToolCategory.PDF),
        ToolItem("pdf_delete_pages", "Delete PDF Pages", "Remove unwanted or blank pages from your PDF", ToolCategory.PDF),
        ToolItem("pdf_extract_pages", "Extract PDF Pages", "Pick and export chosen pages to a fresh new PDF", ToolCategory.PDF),
        ToolItem("pdf_page_numbers", "Add Page Numbers", "Print clean 'Page X of Y' numbering onto your PDF pages", ToolCategory.PDF),
        ToolItem("pdf_watermark", "Add Watermark to PDF", "Stamp 'CONFIDENTIAL', 'DRAFT', or custom watermark on PDF", ToolCategory.PDF),
        ToolItem("pdf_pages_to_images", "Convert PDF Pages to Images", "High-res page-by-page image extraction and preview", ToolCategory.PDF),
        ToolItem("pdf_info", "View PDF Information", "Inspect page dimensions, total pages, file size, and specs", ToolCategory.PDF),
        ToolItem("pdf_download", "Download Processed PDF", "Quick access to your compiled and exported PDF documents", ToolCategory.PDF),

        // NOTES & WRITING TOOLS (1 - 20)
        ToolItem("notes_create", "Create New Note", "Rich text notepad with auto-save and folder organization", ToolCategory.NOTES, isPopular = true, badge = "Core"),
        ToolItem("notes_edit_delete", "Manage Notes", "Edit, search, pin, or delete notes easily", ToolCategory.NOTES),
        ToolItem("notes_auto_save", "Auto-Saving Notes", "Never lose a thought with real-time continuous background saving", ToolCategory.NOTES),
        ToolItem("notes_search", "Search Notes", "Fast full-text search across titles, contents, and tags", ToolCategory.NOTES),
        ToolItem("notes_pin", "Pin Important Notes", "Keep critical notes pinned to the top of your list", ToolCategory.NOTES),
        ToolItem("notes_folders", "Organize in Folders", "Sort into Work, Personal, Ideas, and Custom folders", ToolCategory.NOTES),
        ToolItem("notes_tags", "Tags Organizer", "Tag notes with custom keywords for instant discovery", ToolCategory.NOTES),
        ToolItem("notes_todo_list", "To-Do List", "Action items list with checkboxes and status tracking", ToolCategory.NOTES, isPopular = true),
        ToolItem("notes_checklist", "Interactive Checklist", "Reusable packing, shopping, or inspection checklists", ToolCategory.NOTES),
        ToolItem("text_word_counter", "Word Counter", "Calculate total words, lines, and reading duration", ToolCategory.NOTES, isPopular = true),
        ToolItem("text_character_counter", "Character Counter", "Count characters with and without whitespace", ToolCategory.NOTES),
        ToolItem("text_case_converter", "Text Case Converter", "UPPERCASE, lowercase, Title Case, camelCase, snake_case", ToolCategory.NOTES, isPopular = true),
        ToolItem("text_remove_spaces", "Remove Extra Spaces", "Strip leading/trailing spaces and collapse multiple whitespaces", ToolCategory.NOTES),
        ToolItem("text_remove_duplicates", "Remove Duplicate Lines", "Filter out repeating sentences or lines instantly", ToolCategory.NOTES),
        ToolItem("text_sort_lines", "Sort Lines Alphabetically", "Sort lines A-Z, Z-A, or by line length", ToolCategory.NOTES),
        ToolItem("text_to_speech", "Text to Speech (TTS)", "Listen to your notes read aloud with pitch & speed controls", ToolCategory.NOTES, badge = "Voice"),
        ToolItem("speech_to_text", "Speech to Text", "Dictate thoughts and convert spoken voice to written text", ToolCategory.NOTES),
        ToolItem("notes_export_txt", "Export Notes to TXT", "Save note text as standard .txt file to device", ToolCategory.NOTES),
        ToolItem("notes_export_pdf", "Export Notes to PDF", "Generate polished, printable PDF document from your note", ToolCategory.NOTES),
        ToolItem("notes_copy_one_click", "One-Click Clipboard Copy", "Fast copy with instant clipboard toast confirmation", ToolCategory.NOTES),

        // CALCULATOR & CONVERSION TOOLS (1 - 16)
        ToolItem("calc_standard", "Standard Calculator", "Everyday arithmetic with history and memory registers", ToolCategory.CALCULATOR, isPopular = true),
        ToolItem("calc_scientific", "Scientific Calculator", "Trigonometry, logarithms, powers, roots, and factorial", ToolCategory.CALCULATOR, isPopular = true, badge = "Sci"),
        ToolItem("calc_percentage", "Percentage Calculator", "Calculate percent of, percentage change, and discounts", ToolCategory.CALCULATOR, isPopular = true),
        ToolItem("calc_age", "Age Calculator", "Exact age in years, months, days, total days & next birthday", ToolCategory.CALCULATOR, isPopular = true, badge = "Top"),
        ToolItem("calc_date_diff", "Date Difference Calculator", "Calculate days, weeks, and business days between dates", ToolCategory.CALCULATOR),
        ToolItem("calc_time", "Time Calculator", "Add and subtract hours, minutes, and work shift durations", ToolCategory.CALCULATOR),
        ToolItem("conv_unit_universal", "Universal Unit Converter", "Quick multi-category measurement converter", ToolCategory.CALCULATOR, isPopular = true),
        ToolItem("conv_length", "Length Converter", "Meters, feet, inches, kilometers, miles, centimeters", ToolCategory.CALCULATOR),
        ToolItem("conv_weight", "Weight & Mass Converter", "Kilograms, pounds, grams, ounces, stones, tons", ToolCategory.CALCULATOR),
        ToolItem("conv_temperature", "Temperature Converter", "Celsius, Fahrenheit, Kelvin, and Rankine scales", ToolCategory.CALCULATOR),
        ToolItem("conv_area", "Area Converter", "Square meters, square feet, acres, hectares, sq miles", ToolCategory.CALCULATOR),
        ToolItem("conv_volume", "Volume Converter", "Liters, milliliters, gallons, cups, fluid ounces", ToolCategory.CALCULATOR),
        ToolItem("conv_speed", "Speed Converter", "Kilometers per hour, miles per hour, m/s, knots", ToolCategory.CALCULATOR),
        ToolItem("conv_data_storage", "Data Storage Converter", "Bytes, KB, MB, GB, TB, and binary Kibibytes", ToolCategory.CALCULATOR),
        ToolItem("conv_number_system", "Number System Converter", "Decimal, Binary, Hexadecimal, and Octal radix converter", ToolCategory.CALCULATOR),
        ToolItem("conv_currency", "Currency Converter", "Exchange rates across USD, EUR, GBP, INR, JPY, CAD, AUD", ToolCategory.CALCULATOR, badge = "Rates"),

        // FILE AND TEXT TOOLS (1 - 18)
        ToolItem("file_size_checker", "File Size Checker", "Examine exact file size in Bytes, KB, MB, and comparison", ToolCategory.FILE_TEXT),
        ToolItem("file_extension_checker", "File Extension Checker", "Lookup extension MIME types and recommended software", ToolCategory.FILE_TEXT),
        ToolItem("file_info_viewer", "File Info & Checksum", "File details, last modified date, and SHA-256 hash checksum", ToolCategory.FILE_TEXT),
        ToolItem("file_zip_creator", "ZIP File Creator", "Bundle text files or photos into real compressed .zip archive", ToolCategory.FILE_TEXT, badge = "Zip"),
        ToolItem("text_base64", "Base64 Encoder & Decoder", "Convert strings or files to and from Base64 encoding", ToolCategory.FILE_TEXT),
        ToolItem("text_url_encode", "URL Encoder & Decoder", "RFC 3986 URL percentage encoding and decoding", ToolCategory.FILE_TEXT),
        ToolItem("text_html_entity", "HTML Entity Encoder", "Escape and unescape HTML special characters (&, <, >, quotes)", ToolCategory.FILE_TEXT),
        ToolItem("text_json_formatter", "JSON Formatter & Validator", "Format, beautify (2/4 spaces), minify, and validate JSON syntax", ToolCategory.FILE_TEXT, isPopular = true),
        ToolItem("tool_password_gen", "Password Generator", "Cryptographically strong custom passwords with entropy meter", ToolCategory.FILE_TEXT, isPopular = true, badge = "Security"),
        ToolItem("tool_random_number", "Random Number Generator", "Generate random numbers, lottery picks, dice rolls, coin toss", ToolCategory.FILE_TEXT),
        ToolItem("tool_uuid_gen", "UUID Generator", "Generate unique UUID v4 strings in bulk or single", ToolCategory.FILE_TEXT),
        ToolItem("tool_lorem_ipsum", "Lorem Ipsum Generator", "Generate dummy placeholder text by paragraphs or words", ToolCategory.FILE_TEXT),
        ToolItem("text_comparison", "Text Comparison (Diff)", "Highlight differences, additions, and deletions between two texts", ToolCategory.FILE_TEXT),
        ToolItem("text_to_pdf_util", "Text to PDF Document", "Convert raw text documents into a formatted printable PDF", ToolCategory.FILE_TEXT),
        ToolItem("text_markdown_preview", "Markdown Previewer", "Live preview Markdown headings, lists, bold, italics, code blocks", ToolCategory.FILE_TEXT),
        ToolItem("color_picker_palette", "Color Picker & Palette", "Interactive color sliders, hex codes, and color harmonies", ToolCategory.FILE_TEXT),
        ToolItem("color_converter_hex_rgb", "HEX, RGB & HSL Converter", "Convert seamlessly between HEX, RGB, HSL, and HSV models", ToolCategory.FILE_TEXT),
        ToolItem("color_image_picker", "Image Color Eyedropper", "Sample the exact HEX / RGB color from any pixel of an uploaded photo", ToolCategory.FILE_TEXT),

        // DAILY PRODUCTIVITY TOOLS (1 - 12)
        ToolItem("daily_digital_clock", "Digital Clock & Fullscreen", "Modern glowing digital desk clock with date and animated seconds", ToolCategory.DAILY, isPopular = true),
        ToolItem("daily_stopwatch", "Precise Stopwatch", "Millisecond accuracy with lap recorder and split timer", ToolCategory.DAILY),
        ToolItem("daily_countdown", "Countdown Timer", "Custom timer with quick presets (1m, 5m, 15m, 25m) and alerts", ToolCategory.DAILY),
        ToolItem("daily_calendar", "Monthly Calendar", "Monthly view, date selector, week numbers, and event markers", ToolCategory.DAILY),
        ToolItem("daily_world_clock", "World Clock", "Time zones across New York, London, Tokyo, Sydney, Paris, Dubai, Delhi", ToolCategory.DAILY),
        ToolItem("daily_qr_scanner_cam", "Camera QR Scanner", "Live scanner using device camera with instant preview", ToolCategory.DAILY, badge = "Camera"),
        ToolItem("daily_aspect_ratio_calc", "Aspect Ratio & DPI Calc", "Aspect ratio solver (16:9, 4:3) and screen PPI/DPI calculator", ToolCategory.DAILY),
        ToolItem("daily_random_name_picker", "Random Name Picker", "Pick random winners from names list with winner animation", ToolCategory.DAILY),
        ToolItem("daily_decision_picker", "Decision & Choice Picker", "Yes / No / Maybe decider, 8-ball answers, and custom options picker", ToolCategory.DAILY),
        ToolItem("daily_pomodoro", "Pomodoro Focus Timer", "25-min focus sessions, short/long breaks, and streak tracker", ToolCategory.DAILY, isPopular = true, badge = "Focus"),
        ToolItem("daily_task_planner", "Daily Task Planner", "Prioritize high/medium/low tasks, due times, and progress bar", ToolCategory.DAILY, isPopular = true),
        ToolItem("daily_expense_tracker", "Simple Expense Tracker", "Log daily spending with categories, monthly totals & breakdown chart", ToolCategory.DAILY, isPopular = true, badge = "Money"),

        // SMART DISCOVERY (ROADMAP & AI SUGGESTIONS)
        ToolItem("discover_ai_finder", "Smart AI Tool Finder", "Search tools by your goal or problem and get instant recommendations", ToolCategory.DISCOVER, isPopular = true, badge = "Smart"),
        ToolItem("discover_suggest_tool", "Suggest a New Tool", "Submit your ideas and feature requests directly to the developers", ToolCategory.DISCOVER),
        ToolItem("discover_roadmap", "Tools Roadmap & Updates", "See upcoming utilities and newly approved features", ToolCategory.DISCOVER)
    )

    fun getToolById(id: String): ToolItem? = tools.find { it.id == id }

    fun getToolsByCategory(category: ToolCategory): List<ToolItem> =
        if (category == ToolCategory.ALL) tools else tools.filter { it.category == category }

    fun searchTools(query: String): List<ToolItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return tools
        return tools.filter {
            it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.category.title.lowercase().contains(q) ||
            it.id.lowercase().contains(q)
        }
    }
}
