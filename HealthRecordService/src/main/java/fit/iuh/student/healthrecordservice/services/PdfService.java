package fit.iuh.student.healthrecordservice.services;

/**
 * Service for generating PDF documents
 */
public interface PdfService {

    /**
     * Generate prescription PDF for a medical record
     *
     * @param recordId Medical record ID
     * @return PDF as byte array
     * @throws Exception if record not found, no prescriptions, or PDF generation fails
     */
    byte[] generatePrescriptionPdf(String recordId) throws Exception;
}
