package com.example.controldepresencia2026.utils;

public class ErrorUtils {
    public static String getFriendlyMessage(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "No tienes conexión a Internet. Comprueba tu red.";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "El servidor está tardando en responder. Inténtalo de nuevo.";
        } else if (t instanceof java.net.ConnectException) {
            return "No se ha podido conectar con el servidor.";
        } else {
            return "Error de comunicación: " + t.getLocalizedMessage();
        }
    }
}
