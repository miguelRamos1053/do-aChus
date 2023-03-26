/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package donnachusv1.cliente;

import conexionSQL.conexionSQL;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Miguel Ramos
 */
public class indexCliente extends javax.swing.JFrame {

    
    //-- Conexiones a SQL
    conexionSQL cc = new conexionSQL();
    Connection con = cc.conexion();
    
    DefaultTableModel modelo;
    
     // Inicializar variables
         String mesaSeleccionada;
         
    public indexCliente() {
        initComponents();
        
        this.setLocationRelativeTo(null);
        num(txtMesa);  //Impedir STRING en txtField
        num(txtBuscarProducto);  //Impedir STRING en txtField

        
        mostrarMesasActivas();
        
        
        // ---- Configuraciones de la tabla carrito------------------------------------
         inicializarTablaVentas();
         
        
         //-------------------------------------------------------------------------
         
        
        
    }

    public void inicializarTablaVentas(){
         tableCarrito.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD,12));
        tableCarrito.getTableHeader().setOpaque(false);
        tableCarrito.getTableHeader().setForeground(new Color(0,0,0,100));
        tableCarrito.getTableHeader().setBackground(new Color(114,140,60));
        tableCarrito.setRowHeight(25);
        
         modelo = new DefaultTableModel(){
             @Override
             public boolean isCellEditable(int fila, int columnas){
                 if(columnas == 3){
                     return true;
                 }else{
                     return false;
                 }
             }
         };
         modelo.addColumn("CODIGO");
         modelo.addColumn("NOMBRE");
         modelo.addColumn("PESO");
         modelo.addColumn("CANTIDAD");
         this.tableCarrito.setModel(modelo);
    }
    public void buscarProducto(String codigoProducto){
         String[] registros = new String[4];
         String SQL = "SELECT * FROM producto where codigo = "+codigoProducto;

         
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                registros[0]=rs.getString("codigo");
                registros[1]=rs.getString("nombre");
                registros[2]=rs.getString("peso");
                registros[3]="1";
                modelo.addRow(registros);
             }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al cargar la tabla "+e.getMessage());
        }
         
         System.out.println(modelo.toString());
    }
    
    private void num(JTextField a){
            a.addKeyListener (new KeyAdapter() {
               public void keyTyped (KeyEvent e) {
                      char c = e.getKeyChar ();
                      if(!Character.isDigit(c) && c != '.'){
                             e.consume() ;
                 }
                if(c == '.' && txtMesa.getText().contains(".")){
                         e .consume () ;
                }
           }
                    });
    }
    private boolean validarMesa(String mesa){
        String estado = "";
        boolean disponible;
        String SQL = "SELECT * FROM venta WHERE  estado='activa' AND mesa = "+mesa;
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SQL);
            
            while(rs.next()){
               estado = String.valueOf(rs.getString("estado"));           
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al validar estado de mesa "+ e.getMessage());
        }
        
        //Si estado es vacio significa que no encontro que esa mesa aun estuviera activa
        if(estado.equals("")){
            disponible = true;
        }else{
        //Si estado no es vacio significa que esa mesa aun esta activa
            disponible= false;
        }
        return disponible;
    }
    
    private String validarProductos(JTable carrito){
        String productosInsuficientes = "";
        String cantidad = "";
        for (int i = 0; i < carrito.getRowCount(); i++) {
                String codigoProducto = carrito.getValueAt(i, 0).toString();  
                String nombreProducto = carrito.getValueAt(i, 1).toString();
                String cantidadSolicitada = carrito.getValueAt(i, 3).toString();
                
                
                String SQL = "SELECT * FROM producto WHERE  codigo ="+codigoProducto;
                try {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery(SQL);

                    while(rs.next()){
                       cantidad = String.valueOf(rs.getString("cantidad"));
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error al validar codigo de producto "+ e.getMessage());
                }
                
                if((Integer.parseInt(cantidad)-Integer.parseInt(cantidadSolicitada)<0)){
                    productosInsuficientes = productosInsuficientes + nombreProducto+" ";
                }
                
            }
        return productosInsuficientes;
    }
    
    private boolean validarVenta(){
        boolean BanderaVenta = false;
        if(txtMesa.getText().equals("")){  //lleno campo mesa?
          JOptionPane.showMessageDialog(null, "Debes escribir el numero de mesa");
      }else{
           boolean estado = validarMesa(txtMesa.getText());
           if(estado){ //La mesa es valida?
               System.out.println("Excelente puedes asignar esta mes");
               String ProductosInsuficiones = validarProductos(tableCarrito);
                if(ProductosInsuficiones.equals("")){ //Productos validos?
                    System.out.println("Excelente hay la cantidad adecuada de productos");
                    if(tableCarrito.getRowCount()>0){
                        System.out.println("Excelente escogiste un producto");
                        //------------------------- ESTADO DE TODO ADMINITOOOOO------------------------
                        BanderaVenta = true;
                        
                        
                    }else{
                        JOptionPane.showMessageDialog(null, "Añade un producto");
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "No hay la cantidad suficiente de: "+ProductosInsuficiones);
                }
           }else{
               JOptionPane.showMessageDialog(null, "La mesa "+txtMesa.getText()+" esta ocupada");
           }
      }
        return BanderaVenta;
    }
    private boolean insertarVenta(){
        boolean ventaGuardad = false;
        try {
            String SQL = "INSERT INTO venta(tipoVenta,estado,fecha,mesa) values(?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(SQL);

            pst.setString(1, "local");
          
            pst.setString(2, "activa");
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");       
            pst.setString(3, dtf.format(LocalDateTime.now()));     
            
            
            pst.setString(4, txtMesa.getText()); 
           
            pst.execute();
             

            JOptionPane.showMessageDialog(null, "Venta guardada");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error de Registros "+e.getMessage());
            return ventaGuardad = false;
        }
        return ventaGuardad = true;
        
    }
    private void insertarProductosVenta(){
        
        String idVenta = "";
        int precioProducto = 0;
        int idProducto = 0;
        
        //----Buscar el id de la venta creada -------------------------------------------------------------------
        String SQL = "SELECT * FROM venta WHERE  estado = 'activa' AND mesa = "+txtMesa.getText();
                try {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery(SQL);

                    while(rs.next()){
                       idVenta = String.valueOf(rs.getString("id"));
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error al validar codigo de producto "+ e.getMessage());
                }
        //-----------------------------------------------------------------------------------------------------------
        
         for (int i = 0; i < tableCarrito.getRowCount(); i++) {
                
                String codigoProducto = tableCarrito.getValueAt(i, 0).toString();  
                String cantidadSolicitada = tableCarrito.getValueAt(i, 3).toString();
                
                //---------Busca el id y el precio del producto ---------
                String SQLProducto = "SELECT * FROM producto WHERE  codigo ="+codigoProducto;
                try {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery(SQLProducto);

                    while(rs.next()){
                       idProducto = rs.getInt("id");
                       precioProducto = rs.getInt("precio");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error al encontrar precio de producto "+ e.getMessage());
                }
                
                //------ Inserta el producto que se endio con su subtotal ------------
                int subTotal = precioProducto * Integer.parseInt(cantidadSolicitada);
                try {
                    String SQLVentaProducto = "INSERT INTO ventaProducto(subTotal,cantidad,idProducto,idVenta) values(?,?,?,?)";
                    PreparedStatement pst = con.prepareStatement(SQLVentaProducto);

                    pst.setInt(1, subTotal);

                    pst.setInt(2, Integer.parseInt(cantidadSolicitada));
                    
                    pst.setInt(3, idProducto);
                    pst.setInt(4, Integer.parseInt(idVenta));
                    
                    pst.execute();

                    descontarInventario(Integer.parseInt(cantidadSolicitada),idProducto);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error en insertar productos de venta "+e.getMessage());
                }
                
           }//---FIN FOR--
                  
    }
    
    public void descontarInventario(int cantidad, int idProducto){
        try {
            String SQL = "UPDATE producto SET cantidad = cantidad - ? WHERE id=?";
            
            PreparedStatement pst = con.prepareStatement(SQL);

            pst.setInt(1,cantidad);
            pst.setInt(2,idProducto);
     
            pst.execute();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al descontar en el inventario "+e.getMessage());
        }
    }
    
    private void mostrarMesasActivas(){
        String[] titulos={"# MESA"};
         String[] numMesa = new String[1];

         DefaultTableModel modelo = new DefaultTableModel(null, titulos){
             @Override
             public boolean isCellEditable(int fila, int columnas){
                 if(columnas == 3){
                     return true;
                 }else{
                     return false;
                 }
             }
         };
         
         
         String SQL = "SELECT mesa FROM venta WHERE estado = 'activa' ORDER BY CAST(mesa AS INT)";

         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                numMesa[0]=rs.getString("mesa");
                


                modelo.addRow(numMesa);
             }
             tablaMesa.setModel(modelo);
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al cargar las mesas activas "+e.getMessage());
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jlbPrincipal = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        txtBuscarProducto = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableCarrito = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        btnEliminar = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        txtMesa = new javax.swing.JTextField();
        btnGuardar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaMesa = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(114, 140, 69));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Century Gothic", 3, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Inventario");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 340, -1, -1));

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Century Gothic", 3, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Cuadre");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 450, 60, -1));

        jLabel5.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Ventas");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 220, 100, -1));

        jlbPrincipal.setBackground(new java.awt.Color(0, 0, 0));
        jlbPrincipal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlbPrincipal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/principal1.png"))); // NOI18N
        jlbPrincipal.setBorder(null);
        jlbPrincipal.setOpaque(true);
        jPanel1.add(jlbPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 160, 90));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/logo.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 160, -1));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/producto.png"))); // NOI18N
        jLabel2.setBorder(null);
        jLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel2MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel2MouseExited(evt);
            }
        });
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 260, 160, 100));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/cuadre.png"))); // NOI18N
        jLabel3.setBorder(null);
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel3MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel3MouseExited(evt);
            }
        });
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 370, 160, 110));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 160, 490));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtBuscarProducto.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        txtBuscarProducto.setForeground(new java.awt.Color(153, 153, 153));
        txtBuscarProducto.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBuscarProducto.setToolTipText("");
        txtBuscarProducto.setBorder(null);
        txtBuscarProducto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                txtBuscarProductoMousePressed(evt);
            }
        });
        txtBuscarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarProductoActionPerformed(evt);
            }
        });
        txtBuscarProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBuscarProductoKeyTyped(evt);
            }
        });
        jPanel2.add(txtBuscarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 120, 110, -1));
        jPanel2.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 140, 110, -1));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/anadir.png"))); // NOI18N
        jLabel7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 110, -1, 40));

        jScrollPane1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jScrollPane1KeyReleased(evt);
            }
        });

        tableCarrito.setBorder(null);
        tableCarrito.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tableCarrito.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "CODIGO", "PRODUCTO", "PESO", "CANTIDAD"
            }
        ));
        tableCarrito.setFocusable(false);
        tableCarrito.setRowHeight(25);
        tableCarrito.setSelectionBackground(new java.awt.Color(64, 52, 28));
        tableCarrito.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tableCarrito.setShowHorizontalLines(true);
        tableCarrito.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tableCarrito);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 160, 370, 180));

        jLabel8.setFont(new java.awt.Font("Century Gothic", 1, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Centro de Ventas");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 30, -1, -1));

        btnEliminar.setBackground(new java.awt.Color(204, 0, 0));
        btnEliminar.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar");
        btnEliminar.setBorder(null);
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });
        jPanel2.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 350, 80, 30));

        jLabel9.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Numero Mesa: ");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 390, -1, 30));

        txtMesa.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jPanel2.add(txtMesa, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 390, 30, -1));

        btnGuardar.setBackground(new java.awt.Color(0, 153, 0));
        btnGuardar.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnGuardar.setText("Guardar ");
        btnGuardar.setBorder(null);
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        jPanel2.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 440, 80, 30));

        tablaMesa.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        tablaMesa.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "# MESA"
            }
        ));
        tablaMesa.setFocusable(false);
        tablaMesa.setRowHeight(30);
        tablaMesa.setSelectionBackground(new java.awt.Color(64, 52, 28));
        tablaMesa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaMesaMouseClicked(evt);
            }
        });
        tablaMesa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tablaMesaKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(tablaMesa);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 130, 100, 330));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 0, 670, 490));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtBuscarProductoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtBuscarProductoMousePressed
        // TODO add your handling code here:
        if(txtBuscarProducto.getText().equals("CODIGO")){
            txtBuscarProducto.setText("");
            txtBuscarProducto.setForeground(Color.BLACK);
        }
    }//GEN-LAST:event_txtBuscarProductoMousePressed

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        if(txtBuscarProducto.getText().equals("")){
           JOptionPane.showMessageDialog(null, "Digite el codigo del producto");
        }else{
            String codigoProducto = txtBuscarProducto.getText();
            txtBuscarProducto.setText("");
            
            //--- Verdadeero = ya se añadio este produto 
            if(bucarRepetido(modelo,codigoProducto)){

                 JOptionPane.showMessageDialog(null, "Ya añadiste este producto");
            }else{
                buscarProducto(codigoProducto);
            }

        }        
        
    }//GEN-LAST:event_jLabel7MouseClicked

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        int fila = tableCarrito.getSelectedRow();
        if (fila>=0) {
            modelo.removeRow(fila);
        }else{
            JOptionPane.showMessageDialog(null, "Debes seleccionar un producto");
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private boolean bucarRepetido(DefaultTableModel modelo, String codigoProducto){
        int numFilas = modelo.getRowCount();
        if(numFilas==0){
            return false;
        }
        for (int i = 0; i < numFilas; i++) {

            int codigoTabla = Integer.parseInt(modelo.getValueAt(i,0).toString());

            if(codigoTabla == Integer.parseInt(codigoProducto)){
                return true;
            }
        }
        return false;
    }
    
    
    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
      if(validarVenta()){
          if(insertarVenta()){
              insertarProductosVenta();
              mostrarMesasActivas();
              txtMesa.setText("");
              
              inicializarTablaVentas();

          }
      }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void tablaMesaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaMesaMouseClicked
        // TODO add your handling code here:
        int filaSeleccionada = tablaMesa.rowAtPoint((evt.getPoint()));
        
         mesaSeleccionada = tablaMesa.getValueAt(filaSeleccionada,0).toString();
        System.out.println(mesaSeleccionada);
        
         int fila = tablaMesa.getSelectedRow();
        if (fila>=0) {
            detallesMesa dMesa = new detallesMesa();
            dMesa.setVisible(true);
            this.dispose();
            dMesa.mesaSeleccionada(mesaSeleccionada);
        }else{
            JOptionPane.showMessageDialog(null, "Debes seleccionar una mesa");
        }
    }//GEN-LAST:event_tablaMesaMouseClicked

    private void jLabel2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseEntered
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        jLabel2.setOpaque(true);
    }//GEN-LAST:event_jLabel2MouseEntered

    private void jLabel2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseExited
        // TODO add your handling code here:
        jLabel2.setBorder(null);
        jLabel2.setOpaque(false);
        


    }//GEN-LAST:event_jLabel2MouseExited

    private void jLabel3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseEntered
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        jLabel3.setOpaque(true);
    }//GEN-LAST:event_jLabel3MouseEntered

    private void jLabel3MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseExited
        // TODO add your handling code here:
        jLabel3.setBorder(null);
        jLabel3.setOpaque(false);


        
    }//GEN-LAST:event_jLabel3MouseExited

    private void txtBuscarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarProductoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarProductoActionPerformed

    private void txtBuscarProductoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarProductoKeyTyped
        // TODO add your handling code here:
        
        char tecla = evt.getKeyChar();
        
        if(tecla == KeyEvent.VK_ENTER){
               if(txtBuscarProducto.getText().equals("")){
           JOptionPane.showMessageDialog(null, "Digite el codigo del producto");
        }else{
            String codigoProducto = txtBuscarProducto.getText();
            txtBuscarProducto.setText("");
            
            //--- Verdadeero = ya se añadio este produto 
            if(bucarRepetido(modelo,codigoProducto)){

                 JOptionPane.showMessageDialog(null, "Ya añadiste este producto");
            }else{
                buscarProducto(codigoProducto);
            }

        } 
        }
    }//GEN-LAST:event_txtBuscarProductoKeyTyped

    private void tablaMesaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tablaMesaKeyTyped
        char tecla = evt.getKeyChar();
        if(tecla == KeyEvent.VK_ENTER){
                     int fila = tablaMesa.getSelectedRow();
                    if (fila>=0) {
                        detallesMesa dMesa = new detallesMesa();
                        dMesa.setVisible(true);
                        this.dispose();
                        dMesa.mesaSeleccionada(mesaSeleccionada);
                    }else{
                        JOptionPane.showMessageDialog(null, "Debes seleccionar una mesa");
                    }
        }
    }//GEN-LAST:event_tablaMesaKeyTyped

    private void jScrollPane1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jScrollPane1KeyReleased
        // TODO add your handling code here:
        System.out.println("estripe tecla en jtable");
    }//GEN-LAST:event_jScrollPane1KeyReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(indexCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(indexCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(indexCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(indexCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new indexCliente().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel jlbPrincipal;
    private javax.swing.JTable tablaMesa;
    private javax.swing.JTable tableCarrito;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtMesa;
    // End of variables declaration//GEN-END:variables
}
