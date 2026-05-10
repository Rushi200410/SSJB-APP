package com.example.ssjb.util;

import android.content.Context;

import com.example.ssjb.R;
import com.example.ssjb.data.AttendanceSession;
import com.example.ssjb.data.SessionStudentRow;
import com.example.ssjb.data.Student;
import com.example.ssjb.data.StudentAttendanceStat;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

public final class PdfGenerator {
    private PdfGenerator() {
    }

    public static File generate(Context context, AttendanceSession session, List<SessionStudentRow> rows) throws Exception {
        File out = new File(context.getCacheDir(), "attendance_" + session.id + ".pdf");
        Document doc = new Document(PageSize.A4.rotate(), 22f, 22f, 20f, 18f);
        PdfWriter.getInstance(doc, new FileOutputStream(out));
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font bodyFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

        addTitle(doc, "CLASS".equals(session.sessionType) ? "Attendance Summary" : "Call Attendance Summary", titleFont);

        PdfPTable meta = new PdfPTable(new float[]{1.2f, 2.8f, 1.1f, 1.9f});
        meta.setWidthPercentage(100);
        meta.setSpacingBefore(6f);
        meta.addCell(metaLabelCell("Type", headerFont));
        meta.addCell(metaValueCell("CLASS".equals(session.sessionType) ? "Class" : "Call", bodyFont));
        meta.addCell(metaLabelCell("Date", headerFont));
        meta.addCell(metaValueCell(formatDate(session.dateIso) + " (" + safe(session.dayLabel) + ")", bodyFont));
        meta.addCell(metaLabelCell("Time", headerFont));
        meta.addCell(metaValueCell(safe(session.timeLabel), bodyFont));
        meta.addCell(metaLabelCell("Details", headerFont));
        meta.addCell(metaValueCell(buildSessionDetail(session), bodyFont));
        doc.add(meta);

        int present = 0;
        for (SessionStudentRow row : rows) {
            if (row.present) {
                present++;
            }
        }
        PdfPTable summary = new PdfPTable(new float[]{1f, 1f, 1f, 1f});
        summary.setWidthPercentage(100);
        summary.setSpacingBefore(8f);
        summary.addCell(summaryCell("Members", String.valueOf(rows.size()), headerFont, bodyFont));
        summary.addCell(summaryCell("Present", String.valueOf(present), headerFont, bodyFont));
        summary.addCell(summaryCell("Absent", String.valueOf(rows.size() - present), headerFont, bodyFont));
        summary.addCell(summaryCell("Attendance", String.format(Locale.US, "%.1f%%", rows.isEmpty() ? 0 : (present * 100f / rows.size())), headerFont, bodyFont));
        doc.add(summary);

        PdfPTable roster = new PdfPTable(new float[]{3.8f, 1.3f, 3.8f, 1.3f, 3.8f, 1.3f});
        roster.setWidthPercentage(100);
        roster.setSpacingBefore(10f);
        roster.setSplitLate(false);
        roster.addCell(tableHeader("Student", headerFont));
        roster.addCell(tableHeader("Status", headerFont));
        roster.addCell(tableHeader("Student", headerFont));
        roster.addCell(tableHeader("Status", headerFont));
        roster.addCell(tableHeader("Student", headerFont));
        roster.addCell(tableHeader("Status", headerFont));

        for (int i = 0; i < rows.size(); i += 3) {
            for (int slot = 0; slot < 3; slot++) {
                int index = i + slot;
                if (index < rows.size()) {
                    SessionStudentRow row = rows.get(index);
                    roster.addCell(dataCell(row.name, bodyFont));
                    roster.addCell(statusCell(row.present ? "Present" : "Absent", row.present, bodyFont));
                } else {
                    roster.addCell(emptyCell());
                    roster.addCell(emptyCell());
                }
            }
        }
        doc.add(roster);

        Paragraph note = new Paragraph(
                "Generated from SSJB attendance monitor",
                subtitleFont
        );
        note.setAlignment(Element.ALIGN_RIGHT);
        note.setSpacingBefore(6f);
        doc.add(note);
        doc.close();
        return out;
    }

