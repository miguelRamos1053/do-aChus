/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexionSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

/**
 *
 * @author user
 */
public class conexionSQL {
     Connection conectar=null;
  
     public Connection conexion(){
        
         try {
             Class.forName("com.mysql.jdbc.Driver");
             conectar = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/donnachusdb","root","");

             //JOptionPane.showMessageDialog(null,"Conexion Exitosa!");
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null," Error en la conexion "+e.getMessage());
         }
         return conectar;
     }
    
}
