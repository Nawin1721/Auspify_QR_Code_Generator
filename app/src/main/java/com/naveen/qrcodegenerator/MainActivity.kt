
package com.naveen.qrcodegenerator

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.content.Intent
import java.io.File
import java.io.IOException

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.FileProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                QRGeneratorScreen()
            }
        }
    }
}

@Composable
fun QRGeneratorScreen() {

    var inputText by remember {
        mutableStateOf("")
    }

    var qrBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var saveMessage by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

    // SAVE QR CODE
    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->

        if (uri != null && qrBitmap != null) {

            try {

                val outputStream = context.contentResolver
                    .openOutputStream(uri)
                    ?: throw IOException("Cannot open output stream")

                outputStream.use {

                    val saved = qrBitmap!!.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        it
                    )

                    if (!saved) {
                        throw IOException("PNG compression failed")
                    }
                }

                saveMessage = "QR Code saved successfully!"

            } catch (e: Exception) {

                saveMessage = "Failed to save QR Code"
                e.printStackTrace()
            }

        } else {

            saveMessage = "Save cancelled"
        }
    }

    // SHARE QR CODE
    fun shareQRCode() {

        val bitmap = qrBitmap ?: return

        try {

            val shareDirectory = File(
                context.cacheDir,
                "shared_qr_codes"
            )

            if (!shareDirectory.exists() &&
                !shareDirectory.mkdirs()
            ) {
                throw IOException("Cannot create cache directory")
            }

            val imageFile = File(
                shareDirectory,
                "MyQRCode.png"
            )

            imageFile.outputStream().use { outputStream ->

                val saved = bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    outputStream
                )

                if (!saved) {
                    throw IOException("Image compression failed")
                }
            }

            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {

                type = "image/png"

                putExtra(
                    Intent.EXTRA_STREAM,
                    imageUri
                )

                clipData = android.content.ClipData.newUri(
                    context.contentResolver,
                    "QR Code",
                    imageUri
                )

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share QR Code"
                )
            )

            saveMessage = "Choose an app to share QR Code"

        } catch (e: Exception) {

            saveMessage = "Unable to share QR Code"
            e.printStackTrace()
        }
    }

    // MAIN UI
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(35.dp))

            // APP TITLE
            Text(
                text = "QR Code Generator",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Generate QR codes easily",
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(35.dp))

            // INPUT FIELD
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    saveMessage = ""
                },
                label = {
                    Text("Enter Text or URL")
                },
                placeholder = {
                    Text("https://example.com")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // GENERATE BUTTON
            Button(
                onClick = {

                    qrBitmap = QRGenerator.generateQRCode(inputText)

                    saveMessage = if (qrBitmap != null) {
                        "QR Code generated successfully!"
                    } else {
                        "Unable to generate QR Code"
                    }
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "Generate QR Code",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // QR CODE PREVIEW
            Card(
                modifier = Modifier.size(240.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 5.dp
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    if (qrBitmap != null) {

                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )

                    } else {

                        Text(
                            text = "QR Code Appears Here",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // SAVE AND SHARE BUTTONS
            if (qrBitmap != null) {

                Spacer(modifier = Modifier.height(20.dp))

                // SAVE BUTTON
                OutlinedButton(
                    onClick = {
                        saveLauncher.launch("MyQRCode.png")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text = "Save QR Code",
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SHARE BUTTON
                Button(
                    onClick = {
                        shareQRCode()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text = "Share QR Code",
                        fontSize = 16.sp
                    )
                }
            }

            // STATUS MESSAGE
            if (saveMessage.isNotEmpty()) {

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = saveMessage,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
