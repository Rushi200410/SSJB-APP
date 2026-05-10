package com.example.ssjb.util;

import android.content.Context;

import com.example.ssjb.data.AttendanceSession;
import com.example.ssjb.data.SessionStudentRow;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public final class PdfGenerator {
    private PdfGenerator() {
    }

    public static File generate(Context context, AttendanceSession session, List<SessionStudentRow> rows) throws Exception {
        File out = new File(context.getCacheDir(), "attendance_" + session.id + ".pdf");
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(out));
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        String heading = "CLASS".equals(session.sessionType) ? "Band Class Attendance" : "Band Call Attendance";
        Paragraph title = new Paragraph(heading, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new Paragraph(" "));

        String meta = session.dateIso + " (" + session.dayLabel + ")  |  " + session.timeLabel;
        Paragraph metaLine = new Paragraph(meta, subtitleFont);
        metaLine.setAlignment(Element.ALIGN_CENTER);
        doc.add(metaLine);

        if ("CLASS".equals(session.sessionType)) {
            doc.add(new Paragraph("Type: " + (session.classMode == null ? "-" : session.classMode), subtitleFont));
        } else {
            doc.add(new Paragraph("Title: " + safe(session.callTitle), subtitleFont));
            doc.add(new Paragraph("Location: " + safe(session.location), subtitleFont));
            doc.add(new Paragraph("Description: " + safe(session.callDescription), subtitleFont));
        }

        doc.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(new float[]{4f, 2f});
        table.setWidthPercentage(100);
        table.addCell(headerCell("Student", headerFont, "CLASS".equals(session.sessionType)));
        table.addCell(headerCell("Status", headerFont, "CLASS".equals(session.sessionType)));

        int present = 0;
        for (SessionStudentRow row : rows) {
            if (row.present) {
                present++;
            }
            table.addCell(bodyCell(row.name, bodyFont));
            table.addCell(bodyCell(row.present ? "Present" : "Absent", bodyFont));
        }
        doc.add(table);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Present: " + present + " | Absent: " + (rows.size() - present), subtitleFont));
        doc.close();
        return out;
    }

    private static PdfPCell headerCell(String text, Font font, boolean classTheme) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8f);
        if (classTheme) {
            cell.setBackgroundColor(new java.awt.Color(222, 235, 255));
        } else {
            cell.setBackgroundColor(new java.awt.Color(228, 252, 237));
        }
        return cell;
    }

    private static PdfPCell bodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(7f);
        return cell;
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }
}
