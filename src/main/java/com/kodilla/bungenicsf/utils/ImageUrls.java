package com.kodilla.bungenicsf.utils;

public class ImageUrls {
    public static String getRabbitImageUrl(String breed) {
        if (breed == null) {
            return "images/rabbits/WhiteDwarf1.jpg";
        }
        return switch (breed.toUpperCase()) {
            case "WHITE_DWARF" -> "images/rabbits/WhiteDwarf1.jpg";
            case "LIONHEAD" -> "images/rabbits/Lionhead1.jpg";
            case "FUZZY_LOP" -> "images/rabbits/FuzzyLop1.jpg";
            case "ANGORA" -> "images/rabbits/Angora1.jpg";
            case "FOX" -> "images/rabbits/Fox1.jpg";
            case "DALMATIAN" -> "images/rabbits/Dalmatian1.jpg";
            case "HARLEQUIN" -> "images/rabbits/Harlequin1.jpg";
            case "CHINCHILLA" -> "images/rabbits/Chinchilla1.jpg";
            case "GIANT" -> "images/rabbits/Giant1.jpg";
            default -> "images/rabbits/WhiteDwarf1.jpg";
        };
    }
}
