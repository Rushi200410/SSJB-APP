package com.example.ssjb.util;

import com.example.ssjb.data.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CsvImportUtil {
    private CsvImportUtil() {
    }

    public static List<Student> parseStudents(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String headerLine = bufferedReader.readLine();
        if (headerLine == null) {
            return new ArrayList<>();
        }

        List<String> headers = parseLine(headerLine);
        Map<String, Integer> headerIndex = buildIndex(headers);

        List<Student> students = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            List<String> columns = parseLine(line);
            Student student = parseStudentRow(columns, headerIndex);
            if (student != null) {
                students.add(student);
            }
        }
        return students;
    }

    private static Map<String, Integer> buildIndex(List<String> headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            map.put(normalize(headers.get(i)), i);
        }
        return map;
    }

    private static Student parseStudentRow(List<String> columns, Map<String, Integer> headerIndex) {
        String name = value(columns, headerIndex, "name");
        String middleName = value(columns, headerIndex, "middlename");
        String surname = value(columns, headerIndex, "surname");
        String phone = firstNonEmpty(columns, headerIndex, "phone", "phone number", "mobile", "mobile number");
        String instrument = firstNonEmpty(columns, headerIndex, "instrument", "instument");
        String address = value(columns, headerIndex, "address");
        String joiningDate = firstNonEmpty(columns, headerIndex, "joining date", "joiningdate");
        String knowledge = firstNonEmpty(columns, headerIndex, "knowledge level", "knowledge", "how much do they know");
        String balanceText = firstNonEmpty(columns, headerIndex, "balance");
        String lastUpdated = firstNonEmpty(columns, headerIndex, "last update date", "last updated", "lastupdated");

        if (name.isEmpty()) {
            String fullName = firstNonEmpty(columns, headerIndex, "full name", "student");
            if (!fullName.isEmpty()) {
                NameParts parts = splitName(fullName);
                name = parts.first;
                middleName = middleName.isEmpty() ? parts.middle : middleName;
                surname = surname.isEmpty() ? parts.last : surname;
            }
        }

        if (name.isEmpty()) {
            return null;
        }

        if (instrument.isEmpty()) {
            instrument = "Sidedrum";
        }
        if (knowledge.isEmpty()) {
            knowledge = "Beginner";
        }
        if (joiningDate.isEmpty()) {
            joiningDate = DateUtils.todayIso();
        }
        if (lastUpdated.isEmpty()) {
            lastUpdated = DateUtils.nowIsoDateTime();
        }

        Double balance = null;
        if (!balanceText.isEmpty()) {
            try {
                balance = Double.parseDouble(balanceText);
            } catch (NumberFormatException ignored) {
            }
        }

        return new Student(
                name,
                middleName,
                surname,
                phone,
                instrument,
                address,
                joiningDate,
                knowledge,
                lastUpdated,
                balance
        );
    }

    private static String value(List<String> columns, Map<String, Integer> headerIndex, String key) {
        Integer index = headerIndex.get(normalize(key));
        if (index == null || index < 0 || index >= columns.size()) {
            return "";
        }
        return safe(columns.get(index));
    }

    private static String firstNonEmpty(List<String> columns, Map<String, Integer> headerIndex, String... keys) {
        for (String key : keys) {
            String value = value(columns, headerIndex, key);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static List<String> parseLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result;
    }

    private static NameParts splitName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return new NameParts(parts[0], "", "");
        }
        if (parts.length == 2) {
            return new NameParts(parts[0], "", parts[1]);
        }
        StringBuilder middle = new StringBuilder();
        for (int i = 1; i < parts.length - 1; i++) {
            if (middle.length() > 0) {
                middle.append(' ');
            }
            middle.append(parts[i]);
        }
        return new NameParts(parts[0], middle.toString(), parts[parts.length - 1]);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US).replace("_", "").replace(" ", "");
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class NameParts {
        final String first;
        final String middle;
        final String last;

        NameParts(String first, String middle, String last) {
            this.first = first;
            this.middle = middle;
            this.last = last;
        }
    }
}
