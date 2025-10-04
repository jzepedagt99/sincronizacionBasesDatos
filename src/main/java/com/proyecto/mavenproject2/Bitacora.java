/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto.mavenproject2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Javier Zepeda
 */
public class Bitacora {
    
    private static final String ARCHIVO = "bitacora.txt";
    
    public static void registrar(String dpi, String baseDatos, String operacion) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaHora = LocalDateTime.now().format(formatter);

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(fechaHora).append("] ").append(" DPI: ").append(dpi).append(" | Operacion: ").append(operacion).append(" | Realizado en la Base de datos: ").append(baseDatos);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            writer.write(sb.toString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
