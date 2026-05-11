package com.example.ssjb.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;

import com.example.ssjb.data.AttendanceSession;
import com.example.ssjb.data.SessionStudentRow;
import com.example.ssjb.data.Student;
import com.example.ssjb.data.StudentAttendanceStat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

public final class PdfGenerator {
    private static final int PAGE_WIDTH_LANDSCAPE = 842;
    private static final int PAGE_HEIGHT_LANDSCAPE = 595;
    private static final int PAGE_WIDTH_PORTRAIT = 595;
    private static final int PAGE_HEIGHT_PORTRAIT = 842;
    private static final int MARGIN = 24;

    private PdfGenerator() {
    }

    public static File generate(Context context, AttendanceSession session, List<SessionStudentRow> rows) throws Exception {
        File out = new File(context.getCacheDir(), "attendance_" + session.id + ".pdf");
        PdfDocument document = new PdfDocument();
        try {
            PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH_LANDSCAPE, PAGE_HEIGHT_LANDSCAPE, 1).create());
            Canvas canvas = page.getCanvas();

            Paint titlePaint = paint(24, true, Color.rgb(28, 45, 78));
            Paint bodyPaint = paint(11, false, Color.rgb(42, 48, 60));
            Paint smallPaint = paint(9, false, Color.rgb(96, 104, 118));
            Paint borderPaint = paint(1, false, Color.rgb(210, 218, 229));
            borderPaint.setStyle(Paint.Style.STROKE);

            int x = MARGIN;
            int y = MARGIN;
            String title = "CLASS".equals(session.sessionType) ? "Class" : "Call";
            canvas.drawText(title, x, y + 22, titlePaint);
            y += 30;
            canvas.drawText("Date: " + DateUtils.formatDisplayDate(session.dateIso), x, y + 12, smallPaint);
            y += 18;

            if ("CALL".equals(session.sessionType)) {
                canvas.drawText("Time: " + safe(session.timeLabel), x, y + 12, smallPaint);
                y += 18;
                canvas.drawText("Description: " + safe(session.callDescription), x, y + 12, bodyPaint);
                y += 18;
            } else {
                canvas.drawText("Mode: " + safe(session.classMode), x, y + 12, bodyPaint);
                y += 18;
            }

            String statusLabel = attendanceLabel(session);
            canvas.drawText("Attendance: " + statusLabel, x, y + 12, bodyPaint);
            y += 26;

            int panelTop = y + 4;
            int panelHeight = PAGE_HEIGHT_LANDSCAPE - panelTop - MARGIN - 18;
            drawAttendanceColumns(canvas, rows, borderPaint, titlePaint, bodyPaint, smallPaint, x, panelTop, PAGE_WIDTH_LANDSCAPE - (MARGIN * 2), panelHeight);

            Paint footerPaint = paint(9, false, Color.rgb(88, 96, 110));
            canvas.drawText("Every session matters. Keep showing up, keep growing, and keep the music moving.", x, PAGE_HEIGHT_LANDSCAPE - 10, footerPaint);

