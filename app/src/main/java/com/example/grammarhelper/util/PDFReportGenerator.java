package com.example.grammarhelper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import com.example.grammarhelper.R;
import com.example.grammarhelper.database.DatabaseHelper;
import com.example.grammarhelper.database.ErrorLogDAO;
import com.example.grammarhelper.database.SessionDAO;
import com.example.grammarhelper.model.GrammarError;
import com.example.grammarhelper.model.Session;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PDFReportGenerator {

    private Context context;
    private SessionDAO sessionDAO;
    private ErrorLogDAO errorLogDAO;
    private DatabaseHelper dbHelper;

    public PDFReportGenerator(Context context) {
        this.context = context;
        this.sessionDAO = new SessionDAO(context);
        this.errorLogDAO = new ErrorLogDAO(context);
        this.dbHelper = new DatabaseHelper(context);
    }

    public File generateReport() throws Exception {
        sessionDAO.open();
        errorLogDAO.open();

        // Get CURRENT date and time
        Date currentDate = new Date();

        // Create filename with current timestamp (24-hour format for filename)
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = fileDateFormat.format(currentDate);
        String fileName = "GrammarHelper_Report_" + timestamp + ".pdf";

        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        // Initialize PDF writer
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        // Add fonts
        PdfFont boldFont = PdfFontFactory.createFont();
        PdfFont regularFont = PdfFontFactory.createFont();

        // ===== TITLE SECTION =====
        Paragraph title = new Paragraph("Grammar Helper - Progress Report")
                .setFont(boldFont)
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(37, 99, 235));
        document.add(title);

        // 使用马来西亚时区 (UTC+8)
        TimeZone malaysiaTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(malaysiaTimeZone);

        String dateStr = dateFormat.format(currentDate);
        Paragraph datePara = new Paragraph("Generated: " + dateStr)
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(datePara);

        document.add(new Paragraph("\n"));

        // ===== USER INFO SECTION =====
        Paragraph section1 = new Paragraph("User Information")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(new DeviceRgb(37, 99, 235));
        document.add(section1);

        SharedPreferences prefs = context.getSharedPreferences("GrammarHelperPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Guest User");

        Table userTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        userTable.setWidth(UnitValue.createPercentValue(100));

        addUserRow(userTable, "Name:", userName, regularFont);
        addUserRow(userTable, "Report ID:", "GH-" + timestamp, regularFont);
        addUserRow(userTable, "Generated:", dateStr, regularFont);

        document.add(userTable);
        document.add(new Paragraph("\n"));

        // ===== STATISTICS SECTION =====
        Paragraph section2 = new Paragraph("Learning Statistics")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(new DeviceRgb(37, 99, 235));
        document.add(section2);

        // Get statistics
        List<Session> sessions = sessionDAO.getAllSessions();
        int sessionCount = sessions.size();
        int totalFixes = errorLogDAO.getTotalFixedCount();
        int streakDays = sessionDAO.getStreakDays();

        // Calculate average score
        int totalScore = 0;
        for (Session s : sessions) {
            totalScore += s.grammarScore;
        }
        int avgScore = sessionCount > 0 ? totalScore / sessionCount : 0;

        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        statsTable.setWidth(UnitValue.createPercentValue(100));

        addStatRow(statsTable, "📝 Total Sessions:", String.valueOf(sessionCount), regularFont);
        addStatRow(statsTable, "✅ Grammar Errors Fixed:", String.valueOf(totalFixes), regularFont);
        addStatRow(statsTable, "🔥 Current Streak:", streakDays + " days", regularFont);
        addStatRow(statsTable, "📊 Average Grammar Score:", avgScore + "/100", regularFont);

        document.add(statsTable);
        document.add(new Paragraph("\n"));

        // ===== ERROR BREAKDOWN SECTION =====
        Paragraph section3 = new Paragraph("Error Type Breakdown")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(new DeviceRgb(37, 99, 235));
        document.add(section3);

        Table errorTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
        errorTable.setWidth(UnitValue.createPercentValue(100));
        errorTable.setBackgroundColor(new DeviceRgb(240, 240, 240));

        // Table headers
        Cell header1 = new Cell().add(new Paragraph("Error Type").setFont(boldFont));
        Cell header2 = new Cell().add(new Paragraph("Count").setFont(boldFont));
        errorTable.addCell(header1);
        errorTable.addCell(header2);

        // Get error distribution
        Cursor errorCursor = errorLogDAO.getErrorDistribution();
        if (errorCursor != null && errorCursor.moveToFirst()) {
            do {
                String type = errorCursor.getString(0);
                int count = errorCursor.getInt(1);
                errorTable.addCell(new Cell().add(new Paragraph(type).setFont(regularFont)));
                errorTable.addCell(new Cell().add(new Paragraph(String.valueOf(count)).setFont(regularFont)));
            } while (errorCursor.moveToNext());
            errorCursor.close();
        } else {
            errorTable.addCell(new Cell().add(new Paragraph("No errors recorded yet").setFont(regularFont)));
            errorTable.addCell(new Cell().add(new Paragraph("0").setFont(regularFont)));
        }

        document.add(errorTable);
        document.add(new Paragraph("\n"));

        // ===== TOP MISTAKES SECTION =====
        Paragraph section4 = new Paragraph("Top 5 Most Common Mistakes")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(new DeviceRgb(37, 99, 235));
        document.add(section4);

        Table mistakesTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
        mistakesTable.setWidth(UnitValue.createPercentValue(100));
        mistakesTable.setBackgroundColor(new DeviceRgb(240, 240, 240));

        Cell mHeader1 = new Cell().add(new Paragraph("Mistake Type").setFont(boldFont));
        Cell mHeader2 = new Cell().add(new Paragraph("Frequency").setFont(boldFont));
        mistakesTable.addCell(mHeader1);
        mistakesTable.addCell(mHeader2);

        Cursor topCursor = errorLogDAO.getTopMistakes();
        if (topCursor != null && topCursor.moveToFirst()) {
            do {
                String subtype = topCursor.getString(0);
                int count = topCursor.getInt(1);
                mistakesTable.addCell(new Cell().add(new Paragraph(subtype).setFont(regularFont)));
                mistakesTable.addCell(new Cell().add(new Paragraph(String.valueOf(count)).setFont(regularFont)));
            } while (topCursor.moveToNext());
            topCursor.close();
        } else {
            mistakesTable.addCell(new Cell().add(new Paragraph("No mistakes recorded").setFont(regularFont)));
            mistakesTable.addCell(new Cell().add(new Paragraph("0").setFont(regularFont)));
        }

        document.add(mistakesTable);
        document.add(new Paragraph("\n"));

        // ===== RECENT SESSIONS SECTION =====
        Paragraph section5 = new Paragraph("Recent Writing Sessions")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(new DeviceRgb(37, 99, 235));
        document.add(section5);

        Table sessionsTable = new Table(UnitValue.createPercentArray(new float[]{30, 25, 25, 20}));
        sessionsTable.setWidth(UnitValue.createPercentValue(100));
        sessionsTable.setBackgroundColor(new DeviceRgb(240, 240, 240));

        Cell sHeader1 = new Cell().add(new Paragraph("Date").setFont(boldFont));
        Cell sHeader2 = new Cell().add(new Paragraph("Score").setFont(boldFont));
        Cell sHeader3 = new Cell().add(new Paragraph("Word Count").setFont(boldFont));
        Cell sHeader4 = new Cell().add(new Paragraph("Tone").setFont(boldFont));
        sessionsTable.addCell(sHeader1);
        sessionsTable.addCell(sHeader2);
        sessionsTable.addCell(sHeader3);
        sessionsTable.addCell(sHeader4);

        // Show last 10 sessions
        int showCount = Math.min(10, sessions.size());
        for (int i = 0; i < showCount; i++) {
            Session s = sessions.get(i);
            String sessionDate = s.timestamp != null ? s.timestamp.substring(0, 16) : "N/A";
            sessionsTable.addCell(new Cell().add(new Paragraph(sessionDate).setFont(regularFont)));
            sessionsTable.addCell(new Cell().add(new Paragraph(String.valueOf(s.grammarScore)).setFont(regularFont)));
            sessionsTable.addCell(new Cell().add(new Paragraph(String.valueOf(s.wordCount)).setFont(regularFont)));
            sessionsTable.addCell(new Cell().add(new Paragraph(s.toneDetected != null ? s.toneDetected : "N/A").setFont(regularFont)));
        }

        document.add(sessionsTable);
        document.add(new Paragraph("\n"));

        // ===== RECOMMENDATIONS SECTION =====
        Paragraph section6 = new Paragraph("Personalized Recommendations")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(new DeviceRgb(37, 99, 235));
        document.add(section6);

        StringBuilder recommendations = new StringBuilder();
        if (streakDays < 7) {
            recommendations.append("• Keep practicing daily to build your learning streak!\n");
        }
        if (avgScore < 70) {
            recommendations.append("• Focus on basic grammar rules. Try our beginner lessons.\n");
        }
        if (totalFixes < 10) {
            recommendations.append("• Review the error suggestions carefully to learn from mistakes.\n");
        }
        if (recommendations.length() == 0) {
            recommendations.append("• Great progress! Continue with advanced grammar topics.\n");
            recommendations.append("• Try writing longer texts to challenge yourself.\n");
        }

        Paragraph recommendationsPara = new Paragraph(recommendations.toString())
                .setFont(regularFont)
                .setFontSize(11);
        document.add(recommendationsPara);

        // ===== FOOTER =====
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Generated by Grammar Helper App - Keep improving your writing skills!")
                .setFont(regularFont)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(128, 128, 128));
        document.add(footer);

        // Close document
        document.close();
        pdfDoc.close();

        sessionDAO.close();
        errorLogDAO.close();

        return pdfFile;
    }

    private void addUserRow(Table table, String label, String value, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(font)));
        table.addCell(new Cell().add(new Paragraph(value).setFont(font)));
    }

    private void addStatRow(Table table, String label, String value, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(font)));
        table.addCell(new Cell().add(new Paragraph(value).setFont(font)));
    }
}