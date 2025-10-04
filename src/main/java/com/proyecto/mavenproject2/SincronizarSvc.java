package com.proyecto.mavenproject2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Javier Zepeda
 */
public class SincronizarSvc {

    /**
     * En base a las conexiones hace una busqueda de los registros de cada base de datos a la tabla personal
     * luego procede a verificar la sincronizacion
     * @param mysql conexion a la base de datos mysql
     * @param postgree conexion a la base de datos en postre
     * 
     */
    public void sincronizarBaseDatos(Connection mysql, Connection postgree) {

        if (mysql == null) {
            mysql = ConexionBD.getConnectionMysql();
        }

        if (postgree == null) {
            postgree = ConexionBD.getPostgresConnection();
        }

        List<Personal> listaPersonalMysql = buscarPersonal(mysql);
        List<Personal> listaPersonalPostgre = buscarPersonal(postgree);

        // Convertir listas a mapas para buscar más fácil por DPI
        Map<String, Personal> mapMysql = listaPersonalMysql.stream().collect(Collectors.toMap(Personal::getDpi, p -> p));
        Map<String, Personal> mapPostgre = listaPersonalPostgre.stream().collect(Collectors.toMap(Personal::getDpi, p -> p));

        // Unir todas las llaves (dpi) de ambas bases
        Set<String> allDpis = new HashSet<>();
        allDpis.addAll(mapMysql.keySet());
        allDpis.addAll(mapPostgre.keySet());

        for (String dpi : allDpis) {
            Personal mysqlP = mapMysql.get(dpi);
            Personal postgreP = mapPostgre.get(dpi);

            if (mysqlP == null && postgreP != null) {
                // No existe en MySQL → Insertar en MySQL
                insertarPersonal(mysql, postgreP, "MySQL");

            } else if (mysqlP != null && postgreP == null) {
                // No existe en PostgreSQL → Insertar en PostgreSQL
                insertarPersonal(postgree, mysqlP, "PostgreSQL");

            } else if (mysqlP != null && postgreP != null) {
                // Existe en ambas → comparar fechas
                if (mysqlP.getFechaModificacion().after(postgreP.getFechaModificacion())) {
                    // MySQL tiene la versión más reciente → actualizar en PostgreSQL
                    actualizarPersonal(postgree, mysqlP, "PostgreSQL");
                } else if (postgreP.getFechaModificacion().after(mysqlP.getFechaModificacion())) {
                    // PostgreSQL tiene la versión más reciente → actualizar en MySQL
                    actualizarPersonal(mysql, postgreP, "MySQL");
                }
            }
        }

    }
    
    /**
     * inserta la nueva persona que no existe en alguna base de datos
     * @param conexion conexion a cualquier base de datos (mysql, postgre)
     * @param p persona con los datos a ingresar en la base de datos donde no exite
     * @param baseDatos 
     */
    public void insertarPersonal(Connection conexion, Personal p, String baseDatos) {
        String sql = "INSERT INTO personal (dpi, primer_nombre, segundo_nombre, primer_apellido, "
                + "segundo_apellido, direccion, telefono_casa, telefono_movil, salario, bonificacion, fecha_modificacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, p.getDpi());
            ps.setString(2, p.getPrimerNombre());
            ps.setString(3, p.getSegundoNombre());
            ps.setString(4, p.getPrimerApellido());
            ps.setString(5, p.getSegundoApellido());
            ps.setString(6, p.getDireccion());
            ps.setString(7, p.getTelefonoCasa());
            ps.setString(8, p.getTelefonoMobil());
            ps.setDouble(9, p.getSalario());
            ps.setDouble(10, p.getBonificacion());
            ps.setDate(11, p.getFechaModificacion());
            ps.executeUpdate();
            

            Bitacora.registrar(p.getDpi(), baseDatos, "INSERT");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * modifica el registro que necesita adquirir los datos del registro mas actal de la otra base de datos
     * @param conexion conexion a cualquier base de datos (mysql, postgre)
     * @param p persona con los datos que van a modificar el registro de la base de datos
     * @param baseDatos nombre de la base de datos
     */
    public void actualizarPersonal(Connection conexion, Personal p, String baseDatos) {
        String sql = "UPDATE personal SET primer_nombre=?, segundo_nombre=?, primer_apellido=?, "
                + "segundo_apellido=?, direccion=?, telefono_casa=?, telefono_movil=?, salario=?, bonificacion=?, fecha_modificacion=? "
                + "WHERE dpi=?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, p.getPrimerNombre());
            ps.setString(2, p.getSegundoNombre());
            ps.setString(3, p.getPrimerApellido());
            ps.setString(4, p.getSegundoApellido());
            ps.setString(5, p.getDireccion());
            ps.setString(6, p.getTelefonoCasa());
            ps.setString(7, p.getTelefonoMobil());
            ps.setDouble(8, p.getSalario());
            ps.setDouble(9, p.getBonificacion());
            ps.setDate(10, p.getFechaModificacion());
            ps.setString(11, p.getDpi());
            ps.executeUpdate();
            
            Bitacora.registrar(p.getDpi(), baseDatos, "UPDATE");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Personal> buscarPersonal(Connection conexion) {

        List<Personal> listaPersonal = new ArrayList<>();

        String query = "Select * from personal ";

        try (PreparedStatement ps = conexion.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

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
                p.setFechaModificacion(rs.getDate("fecha_modificacion"));

                listaPersonal.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Error al intentar buscar los registros en la base de datos de mysql " + e);
        }

        return listaPersonal;
    }
}
