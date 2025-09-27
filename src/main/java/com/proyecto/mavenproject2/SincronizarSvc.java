
package com.proyecto.mavenproject2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Javier Zepeda
 */
public class SincronizarSvc {
            
    public void sincronizarBaseDatos(Connection mysql, Connection postgree){                
        
        List<Personal> listaPersonalMysql = buscarPersonal(mysql);                               
        List<Personal> listaPersonalPostgre = buscarPersonal(postgree);
                       
    }
    
    public List<Personal> buscarPersonal(Connection conexion){
        
        List<Personal> listaPersonal = new ArrayList<>();
        
        String query = "Select * from personal ";
        
        try (PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Personal p = new Personal();
                p.setDpi(rs.getString("dpi"));
                p.setPrimerNombre(rs.getString("primer_nombre"));
                p.setSegundoNombre(rs.getString("segundo_nombre"));
                p.setPrimerApellido(rs.getString("primer_apellido"));
                p.setSegundoApellido(rs.getString("segundo_apellido"));
                p.setDireccion(rs.getString("direccion"));
                p.setTelefonoCasa(rs.getString("telefono_casa"));
                p.setTelefonoMobil(rs.getString("telefono_movil"));
                p.setSalario(Double.valueOf(rs.getString("salario")));
                p.setBonificacion(Double.valueOf(rs.getString("bonificacion")));
                p.setFechaModificacion(rs.getTimestamp("fecha_actualizacion"));

                listaPersonal.add(p);
            }
        }catch(SQLException e){
            System.out.println("Error al intentar buscar los registros en la base de datos de mysql " + e);
        }
        
        
        return listaPersonal;
    }
}
