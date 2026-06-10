package com.kodilla.bungenicsf.dto;

public enum SuggestedLocation {
    STOKKSEYRI("Stokkseyri, Iceland", 63.8350, -21.0620, "Cool summers (10-15°C), cold winters (0-5°C), windy, frequent rain and snow"),
    HAMBLEDEN("Hambleden, United Kingdom", 51.5700, -0.8700, "Mild summers (18-22°C), cool winters (5-10°C), frequent rain"),
    PECICE("Pęcice, Poland", 52.1397, 20.8556, "Warm summers (20-25°C), cold winters (-5 to 0°C), snow in winter"),
    PROKSHINO("Prokshino, Russia", 56.5000, 38.5000, "Warm summers (18-23°C), very cold winters (-10 to -5°C)"),
    SAQQARA("Saqqara, Egypt", 29.8710, 31.2160, "Very hot summers (35-40°C), mild winters (15-20°C), almost no rain"),
    CUANDIXIA("Cuandixia, China", 40.0000, 116.0000, "Hot rainy summers, cold dry winters"),
    KANGAROO_VALLEY("Kangaroo Valley, Australia", -34.7200, 150.5300, "Warm humid summers, mild winters"),
    VAL_QUIRICO("Val'Quirico, Mexico", 19.2800, -98.2300, "Mild year-round, rainy season in summer");

    private final String displayName;
    private final double lat;
    private final double lon;
    private final String description;

    SuggestedLocation(String displayName, double lat, double lon, String description) {
        this.displayName = displayName;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getDescription() { return description; }

    public static SuggestedLocation fromDisplayName(String name) {
        for (SuggestedLocation loc : values()) {
            if (loc.displayName.equals(name)) return loc;
        }
        return null;
    }
}