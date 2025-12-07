package fit.iuh.student.healthrecordservice.utils;

import com.itextpdf.kernel.colors.DeviceRgb;
import fit.iuh.student.healthrecordservice.enums.Frequency;

/**
 * Constants for PDF generation
 */
public class PdfConstants {

    // Font sizes
    public static final float FONT_SIZE_TITLE = 20f;
    public static final float FONT_SIZE_SUBTITLE = 12f;
    public static final float FONT_SIZE_HEADER = 16f;
    public static final float FONT_SIZE_NORMAL = 11f;
    public static final float FONT_SIZE_SMALL = 9f;

    // Margins
    public static final float MARGIN_LEFT = 40f;
    public static final float MARGIN_RIGHT = 40f;
    public static final float MARGIN_TOP = 40f;
    public static final float MARGIN_BOTTOM = 40f;

    // Colors
    public static final DeviceRgb TABLE_HEADER_BG = new DeviceRgb(230, 230, 230);
    public static final DeviceRgb TABLE_BORDER_COLOR = new DeviceRgb(200, 200, 200);

    // Logo
    public static final String LOGO_PATH = "/static/images/Logo.png";
    public static final float LOGO_WIDTH = 80f;
    public static final float LOGO_HEIGHT = 80f;

    // Vietnamese frequency mappings
    public static final String FREQUENCY_MORNING = "Sáng";
    public static final String FREQUENCY_AFTERNOON = "Chiều";
    public static final String FREQUENCY_EVENING = "Tối";

    // Table column widths (relative)
    public static final float[] PRESCRIPTION_TABLE_WIDTHS = {0.8f, 3f, 1.5f, 2f, 2f};

    /**
     * Convert Frequency enum to Vietnamese text
     */
    public static String formatFrequency(Frequency frequency) {
        if (frequency == null) {
            return "";
        }
        switch (frequency) {
            case MORNING:
                return FREQUENCY_MORNING;
            case AFTERNOON:
                return FREQUENCY_AFTERNOON;
            case EVENING:
                return FREQUENCY_EVENING;
            default:
                return "";
        }
    }

    private PdfConstants() {
        // Prevent instantiation
    }
}