            document.finishPage(page);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                document.writeTo(fos);
            }
            return out;
        } finally {
            document.close();
        }
    }

    public static File generateStudentSummary(Context context, Student student, StudentAttendanceStat stat) throws Exception {
        File out = new File(context.getCacheDir(), "student_" + student.id + ".pdf");
        PdfDocument document = new PdfDocument();
        try {
            PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH_PORTRAIT, PAGE_HEIGHT_PORTRAIT, 1).create());
            Canvas canvas = page.getCanvas();

            Paint titlePaint = paint(22, true, Color.rgb(28, 45, 78));
            Paint bodyPaint = paint(11, false, Color.rgb(42, 48, 60));
            Paint smallPaint = paint(9, false, Color.rgb(96, 104, 118));
            Paint borderPaint = paint(1, false, Color.rgb(210, 218, 229));
            borderPaint.setStyle(Paint.Style.STROKE);
            Paint fillPaint = paint(0, false, Color.rgb(246, 249, 252));
            fillPaint.setStyle(Paint.Style.FILL);

            int x = MARGIN;
            int y = MARGIN;
            canvas.drawText("Student Profile", x, y + 22, titlePaint);
            y += 32;
            canvas.drawText("Clean summary for sharing", x, y, smallPaint);
            y += 20;

            drawRoundedBox(canvas, fillPaint, borderPaint, x, y, PAGE_WIDTH_PORTRAIT - (MARGIN * 2), 340);
            int rowX = x + 12;
            int rowY = y + 28;
            int rowGap = 25;
            drawLabelValue(canvas, smallPaint, bodyPaint, "Name", joinName(student), rowX, rowY);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Middle name", safe(student.middleName), rowX, rowY += rowGap);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Surname", safe(student.surname), rowX, rowY += rowGap);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Phone", safe(student.phoneNumber), rowX, rowY += rowGap);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Instrument", safe(student.instrument), rowX, rowY += rowGap);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Address", safe(student.address), rowX, rowY += rowGap);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Joining date", DateUtils.formatDisplayDate(student.joiningDateIso), rowX, rowY += rowGap);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Knowledge", safe(student.knowledgeLevel), rowX, rowY += rowGap);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Last updated", DateUtils.formatDisplayDateTime(student.lastUpdatedIso), rowX, rowY += rowGap);
            drawLabelValue(canvas, smallPaint, bodyPaint, "Balance", student.balance == null ? "-" : String.format(Locale.US, "%.2f", student.balance), rowX, rowY += rowGap);

            int total = stat == null ? 0 : stat.totalSessions;
            int present = stat == null ? 0 : stat.presentCount;
            double percent = total == 0 ? 0 : (present * 100.0 / total);

            int summaryTop = y + 356;
            drawRoundedBox(canvas, fillPaint, borderPaint, x, summaryTop, PAGE_WIDTH_PORTRAIT - (MARGIN * 2), 88);
            canvas.drawText("Attendance", x + 12, summaryTop + 20, smallPaint);
            canvas.drawText(String.format(Locale.US, "%.1f%%", percent), x + 12, summaryTop + 46, bodyPaint);
            canvas.drawText("Sessions", x + 180, summaryTop + 20, smallPaint);
            canvas.drawText(String.valueOf(total), x + 180, summaryTop + 46, bodyPaint);
            canvas.drawText("Present", x + 290, summaryTop + 20, smallPaint);
            canvas.drawText(String.valueOf(present), x + 290, summaryTop + 46, bodyPaint);
            canvas.drawText("Status", x + 400, summaryTop + 20, smallPaint);
            canvas.drawText(total == 0 ? "No attendance yet" : "Active", x + 400, summaryTop + 46, bodyPaint);

            canvas.drawText("Keep growing - every little step matters.", x, PAGE_HEIGHT_PORTRAIT - MARGIN, smallPaint);

            document.finishPage(page);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                document.writeTo(fos);
            }
            return out;
        } finally {
            document.close();
        }
    }

    private static void drawAttendanceColumns(Canvas canvas, List<SessionStudentRow> rows, Paint borderPaint, Paint titlePaint, Paint bodyPaint, Paint smallPaint, int x, int y, int width, int height) {
        if (rows.isEmpty()) {
            canvas.drawText("No attendance rows yet.", x, y + 18, bodyPaint);
            return;
        }

        int columns = Math.min(3, Math.max(1, (int) Math.ceil(rows.size() / 32.0)));
        int gap = 10;
        int columnWidth = (width - ((columns - 1) * gap)) / columns;
        int rowsPerColumn = (int) Math.ceil(rows.size() / (double) columns);
        int rowHeight = 11;
        int headerHeight = 16;
        int columnInnerPadding = 8;
        Paint headerPaint = paint(9, true, Color.rgb(28, 45, 78));

        for (int column = 0; column < columns; column++) {
            int start = column * rowsPerColumn;
            int end = Math.min(rows.size(), start + rowsPerColumn);
            if (start >= end) {
                continue;
            }

            int colX = x + (column * (columnWidth + gap));
            int colY = y;
            int rowsInColumn = end - start;
            int colHeight = headerHeight + 14 + (rowsInColumn * rowHeight) + 10;
            drawRoundedBox(canvas, null, borderPaint, colX, colY, columnWidth, Math.min(height, colHeight));
            canvas.drawText("Student", colX + columnInnerPadding, colY + 13, headerPaint);
            canvas.drawText("Status", colX + columnWidth - 46, colY + 13, headerPaint);

            int rowY = colY + headerHeight + 7;
            for (int i = start; i < end; i++) {
                SessionStudentRow row = rows.get(i);
                boolean present = row.present;
                Paint rowFill = paint(0, false, present ? Color.rgb(232, 245, 233) : Color.rgb(250, 236, 236));
                rowFill.setStyle(Paint.Style.FILL);
                drawRoundedBox(canvas, rowFill, borderPaint, colX + 2, rowY - 8, columnWidth - 4, 11);

                canvas.drawText(truncate(row.name, 24), colX + columnInnerPadding, rowY, bodyPaint);
                canvas.drawText(present ? "Present" : "Absent", colX + columnWidth - 56, rowY, bodyPaint);
                rowY += rowHeight;
            }
        }
    }

    private static void drawLabelValue(Canvas canvas, Paint labelPaint, Paint valuePaint, String label, String value, int x, int y) {
        canvas.drawText(label + ":", x, y, labelPaint);
        drawWrappedText(canvas, valuePaint, value, x + 96, y, PAGE_WIDTH_PORTRAIT - (MARGIN * 2) - 112, 14);
    }

    private static void drawWrappedText(Canvas canvas, Paint paint, String text, int x, int y, int width, int lineHeight) {
        String remaining = safe(text);
        int currentY = y;
        while (!remaining.isEmpty()) {
            int count = paint.breakText(remaining, true, width, null);
            if (count <= 0) {
                break;
            }
            String line = remaining.substring(0, count).trim();
            if (line.isEmpty() && remaining.length() > count) {
                remaining = remaining.substring(count).trim();
                continue;
            }
            canvas.drawText(line, x, currentY, paint);
            currentY += lineHeight;
            remaining = remaining.substring(Math.min(count, remaining.length())).trim();
        }
    }

    private static void drawRoundedBox(Canvas canvas, Paint fillPaint, Paint borderPaint, int x, int y, int width, int height) {
        RectF rect = new RectF(x, y, x + width, y + height);
        if (fillPaint != null) {
            canvas.drawRoundRect(rect, 16f, 16f, fillPaint);
        }
        if (borderPaint != null) {
            canvas.drawRoundRect(rect, 16f, 16f, borderPaint);
        }
    }

    private static Paint paint(float textSize, boolean bold, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setFakeBoldText(bold);
        return paint;
    }

    private static String attendanceLabel(AttendanceSession session) {
        if ("CALL".equals(session.sessionType)) {
            return "Present / Absent";
        }
        return isSirClass(session.classMode) ? "Sir Present / Absent" : "Practice Present / Absent";
    }

    private static boolean isSirClass(String classMode) {
        return classMode != null && classMode.toLowerCase(Locale.US).contains("sir");
    }

    private static String joinName(Student student) {
        StringBuilder builder = new StringBuilder();
        append(builder, student.name);
        append(builder, student.middleName);
        append(builder, student.surname);
        return builder.length() == 0 ? "-" : builder.toString();
    }

    private static void append(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }

    private static String truncate(String value, int maxChars) {
        String safe = safe(value);
        if (safe.length() <= maxChars) {
            return safe;
        }
        return safe.substring(0, Math.max(0, maxChars - 3)) + "...";
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
