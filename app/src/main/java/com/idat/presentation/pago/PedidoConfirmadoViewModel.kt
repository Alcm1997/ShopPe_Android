package com.idat.presentation.pago

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idat.domain.model.Pedido
import com.idat.domain.repository.PedidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.io.ByteArrayOutputStream

@HiltViewModel
class PedidoConfirmadoViewModel @Inject constructor(
    private val repository: PedidoRepository
) : ViewModel() {

    private fun generateQRCodeByteArray(text: String, width: Int, height: Int): ByteArray? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            Log.e("QR_GEN", "Error generating QR: ${e.message}")
            null
        }
    }

    private val _pedido = MutableStateFlow<Pedido?>(null)
    val pedido: StateFlow<Pedido?> = _pedido.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    fun cargarPedido(pedidoId: String) {
        repository.getPedidoById(pedidoId)
            .onEach { _pedido.value = it }
            .launchIn(viewModelScope)
    }

    fun generarPdf(context: Context, onComplete: (String) -> Unit, onError: (String) -> Unit) {
        val currentPedido = _pedido.value
        if (currentPedido == null) {
            onError("No se pudo cargar la información del pedido.")
            return
        }
        generarPdfConPedido(context, currentPedido, onComplete, onError)
    }

    fun generarPdfConPedido(
        context: Context, 
        currentPedido: Pedido, 
        onComplete: (String) -> Unit, 
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            Log.d("PDF_GEN", "Iniciando generación para pedido: ${currentPedido.id}")
            _isGeneratingPdf.value = true
            try {
                val fileName = "Boleta_ShopPe_${currentPedido.id}.pdf"
                
                withContext(Dispatchers.IO) {
                    val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    if (documentsDir != null && !documentsDir.exists()) {
                        documentsDir.mkdirs()
                        Log.d("PDF_GEN", "Directorio creado: ${documentsDir.absolutePath}")
                    }
                    
                    val file = File(documentsDir, fileName)
                    Log.d("PDF_GEN", "Archivo destino: ${file.absolutePath}")
                    
                    val outputStream = FileOutputStream(file)

                    val writer = PdfWriter(outputStream)
                    val pdf = PdfDocument(writer)
                    val document = Document(pdf)

                    // --- HEADER ---
                    val headerTable = Table(UnitValue.createPointArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
                    
                    // Left Column (Emitter)
                    val leftCell = Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    leftCell.add(Paragraph("ShopPe S.A.").setBold().setFontSize(18f))
                    leftCell.add(Paragraph("RUC: 20123456789"))
                    leftCell.add(Paragraph("Calle ShopPe 123, San Isidro, Lima"))
                    headerTable.addCell(leftCell)
                    
                    // Right Column (Boleta Info)
                    val rightCell = Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                    rightCell.add(Paragraph("BOLETA DE VENTA ELECTRÓNICA").setBold().setFontSize(18f))
                    rightCell.add(Paragraph("Serie: B001"))
                    rightCell.add(Paragraph("Nro: ${currentPedido.numComprobante.ifEmpty { currentPedido.id.takeLast(8).uppercase() }}"))
                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(currentPedido.fecha))
                    rightCell.add(Paragraph("Fecha: $dateStr"))
                    headerTable.addCell(rightCell)
                    
                    document.add(headerTable)
                    document.add(Paragraph("\n"))

                    // --- CLIENT INFO ---
                    document.add(Paragraph("INFORMACIÓN DEL CLIENTE").setBold().setFontSize(12f))
                    val clientTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
                    
                    val nombreText = if (currentPedido.clienteNombre.isNullOrBlank()) "No especificado" else currentPedido.clienteNombre
                    val dniText = if (currentPedido.dni.isNullOrBlank()) "No especificado" else currentPedido.dni
                    val direccionText = if (currentPedido.direccion.isNullOrBlank()) "No especificada" else currentPedido.direccion

                    clientTable.addCell(Cell().add(Paragraph("Nombre: $nombreText").setFontSize(10f)))
                    clientTable.addCell(Cell().add(Paragraph("DNI: $dniText").setFontSize(10f)))
                    clientTable.addCell(Cell(1, 2).add(Paragraph("Dirección de Envío: $direccionText").setFontSize(10f)))
                    document.add(clientTable)
                    document.add(Paragraph("\n"))

                    // --- ITEMS TABLE ---
                    val itemsTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 50f, 10f, 15f, 15f))).useAllAvailableWidth()
                    itemsTable.addHeaderCell(Cell().add(Paragraph("Cant").setBold()))
                    itemsTable.addHeaderCell(Cell().add(Paragraph("Descripción").setBold()))
                    itemsTable.addHeaderCell(Cell().add(Paragraph("U.M.").setBold()))
                    itemsTable.addHeaderCell(Cell().add(Paragraph("Prec. Unit").setBold()))
                    itemsTable.addHeaderCell(Cell().add(Paragraph("Subtotal").setBold()))

                    currentPedido.items.forEach { item ->
                        itemsTable.addCell(Cell().add(Paragraph(item.cantidad.toString())))
                        itemsTable.addCell(Cell().add(Paragraph(item.nombre)))
                        itemsTable.addCell(Cell().add(Paragraph("Unidad")))
                        itemsTable.addCell(Cell().add(Paragraph("S/ ${String.format("%.2f", item.precio)}")))
                        itemsTable.addCell(Cell().add(Paragraph("S/ ${String.format("%.2f", item.precio * item.cantidad)}")))
                    }
                    document.add(itemsTable)

                    // --- TOTALS ---
                    document.add(Paragraph("\n"))
                    val totalPara = Paragraph("TOTAL A PAGAR: S/ ${String.format("%.2f", currentPedido.total)}")
                        .setBold()
                        .setFontSize(16f)
                        .setTextAlignment(TextAlignment.RIGHT)
                    document.add(totalPara)

                    // --- QR CODE ---
                    try {
                        val qrData = "PedidoID: ${currentPedido.id}\nComprobante: ${currentPedido.numComprobante}\nTotal: S/ ${currentPedido.total}\nFecha: $dateStr"
                        val qrBytes = generateQRCodeByteArray(qrData, 200, 200)
                        if (qrBytes != null) {
                            val imageData = com.itextpdf.io.image.ImageDataFactory.create(qrBytes)
                            val qrImage = com.itextpdf.layout.element.Image(imageData)
                                .setHorizontalAlignment(com.itextpdf.layout.property.HorizontalAlignment.CENTER)
                                .setWidth(100f)
                            document.add(Paragraph("\n"))
                            document.add(qrImage)
                            document.add(Paragraph("Escanea para validar comprobante").setFontSize(8f).setTextAlignment(TextAlignment.CENTER))
                        }
                    } catch (e: Exception) {
                        Log.e("PDF_GEN", "Error al generar QR", e)
                    }
                    
                    document.add(Paragraph("\nGenerado por ShopPe SDK").setFontSize(8f).setTextAlignment(TextAlignment.CENTER))

                    document.close()
                    Log.d("PDF_GEN", "Documento cerrado exitosamente")
                    withContext(Dispatchers.Main) {
                        onComplete("Comprobante generado con éxito")
                    }
                }
            } catch (e: Exception) {
                Log.e("PDF_GEN", "Error generando PDF", e)
                withContext(Dispatchers.Main) {
                    onError("Error al generar boleta: ${e.localizedMessage}")
                }
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun abrirComprobante(context: Context, pedidoId: String) {
        val fileName = "Boleta_ShopPe_$pedidoId.pdf"
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val targetFile = File(documentsDir, fileName)

        if (targetFile.exists()) {
            abrirArchivoPDF(context, targetFile)
        } else {
            Log.e("PDF_OPEN", "Archivo no encontrado")
            Toast.makeText(context, "El archivo aún no se ha generado o no existe.", Toast.LENGTH_SHORT).show()
        }
    }

    fun abrirArchivoPDF(context: Context, file: File) {
        Log.d("PDF_OPEN", "Intentando abrir: ${file.absolutePath}")
        try {
            val uri = FileProvider.getUriForFile(context, "com.idat.tiendonline.fileprovider", file)
            Log.d("PDF_OPEN", "URI generada: $uri")
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir Boleta con..."))
        } catch (e: Exception) {
            Log.e("PDF_OPEN", "Error al abrir PDF", e)
            Toast.makeText(context, "Error al abrir: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
