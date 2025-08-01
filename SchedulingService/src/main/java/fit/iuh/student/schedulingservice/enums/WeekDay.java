package fit.iuh.student.schedulingservice.enums;

public enum WeekDay {
    MONDAY("Thứ Hai"),
    TUESDAY("Thứ Ba"),
    WEDNESDAY("Thứ Tư"),
    THURSDAY("Thứ Năm"),
    FRIDAY("Thứ Sáu"),
    SATURDAY("Thứ Bảy"),
    SUNDAY("Chủ Nhật");

    private final String displayName;

    WeekDay(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
