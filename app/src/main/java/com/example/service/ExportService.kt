package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.calculation.FinancialEngine
import com.example.model.FinancialSummary
import com.example.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ExportService {

    fun generateCsv(
        context: Context,
        period: String,
        transactions: List<Transaction>
    ): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "Finora_Transactions_${period.replace("-", "_")}.csv")

        val sb = StringBuilder()
        sb.append("ID,Date,Type,Category,AmountMinorUnits,AmountFormatted,Currency,PaymentMethod,Merchant,Description,Recurring\n")

        for (tx in transactions) {
            val amountFormatted = (tx.amountMinorUnits.toDouble() / 100.0).toString()
            val desc = tx.description.replace(",", ";").replace("\n", " ")
            val merchant = (tx.merchant ?: "").replace(",", ";")
            val cat = tx.categoryNameSnapshot.replace(",", ";")
            sb.append("${tx.id},${tx.transactionDate},${tx.type},$cat,${tx.amountMinorUnits},$amountFormatted,${tx.currencyCode},${tx.paymentMethod},$merchant,$desc,${tx.isRecurring}\n")
        }

        file.writeText(sb.toString())
        return file
    }

    fun generatePdf(
        context: Context,
        summary: FinancialSummary,
        transactions: List<Transaction>,
        currencyCode: String = "PKR"
    ): File {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "Finora_Report_${summary.yearMonth.replace("-", "_")}.pdf")

        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard (595 x 842 pt)
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Header Background
        paint.color = Color.parseColor("#0B132B")
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Accent Line
        paint.color = Color.parseColor("#10B981")
        canvas.drawRect(0f, 96f, 595f, 100f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FINORA — FINANCIAL REPORT", 32f, 48f, paint)

        paint.textSize = 12f
        paint.color = Color.parseColor("#94A3B8")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Statement Period: ${summary.yearMonth}   |   Generated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}", 32f, 75f, paint)

        // Summary Metric Cards
        var startY = 120f
        val cardWidth = 120f
        val cardHeight = 60f
        val gap = 12f

        val metrics = listOf(
            Triple("TOTAL INCOME", FinancialEngine.formatMinorUnits(summary.totalIncome, currencyCode, includeDecimals = false), "#10B981"),
            Triple("TOTAL EXPENSES", FinancialEngine.formatMinorUnits(summary.totalExpenses, currencyCode, includeDecimals = false), "#F43F5E"),
            Triple("NET CASH FLOW", FinancialEngine.formatMinorUnits(summary.netCashFlow, currencyCode, includeDecimals = false), if (summary.netCashFlow >= 0) "#10B981" else "#F43F5E"),
            Triple("AVAILABLE BALANCE", FinancialEngine.formatMinorUnits(summary.availableBalance, currencyCode, includeDecimals = false), "#3B82F6")
        )

        for (i in metrics.indices) {
            val (label, value, accentColor) = metrics[i]
            val x = 32f + i * (cardWidth + gap)

            // Card background
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(x, startY, x + cardWidth, startY + cardHeight, 6f, 6f, paint)

            // Label
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(label, x + 8f, startY + 18f, paint)

            // Value
            paint.color = Color.parseColor(accentColor)
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(value, x + 8f, startY + 42f, paint)
        }

        // Category Breakdown Section
        startY = 205f
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Top Category Spending", 32f, startY, paint)

        startY += 15f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(32f, startY, 563f, startY, paint)

        startY += 18f
        val topCategories = summary.categoryBreakdown.take(5)
        if (topCategories.isEmpty()) {
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 10f
            canvas.drawText("No expenses recorded for this month.", 32f, startY, paint)
            startY += 20f
        } else {
            for (cat in topCategories) {
                paint.color = Color.parseColor("#334155")
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(cat.categoryName, 32f, startY, paint)

                val pctStr = String.format(java.util.Locale.US, "%.1f%%", cat.percentageOfTotal)
                canvas.drawText(pctStr, 350f, startY, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val amountStr = FinancialEngine.formatMinorUnits(cat.totalMinorUnits, currencyCode)
                canvas.drawText(amountStr, 470f, startY, paint)

                startY += 18f
            }
        }

        // Recent Transactions Section
        startY += 15f
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Transaction Activity (${summary.yearMonth})", 32f, startY, paint)

        startY += 15f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(32f, startY, 563f, startY, paint)

        // Table Header
        startY += 16f
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DATE", 32f, startY, paint)
        canvas.drawText("CATEGORY / DESCRIPTION", 110f, startY, paint)
        canvas.drawText("PAYMENT METHOD", 350f, startY, paint)
        canvas.drawText("AMOUNT", 470f, startY, paint)

        startY += 8f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(32f, startY, 563f, startY, paint)

        startY += 16f
        val txList = transactions.take(20) // Show up to 20 for single-page summary
        for (tx in txList) {
            paint.color = Color.parseColor("#334155")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            canvas.drawText(tx.transactionDate, 32f, startY, paint)

            val desc = if (tx.description.isNotBlank()) "${tx.categoryNameSnapshot} - ${tx.description}" else tx.categoryNameSnapshot
            val trimmedDesc = if (desc.length > 38) desc.take(35) + "..." else desc
            canvas.drawText(trimmedDesc, 110f, startY, paint)

            canvas.drawText(tx.paymentMethod, 350f, startY, paint)

            paint.color = Color.parseColor(if (tx.isIncome) "#10B981" else "#F43F5E")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val amtStr = (if (tx.isIncome) "+" else "-") + FinancialEngine.formatMinorUnits(tx.amountMinorUnits, currencyCode)
            canvas.drawText(amtStr, 470f, startY, paint)

            startY += 16f
            if (startY > 800f) break
        }

        // Footer Note
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Generated by Finora — Premium Personal Finance Tracker. Records stored in Firebase Firestore.", 32f, 825f, paint)

        doc.finishPage(page)

        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