    public static File generateStudentSummary(Context context, Student student, StudentAttendanceStat stat) throws Exception {
        File out = new File(context.getCacheDir(), "student_" + student.id + ".pdf");
        Document doc = new Document(PageSize.A4, 24f, 24f, 22f, 20f);
        PdfWriter.getInstance(doc, new FileOutputStream(out));
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
        Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        addTitle(doc, "Student Profile", titleFont);
        Paragraph subtitle = new Paragraph("Clean summary for sharing and record keeping", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(8f);
        doc.add(subtitle);

        PdfPTable table = new PdfPTable(new float[]{1.3f, 2.7f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(6f);
        addDetailRow(table, "Name", joinName(student), labelFont, valueFont);
        addDetailRow(table, "Middle name", safe(student.middleName), labelFont, valueFont);
        addDetailRow(table, "Surname", safe(student.surname), labelFont, valueFont);
        addDetailRow(table, "Phone", safe(student.phoneNumber), labelFont, valueFont);
        addDetailRow(table, "Instrument", safe(student.instrument), labelFont, valueFont);
        addDetailRow(table, "Address", safe(student.address), labelFont, valueFont);
        addDetailRow(table, "Joining date", formatDate(student.joiningDateIso), labelFont, valueFont);
        addDetailRow(table, "Knowledge", safe(student.knowledgeLevel), labelFont, valueFont);
        addDetailRow(table, "Last updated", formatDateTime(student.lastUpdatedIso), labelFont, valueFont);
        addDetailRow(table, "Balance", student.balance == null ? "-" : String.format(Locale.US, "%.2f", student.balance), labelFont, valueFont);
        doc.add(table);

        int total = stat == null ? 0 : stat.totalSessions;
        int present = stat == null ? 0 : stat.presentCount;
        double percent = total == 0 ? 0 : (present * 100.0 / total);

        PdfPTable summary = new PdfPTable(new float[]{1f, 1f, 1f, 1f});
        summary.setWidthPercentage(100);
        summary.setSpacingBefore(10f);
        summary.addCell(summaryCell("Sessions", String.valueOf(total), labelFont, valueFont));
        summary.addCell(summaryCell("Present", String.valueOf(present), labelFont, valueFont));
        summary.addCell(summaryCell("Attendance", String.format(Locale.US, "%.1f%%", percent), labelFont, valueFont));
        summary.addCell(summaryCell("Status", total == 0 ? "No attendance yet" : "Active", labelFont, valueFont));
        doc.add(summary);

        Paragraph footer = new Paragraph(
                "Student attendance details generated by SSJB",
                subtitleFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10f);
        doc.add(footer);
        doc.close();
        return out;
    }

    private static void addTitle(Document doc, String title, Font titleFont) throws Exception {
        Paragraph p = new Paragraph(title, titleFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(4f);
        doc.add(p);
    }

    private static PdfPCell tableHeader(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderWidth(0.8f);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private static PdfPCell dataCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
        cell.setBorderWidth(0.5f);
        cell.setPadding(5f);
        return cell;
    }

    private static PdfPCell statusCell(String text, boolean present, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderWidth(0.5f);
        cell.setPadding(5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private static PdfPCell emptyCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0f);
        return cell;
    }

    private static PdfPCell metaLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderWidth(0.6f);
        cell.setPadding(6f);
        return cell;
    }

    private static PdfPCell metaValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
        cell.setBorderWidth(0.6f);
        cell.setPadding(6f);
        cell.setColspan(1);
        return cell;
    }

    private static PdfPCell summaryCell(String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(0.6f);
        cell.setPadding(7f);
        Paragraph p1 = new Paragraph(label, labelFont);
        Paragraph p2 = new Paragraph(value, valueFont);
        p2.setSpacingBefore(2f);
        cell.addElement(p1);
        cell.addElement(p2);
        return cell;
    }

    private static void addDetailRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorderWidth(0.6f);
        labelCell.setPadding(6f);
        PdfPCell valueCell = new PdfPCell(new Phrase(safe(value), valueFont));
        valueCell.setBorderWidth(0.6f);
        valueCell.setPadding(6f);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static String buildSessionDetail(AttendanceSession session) {
        if ("CLASS".equals(session.sessionType)) {
            return safe(session.classMode);
        }
        StringBuilder builder = new StringBuilder();
        append(builder, session.callTitle);
        append(builder, session.location);
        append(builder, session.callDescription);
        return builder.length() == 0 ? "-" : builder.toString();
    }

    private static void append(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" | ");
        }
        builder.append(value.trim());
    }

    private static String joinName(Student student) {
        StringBuilder builder = new StringBuilder();
        appendName(builder, student.name);
        appendName(builder, student.middleName);
        appendName(builder, student.surname);
        return builder.length() == 0 ? "-" : builder.toString();
    }

    private static void appendName(StringBuilder builder, String part) {
        if (part == null || part.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(part.trim());
    }

    private static String formatDate(String isoDate) {
        return DateUtils.formatDisplayDate(isoDate);
    }

    private static String formatDateTime(String isoDateTime) {
        return DateUtils.formatDisplayDateTime(isoDateTime);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
