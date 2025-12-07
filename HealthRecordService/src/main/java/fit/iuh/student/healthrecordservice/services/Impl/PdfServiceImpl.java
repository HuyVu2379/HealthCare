package fit.iuh.student.healthrecordservice.services.Impl;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import fit.iuh.student.healthrecordservice.dtos.responses.MedicalRecordDetailResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionResponse;
import fit.iuh.student.healthrecordservice.services.MedicalRecordService;
import fit.iuh.student.healthrecordservice.services.PdfService;
import fit.iuh.student.healthrecordservice.utils.PdfConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {

    private final MedicalRecordService medicalRecordService;
    private PdfFont vietnameseFont;

    @Override
    public byte[] generatePrescriptionPdf(String recordId) throws Exception {
        // Fetch medical record with prescriptions
        MedicalRecordDetailResponse record = medicalRecordService.getMedicalRecordById(recordId);

        if (record == null) {
            throw new IllegalArgumentException("Không tìm thấy hồ sơ khám: " + recordId);
        }

        if (record.getPrescriptions() == null || record.getPrescriptions().isEmpty()) {
            throw new IllegalArgumentException("Hồ sơ này không có đơn thuốc");
        }

        // Generate PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        // Set margins
        document.setMargins(PdfConstants.MARGIN_TOP, PdfConstants.MARGIN_RIGHT,
                PdfConstants.MARGIN_BOTTOM, PdfConstants.MARGIN_LEFT);

        try {
            // Load Noto Sans font that fully supports Vietnamese characters
            vietnameseFont = loadVietnameseFont();
            document.setFont(vietnameseFont);

            // Add sections
            addLogo(document);
            addHeader(document, record);
            addTitle(document);
            addMedicalAndPatientInfoSideBySide(document, record);
            addPrescriptionTable(document, record);
            addNotes(document, record);
            addFooter(document, record);

        } catch (Exception e) {
            log.error("Error generating PDF for recordId: {}", recordId, e);
            throw new RuntimeException("Lỗi khi tạo file PDF: " + e.getMessage(), e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    /**
     * Add logo to PDF
     */
    private void addLogo(Document document) {
        try {
            InputStream logoStream = getClass().getResourceAsStream(PdfConstants.LOGO_PATH);
            if (logoStream != null) {
                Image logo = new Image(ImageDataFactory.create(logoStream.readAllBytes()));
                logo.setWidth(PdfConstants.LOGO_WIDTH);
                logo.setHeight(PdfConstants.LOGO_HEIGHT);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            } else {
                // Fallback: Display placeholder text
                Paragraph logoPlaceholder = new Paragraph("LOGO")
                        .setFontSize(PdfConstants.FONT_SIZE_HEADER)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10);
                document.add(logoPlaceholder);
            }
        } catch (Exception e) {
            log.warn("Không thể load logo, bỏ qua phần logo", e);
            // Continue without logo
        }
    }

    /**
     * Add header with clinic/hospital name
     */
    private void addHeader(Document document, MedicalRecordDetailResponse record) {
        String serviceName = record.getServiceName() != null ? record.getServiceName() : "PHÒNG KHÁM";

        Paragraph header = createParagraph(serviceName)
                .setFontSize(PdfConstants.FONT_SIZE_HEADER)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);

        document.add(header);
    }

    /**
     * Add title "ĐƠN THUỐC"
     */
    private void addTitle(Document document) {
        Paragraph title = createParagraph("ĐƠN THUỐC")
                .setFontSize(PdfConstants.FONT_SIZE_TITLE)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);
    }

    /**
     * Add patient information section
     */
    private void addPatientInfo(Document document, MedicalRecordDetailResponse record) {
        Paragraph sectionTitle = createParagraph("THÔNG TIN BỆNH NHÂN")
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        if (record.getPatient() != null) {
            addInfoLine(document, "Họ và tên:", record.getPatient().getFullName());
            addInfoLine(document, "Số điện thoại:", record.getPatient().getPhone());
            addInfoLine(document, "Email:", record.getPatient().getEmail());
        }

        document.add(createParagraph("").setMarginBottom(15));
    }

    /**
     * Add medical examination information
     */
    private void addMedicalInfo(Document document, MedicalRecordDetailResponse record) {
        Paragraph sectionTitle = createParagraph("THÔNG TIN KHÁM BỆNH")
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

        addInfoLine(document, "Ngày khám:",
                record.getAppointmentDate() != null ? dateFormat.format(record.getAppointmentDate()) : "N/A");
        addInfoLine(document, "Bác sĩ điều trị:", record.getDoctorName() != null ? record.getDoctorName() : "N/A");
        addInfoLine(document, "Chẩn đoán:", record.getDiagnosis() != null ? record.getDiagnosis() : "N/A");

        document.add(createParagraph("").setMarginBottom(15));
    }

    /**
     * Add medical info (LEFT) and patient info (RIGHT) side by side using 2-column table
     */
    private void addMedicalAndPatientInfoSideBySide(Document document, MedicalRecordDetailResponse record) {
        // Create 2-column table (50% width each)
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setBorder(null);  // No outer border

        // LEFT COLUMN: Medical Info
        Cell leftCell = new Cell()
                .setBorder(null)
                .setPadding(0)
                .setPaddingRight(10);  // Space between columns

        // Add medical section title
        Paragraph medicalTitle = createParagraph("THÔNG TIN KHÁM BỆNH")
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                .setBold()
                .setMarginBottom(10);
        leftCell.add(medicalTitle);

        // Add medical details
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        leftCell.add(createInfoParagraph("Ngày khám:",
                record.getAppointmentDate() != null ? dateFormat.format(record.getAppointmentDate()) : "N/A"));
        leftCell.add(createInfoParagraph("Bác sĩ điều trị:",
                record.getDoctorName() != null ? record.getDoctorName() : "N/A"));
        leftCell.add(createInfoParagraph("Chẩn đoán:",
                record.getDiagnosis() != null ? record.getDiagnosis() : "N/A"));

        // RIGHT COLUMN: Patient Info
        Cell rightCell = new Cell()
                .setBorder(null)
                .setPadding(0)
                .setPaddingLeft(10);  // Space between columns

        // Add patient section title
        Paragraph patientTitle = createParagraph("THÔNG TIN BỆNH NHÂN")
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                .setBold()
                .setMarginBottom(10);
        rightCell.add(patientTitle);

        // Add patient details
        if (record.getPatient() != null) {
            rightCell.add(createInfoParagraph("Họ và tên:", record.getPatient().getFullName()));
            rightCell.add(createInfoParagraph("Số điện thoại:", record.getPatient().getPhone()));
            rightCell.add(createInfoParagraph("Email:", record.getPatient().getEmail()));
        }

        // Add cells to table (LEFT first, then RIGHT)
        infoTable.addCell(leftCell);
        infoTable.addCell(rightCell);

        // Add table to document
        document.add(infoTable);
        document.add(createParagraph("").setMarginBottom(15));
    }

    /**
     * Add prescription table
     */
    private void addPrescriptionTable(Document document, MedicalRecordDetailResponse record) {
        Paragraph sectionTitle = createParagraph("ĐƠN THUỐC ĐIỀU TRỊ")
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Create table with 5 columns
        Table table = new Table(UnitValue.createPercentArray(PdfConstants.PRESCRIPTION_TABLE_WIDTHS))
                .useAllAvailableWidth();

        // Add header row
        addTableHeader(table, "STT");
        addTableHeader(table, "Tên thuốc");
        addTableHeader(table, "Liều lượng");
        addTableHeader(table, "Cách dùng");
        addTableHeader(table, "Thời gian dùng");

        // Add prescription rows
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        int index = 1;
        for (PrescriptionResponse prescription : record.getPrescriptions()) {
            // STT
            table.addCell(createCell(String.valueOf(index++)));

            // Medicine name
            table.addCell(createCell(prescription.getMedicalName() != null ? prescription.getMedicalName() : ""));

            // Dosage
            table.addCell(createCell(prescription.getDosage() != null ? prescription.getDosage() : ""));

            // Frequency
            String frequency = prescription.getFrequency() != null ?
                    prescription.getFrequency().stream()
                            .map(PdfConstants::formatFrequency)
                            .collect(Collectors.joining(" - "))
                    : "";
            table.addCell(createCell(frequency));

            // Duration
            String duration = "";
            if (prescription.getStartDate() != null && prescription.getEndDate() != null) {
                duration = dateFormat.format(prescription.getStartDate()) + " - " +
                        dateFormat.format(prescription.getEndDate());
            }
            table.addCell(createCell(duration));
        }

        document.add(table);
        document.add(createParagraph("").setMarginBottom(15));
    }

    /**
     * Add notes section
     */
    private void addNotes(Document document, MedicalRecordDetailResponse record) {
        boolean hasNotes = false;

        if (record.getDoctorNote() != null && !record.getDoctorNote().trim().isEmpty()) {
            hasNotes = true;
        }

        // Check if any prescription has notes
        if (!hasNotes && record.getPrescriptions() != null) {
            hasNotes = record.getPrescriptions().stream()
                    .anyMatch(p -> p.getNotes() != null && !p.getNotes().trim().isEmpty());
        }

        if (hasNotes) {
            Paragraph sectionTitle = createParagraph("GHI CHÚ VÀ LỜI DẶN:")
                    .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                    .setBold()
                    .setMarginBottom(10);
            document.add(sectionTitle);

            // Doctor notes
            if (record.getDoctorNote() != null && !record.getDoctorNote().trim().isEmpty()) {
                Paragraph doctorNote = createParagraph(record.getDoctorNote())
                        .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                        .setMarginBottom(10);
                document.add(doctorNote);
            }

            // Prescription-specific notes
            if (record.getPrescriptions() != null) {
                for (PrescriptionResponse prescription : record.getPrescriptions()) {
                    if (prescription.getNotes() != null && !prescription.getNotes().trim().isEmpty()) {
                        Paragraph prescriptionNote = createParagraph("- " + prescription.getMedicalName() + ": " + prescription.getNotes())
                                .setFontSize(PdfConstants.FONT_SIZE_SMALL)
                                .setMarginBottom(5);
                        document.add(prescriptionNote);
                    }
                }
            }

            document.add(createParagraph("").setMarginBottom(15));
        }
    }

    /**
     * Add footer with follow-up date and signature
     */
    private void addFooter(Document document, MedicalRecordDetailResponse record) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

        // Follow-up date if exists
        if (record.getFollowUpDate() != null) {
            Paragraph followUp = createParagraph("Ngày tái khám: " + dateFormat.format(record.getFollowUpDate()))
                    .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                    .setMarginBottom(20);
            document.add(followUp);
        }

        // Create 2-column table for signature section (left empty, right has signature)
        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setBorder(null);

        // LEFT CELL: Empty
        Cell leftEmptyCell = new Cell()
                .setBorder(null)
                .add(createParagraph(""));

        // RIGHT CELL: Date + Signature (all centered within this cell)
        Cell rightSignatureCell = new Cell()
                .setBorder(null)
                .setPadding(0);

        // Appointment date
        java.util.Date recordDate = record.getAppointmentDate() != null ?
                record.getAppointmentDate() : record.getCreatedAt();

        if (recordDate != null) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", new Locale("vi", "VN"));
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM", new Locale("vi", "VN"));
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", new Locale("vi", "VN"));

            String appointmentDate = "Ngày " + dayFormat.format(recordDate) +
                    " tháng " + monthFormat.format(recordDate) +
                    " năm " + yearFormat.format(recordDate);

            Paragraph dateP = createParagraph(appointmentDate)
                    .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                    .setTextAlignment(TextAlignment.CENTER)  // Căn giữa trong cột phải
                    .setMarginBottom(10);
            rightSignatureCell.add(dateP);
        }

        // Doctor signature label (centered within right cell)
        Paragraph signatureLabel = createParagraph("Bác sĩ điều trị")
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)  // Căn giữa trong cột phải
                .setMarginBottom(20);
        rightSignatureCell.add(signatureLabel);

        // Signature text (centered within right cell)
        if (record.getSignatureUrl() != null && !record.getSignatureUrl().trim().isEmpty()) {
            Paragraph signatureText = createParagraph(record.getSignatureUrl())
                    .setFontSize(PdfConstants.FONT_SIZE_HEADER)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER);  // Căn giữa trong cột phải
            rightSignatureCell.add(signatureText);
        }

        // Add cells to table
        signatureTable.addCell(leftEmptyCell);
        signatureTable.addCell(rightSignatureCell);

        // Add table to document
        document.add(signatureTable);
    }

    /**
     * Helper: Add info line (label + value)
     */
    private void addInfoLine(Document document, String label, String value) {
        Paragraph line = createParagraph("")
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                .setMarginBottom(5);

        line.add(createBoldText(label + " "));
        line.add(createText(value != null ? value : ""));

        document.add(line);
    }

    /**
     * Helper: Create info paragraph (label + value) for table cells
     */
    private Paragraph createInfoParagraph(String label, String value) {
        Paragraph p = createParagraph("")
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL)
                .setMarginBottom(5);
        p.add(createBoldText(label + " "));
        p.add(createText(value != null ? value : ""));
        return p;
    }

    /**
     * Helper: Create table header cell
     */
    private void addTableHeader(Table table, String text) {
        Paragraph p = createParagraph(text)
                .setBold()
                .setFontSize(PdfConstants.FONT_SIZE_NORMAL);

        Cell cell = new Cell()
                .add(p)
                .setBackgroundColor(PdfConstants.TABLE_HEADER_BG)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
        table.addHeaderCell(cell);
    }

    /**
     * Helper: Create table data cell
     */
    private Cell createCell(String text) {
        Paragraph p = createParagraph(text)
                .setFontSize(PdfConstants.FONT_SIZE_SMALL);

        return new Cell()
                .add(p)
                .setPadding(6)
                .setBorder(new SolidBorder(PdfConstants.TABLE_BORDER_COLOR, 0.5f));
    }

    /**
     * Helper: Load Vietnamese font from external path or classpath
     */
    private PdfFont loadVietnameseFont() throws Exception {
        // Try external font first, fallback to classpath
        String externalFontPath = System.getProperty("user.home") + "/healthcare-fonts/NotoSans-Regular.ttf";
        File externalFont = new File(externalFontPath);

        if (externalFont.exists()) {
            log.info("Loading font from external path: {}", externalFontPath);
            FontProgram fontProgram = FontProgramFactory.createFont(externalFontPath);
            return PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } else {
            log.info("External font not found, trying classpath...");
            InputStream fontStream = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf");
            if (fontStream != null) {
                byte[] fontBytes = fontStream.readAllBytes();
                FontProgram fontProgram = FontProgramFactory.createFont(fontBytes);
                fontStream.close();
                return PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            } else {
                throw new RuntimeException("Vietnamese font not found");
            }
        }
    }

    /**
     * Helper: Create Paragraph with Vietnamese font
     */
    private Paragraph createParagraph(String text) {
        Paragraph p = new Paragraph(text);
        if (vietnameseFont != null) {
            p.setFont(vietnameseFont);
        }
        return p;
    }

    /**
     * Helper: Create Text with Vietnamese font
     */
    private com.itextpdf.layout.element.Text createText(String text) {
        com.itextpdf.layout.element.Text t = new com.itextpdf.layout.element.Text(text);
        if (vietnameseFont != null) {
            t.setFont(vietnameseFont);
        }
        return t;
    }

    /**
     * Helper: Create bold Text with Vietnamese font
     */
    private com.itextpdf.layout.element.Text createBoldText(String text) {
        return createText(text).setBold();
    }
}
