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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Miguel Ramos
 */
public class detallesMesa extends javax.swing.JFrame {

        //-- Conexiones a SQL
    conexionSQL cc = new conexionSQL();
    Connection con = cc.conexion();
    
    String mesaSeleccionada = "";
    String idVenta="";
    
    DefaultTableModel modelo;
    
    public detallesMesa() {
        initComponents();
        this.setLocationRelativeTo(null);
        
        num(txtCambio);
        num(txtBuscarProducto);
        txtCambio.setEnabled(false);
          // ---- Configuraciones de la tabla carrito------------------------------------
         inicializarTablaVentas();
         
        
         //-------------------------------------------------------------------------

       

    }
     private void num(JTextField a){
            a.addKeyListener (new KeyAdapter() {
               public void keyTyped (KeyEvent e) {
                      char c = e.getKeyChar ();
                      if(!Character.isDigit(c) && c != '.'){
                             e.consume() ;
                 }
                if(c == '.' && txtCambio.getText().contains(".")){
                         e .consume () ;
                }
           }
                    });
    }
    public void mesaSeleccionada(String mesa){
         mesaSeleccionada = mesa;
         jLabel1.setText(mesa);

          buscarIdVenta(mesaSeleccionada);
          
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
                 if(columnas == 2){
                     return true;
                 }else{
                     return false;
                 }
             }
         };
         modelo.addColumn("CODIGO");
         modelo.addColumn("PRODUCTO");
         modelo.addColumn("CANTIDAD");
         modelo.addColumn("SUBTOTAL");
         this.tableCarrito.setModel(modelo);
    }
    public void buscarIdVenta(String mesa){
         String SQL = "SELECT * FROM venta WHERE estado = 'activa' AND mesa = "+mesa;

         
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                idVenta=rs.getString("id");
             }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error en la busqueda del id venta "+e.getMessage());
        }
         
         System.out.println("El id de la venta es: "+idVenta);
         
         buscarProductoVenta(idVenta);
         calcularTotal(idVenta);
    }
    public void buscarProductoVenta(String idVenta){
         String[] registros = new String[4];
         String idProducto = "";

         String SQL = "SELECT * FROM ventaProducto WHERE idVenta ="+idVenta;

         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                 
            //---- consulta sobre NOMBRE y CODIGO del id de un producto 
                            idProducto = rs.getString("idProducto");
                            //System.out.println("El id de este producto es: "+idProducto);
                            
                            String SQL2 = "SELECT * FROM producto WHERE id ="+idProducto;

                            try {
                                Statement st2 = con.createStatement();
                                ResultSet rs2 = st2.executeQuery(SQL2);

                                while(rs2.next()){

                                   registros[0]=rs2.getString("codigo");
                                   registros[1]=rs2.getString("nombre");
                                   registros[2]=rs.getString("cantidad");
                                   registros[3]=rs.getString("subTotal");

                                }
                           } catch (Exception e) {
                                JOptionPane.showMessageDialog(null, "Error al cargar el nombre y codigo del producto"+e.getMessage());
                           }
        //------------------------------------------------     

                modelo.addRow(registros);
             }
             tableCarrito.setModel(modelo);
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al cargar productos de idVenta "+e.getMessage());
        }
    }
    
    public void calcularTotal(String idVenta){
        String total ="";
        String SQL = "SELECT SUM(subTotal) as total FROM ventaproducto WHERE idVenta ="+idVenta;

         
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                total=rs.getString("total");
             }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error en el calculo de total"+e.getMessage());
        }
        jLTotal.setText(total);
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
                registros[2]="1";
                registros[3]=rs.getString("precio");
                modelo.addRow(registros);
             }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al cargar el producto buscado "+e.getMessage());
        }
         
         System.out.println(modelo.toString());
    }
      
    public void finalizarVenta(String idVenta){
        float pagoTotal = Float.parseFloat(jLTotal.getText());
        Object tipoPago= comboBoxTipoPago.getSelectedItem();
        
         try {
            String SQL = "UPDATE venta SET estado='libre', total=?,tipoPago=? WHERE id=?";
            
            PreparedStatement pst = con.prepareStatement(SQL);

            pst.setFloat(1,pagoTotal);
            pst.setString(2,tipoPago.toString());
            pst.setInt(3,Integer.parseInt(idVenta));
     
            pst.execute();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al finalizar venta"+e.getMessage());
        }
    }
    public void mostrarCambio(float total, float Cambio, Icon icon){
        float devuelto = Cambio - total;
        if(devuelto <0){
            JOptionPane.showMessageDialog(rootPane, "¡Se finalizo la venta conrrectamente! \n No tienes que devolver nada","FIN VENTA",JOptionPane.PLAIN_MESSAGE,icon);
        }else{
            JOptionPane.showMessageDialog(rootPane, "Se finalizo la venta conrrectamente! \n Debes devolver: "+devuelto,"FIN VENTA",JOptionPane.PLAIN_MESSAGE,icon);
        }
    }
    public void actualizarVenta(String idVenta,  DefaultTableModel modelo){
          int numFilas=0;
          String codigoProductoPedido="";
          int cantidadRegistrosBase = 0;
         //---- consulta las ventaProductos de este idVenta
         String SQL = "SELECT * FROM ventaProducto WHERE idVenta ="+idVenta;
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                    
                    int idProducto = Integer.parseInt(rs.getString("idProducto"));  // idProducto BASE DATOS

                    cantidadRegistrosBase ++;
                     //se consulta el codigo del idProducto de ventaProducto
                    String codigoProductoBaseDatos = (consultarCodigoProducto(idProducto));  // codigo idProducto BASE DATOS
                    
                    numFilas = modelo.getRowCount(); 
                    //---------COMPARACION BASE DE DATOS CON JTABLE---------------------- 
                    for (int i = 0; i < numFilas; i++) {
                        
                        codigoProductoPedido = modelo.getValueAt(i,0).toString();
                        
                        // Si el getValueAt(i,0) = al codigoConsulta realice las operaciones
                        if(codigoProductoBaseDatos.equals(codigoProductoPedido)){
                            //System.out.println(codigoProductoBaseDatos+" - "+codigoProductoPedido);
                            //int cantidadBaseDatos = Integer.parseInt(rs.getString("cantidad"));
                            int cantidadPedido = Integer.parseInt(modelo.getValueAt(i,2).toString());
                            
                            //int cantidadTotal = calcularCantidad(cantidadBaseDatos,cantidadPedido);
                            if(cantidadPedido == 0){
                                
                                int cantidadVentaProducto = consultarCantidadVentaProducto(idProducto,idVenta);
                                eliminarVentaProducto(idProducto,idVenta);
                                
                                descontarInventario((cantidadVentaProducto)*-1, idProducto);
                                
                                System.out.println("ELIMINANDO");

                            }else{
                                actualizarVentaProducto(cantidadPedido,idProducto,idVenta);
                            }
                        }
                    }
             }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al cargar productos de idVenta "+e.getMessage());
        }
         
        if(cantidadRegistrosBase<numFilas){
            
                    int nuevosRegistros = numFilas-(numFilas-cantidadRegistrosBase);
                    
                    for (int i = nuevosRegistros; i < numFilas; i++) {
            
                                System.out.println("SE AGREGARA EL PRODUCTO NUEVO "+modelo.getValueAt(i,0).toString());

                                int cantidadProductoPedido = Integer.parseInt(modelo.getValueAt(i,2).toString());
                                codigoProductoPedido = modelo.getValueAt(i,0).toString();
                                int idProductoPedido = consultarIdProducto(codigoProductoPedido);
                                
                                float subtotal = calcularSubtotal(idProductoPedido,cantidadProductoPedido);
                                //System.out.println("El id del producto adicional es: "+idProductoPedido+" y el subtotal es= "+subtotal);
                                agregarProductoVenta(idProductoPedido,cantidadProductoPedido,subtotal,idVenta);
                    } 
        }
        
        actualizarInfoVista();
    }
    public void agregarProductoVenta(int idProductoPedido,int cantidadProductoPedido,float subtotal,String idVenta){
        try {
                    String SQLVentaProducto = "INSERT INTO ventaProducto(subTotal,cantidad,idProducto,idVenta) values(?,?,?,?)";
                    PreparedStatement pst = con.prepareStatement(SQLVentaProducto);

                    pst.setFloat(1, subtotal);

                    pst.setInt(2, cantidadProductoPedido);
                    
                    pst.setInt(3, idProductoPedido);
                    pst.setString(4, idVenta);
                    
                    pst.execute();

                    //descontarInventario(Integer.parseInt(cantidadSolicitada),idProducto);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error en insertar productos de venta "+e.getMessage());
                }
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
    
    public String consultarCodigoProducto(int idProducto){
        
        String codigoProdcuto ="";
        String SQL = "SELECT * FROM producto WHERE id ="+idProducto;
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                    codigoProdcuto = rs.getString("codigo");
             }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al consultar el CODIGO "+e.getMessage());
        }
         return codigoProdcuto;
    }
      public int consultarIdProducto(String codigo){
        
        int idProducto=0;
        String SQL = "SELECT * FROM producto WHERE codigo ="+codigo;
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                    idProducto = rs.getInt("id");
             }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al consultar el id en base al CODIGO "+e.getMessage());
        }
         return idProducto;
    }
    
    public float consultarPrecioProducto(int idProducto){
        
        float precioProducto= 0;
        String SQL = "SELECT * FROM producto WHERE id ="+idProducto;
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                    precioProducto = Float.parseFloat(rs.getString("precio"));
             }
             tableCarrito.setModel(modelo);
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al consultar el precio del producto "+e.getMessage());
        }
         return precioProducto;
    }
    public void  actualizarVentaProducto(int cantidadPedido, int idProducto, String idVenta){
        
        
        //------------ INVENTARIO --------------
        int cantidadVentaProducto = consultarCantidadVentaProducto(idProducto,idVenta);
        int diferenciaCantidad =  cantidadPedido - cantidadVentaProducto;
         descontarInventario((diferenciaCantidad), idProducto);
                                
        
         // ---- VENTA PRODUCTO -----------
        float precioProducto = consultarPrecioProducto(idProducto);
        
        float subTotal = precioProducto * cantidadPedido;
        
         try {
            String SQL = "UPDATE ventaProducto SET subTotal=?, cantidad=? WHERE idVenta=? AND idProducto = ?";
            
            PreparedStatement pst = con.prepareStatement(SQL);

            pst.setFloat(1,subTotal);
            pst.setInt(2,cantidadPedido);
            pst.setInt(3,Integer.parseInt(idVenta));
            pst.setInt(4,idProducto);

     
            pst.execute();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar cantidad y subTotal"+e.getMessage());
        }
       
    }
    
    public void eliminarVentaProducto(int idProducto, String idVenta){
        try {
                        // DELETE FROM ventaProducto WHERE idVenta=? AND idProducto = ?
            String SQL = "DELETE FROM ventaProducto WHERE idVenta=? AND idProducto = ?";
            
            PreparedStatement pst = con.prepareStatement(SQL);

            pst.setString(1,idVenta);
            pst.setInt(2,idProducto);

     
            pst.execute();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar productoVenta"+e.getMessage());
        }
    }
    
    public int calcularCantidad(int cantidadBaseDatos,int cantidadPedido){
        int cantidadTotal;
        int diferencia = cantidadPedido - cantidadBaseDatos;
        cantidadTotal =cantidadBaseDatos + diferencia;
        return cantidadTotal;
    }
    
   public void actualizarInfoVista(){
       inicializarTablaVentas();
       buscarProductoVenta(idVenta);
       calcularTotal(idVenta);
   }
   
   public float calcularSubtotal(int idProductoPedido,int cantidadProductoPedido){
       float subTotal=0;
       
       float precioProducto = consultarPrecioProducto(idProductoPedido);
       subTotal = precioProducto*cantidadProductoPedido;
       
       return subTotal;
   }
   //          eliminarVentaProducto
   public void eliminarVentaProducto(String codigoProducto,String idVenta){
       
       int idProducto = consultarIdProducto(codigoProducto);
       
       int cantidadVentaProducto = consultarCantidadVentaProducto(idProducto,idVenta);
       
       // Solo elimina de la JTable ya que ese producto no se a agregado
       
       if(cantidadVentaProducto == 0){
           System.out.println("La cantidad es "+cantidadVentaProducto+" por esto se eliminara sin registro");
           int fila = tableCarrito.getSelectedRow();
           modelo.removeRow(fila);
       }else{
       // En este caso ya esta guardado en Venta producto y necesita ser borrado y cambiado en inventario
            System.out.println("Ahora si se eliminara del registro");
            eliminarVentaProducto(Integer.parseInt(codigoProducto),idVenta);
            descontarInventario((cantidadVentaProducto)*-1, idProducto);
            
       }
   }
   
   
   public int consultarCantidadVentaProducto(int idProducto, String idVenta){
       int cantidadPedido = 0;
       int idVenta1 = Integer.parseInt(idVenta);
       
       
        String SQL = "SELECT * FROM ventaProducto WHERE idVenta ='"+idVenta1+"'AND idProducto = '"+idProducto+"'";
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);

             while(rs.next()){
                    cantidadPedido = Integer.parseInt(rs.getString("cantidad"));
             }
             tableCarrito.setModel(modelo);
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Error al consultar la cantidad de ventaProducto para eliminar "+e.getMessage());
        }
         return cantidadPedido;
       
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
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lblAtras = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableCarrito = new javax.swing.JTable();
        txtBuscarProducto = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        btnEliminar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLTotal = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        comboBoxTipoPago = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        txtCambio = new javax.swing.JTextField();
        btnFinalizarVenta = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(114, 140, 69));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/logoPeque.png"))); // NOI18N
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 0, 100, 100));

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setFont(new java.awt.Font("Century Gothic", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Mesa numero : ");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 30, -1, -1));

        jLabel1.setBackground(new java.awt.Color(35, 35, 36));
        jLabel1.setFont(new java.awt.Font("Century Gothic", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(35, 35, 36));
        jLabel1.setText("#");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 20, -1, 50));

        lblAtras.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/iconAtras.png"))); // NOI18N
        lblAtras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblAtras.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAtrasMouseClicked(evt);
            }
        });
        jPanel2.add(lblAtras, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 750, 90));

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
                "CODIGO", "PRODUCTO", "CANTIDAD", "SUB TOTAL"
            }
        ));
        tableCarrito.setRowHeight(25);
        tableCarrito.setSelectionBackground(new java.awt.Color(64, 52, 28));
        tableCarrito.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tableCarrito.setShowHorizontalLines(true);
        tableCarrito.getTableHeader().setReorderingAllowed(false);
        tableCarrito.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tableCarritoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableCarritoKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tableCarritoKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(tableCarrito);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 210, 380, 240));

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
        txtBuscarProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarProductoKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBuscarProductoKeyTyped(evt);
            }
        });
        jPanel1.add(txtBuscarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 150, 110, -1));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/anadir.png"))); // NOI18N
        jLabel7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });
        jLabel7.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jLabel7KeyPressed(evt);
            }
        });
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 140, -1, 40));

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
        jPanel1.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 470, 80, 30));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 170, 110, -1));
        jPanel1.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 220, -1, -1));

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel1.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 120, 20, 390));

        jLabel8.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(153, 153, 153));
        jLabel8.setText("Cambio de:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 340, -1, 30));

        jLabel4.setBackground(new java.awt.Color(102, 102, 102));
        jLabel4.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donnachusv1/imgs/iconFactura.png"))); // NOI18N
        jLabel4.setText("Facturar");
        jLabel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel4.setOpaque(true);
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel4MouseEntered(evt);
            }
        });
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 390, 160, 50));

        jLTotal.setFont(new java.awt.Font("Century Gothic", 1, 24)); // NOI18N
        jLTotal.setForeground(new java.awt.Color(0, 0, 0));
        jLTotal.setText("0000");
        jPanel1.add(jLTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 200, -1, 50));

        jLabel10.setFont(new java.awt.Font("Century Gothic", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("CUENTA");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 130, -1, -1));

        comboBoxTipoPago.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        comboBoxTipoPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--", "Efectivo", "Transferencia" }));
        comboBoxTipoPago.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                comboBoxTipoPagoMouseClicked(evt);
            }
        });
        comboBoxTipoPago.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxTipoPagoActionPerformed(evt);
            }
        });
        jPanel1.add(comboBoxTipoPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 280, 110, -1));

        jLabel9.setFont(new java.awt.Font("Century Gothic", 0, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Total:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 210, -1, -1));

        jLabel11.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Tipo de pago:");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 280, -1, -1));
        jPanel1.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 380, -1, -1));
        jPanel1.add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 370, -1, -1));
        jPanel1.add(jSeparator6, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 272, -1, 100));
        jPanel1.add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 370, 100, 10));

        txtCambio.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        txtCambio.setBorder(null);
        jPanel1.add(txtCambio, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 350, 100, -1));

        btnFinalizarVenta.setBackground(new java.awt.Color(51, 153, 0));
        btnFinalizarVenta.setFont(new java.awt.Font("Century Gothic", 1, 20)); // NOI18N
        btnFinalizarVenta.setText("Finalizar Venta");
        btnFinalizarVenta.setBorder(null);
        btnFinalizarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinalizarVentaActionPerformed(evt);
            }
        });
        jPanel1.add(btnFinalizarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 460, 200, 50));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 750, 550));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblAtrasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAtrasMouseClicked
        indexCliente iCliente = new indexCliente();
        iCliente.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_lblAtrasMouseClicked

    private void txtBuscarProductoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtBuscarProductoMousePressed
        // TODO add your handling code here:
        if(txtBuscarProducto.getText().equals("CODIGO")){
            txtBuscarProducto.setText("");
            txtBuscarProducto.setForeground(Color.BLACK.BLACK);
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
    
    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        int fila = tableCarrito.getSelectedRow();
        if (fila>=0) {
            
            String codigoProducto = modelo.getValueAt(fila,0).toString();

            System.out.println("El codigo que se eliminara es: "+codigoProducto);
            eliminarVentaProducto(codigoProducto,idVenta);
            actualizarInfoVista();
        }else{
            JOptionPane.showMessageDialog(null, "Debes seleccionar un producto");
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void jLabel7KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jLabel7KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel7KeyPressed

    private void comboBoxTipoPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxTipoPagoActionPerformed
        // TODO add your handling code here:
        Object tipoPago= comboBoxTipoPago.getSelectedItem();
        
        if(tipoPago.equals("Efectivo")){
            txtCambio.setEnabled(true);
            jLabel8.setForeground(Color.black);
        }else{
            txtCambio.setEnabled(false);
            jLabel8.setForeground(Color.GRAY);
        }
        


    }//GEN-LAST:event_comboBoxTipoPagoActionPerformed

    private void jLabel4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseEntered
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jLabel4MouseEntered

    private void btnFinalizarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizarVentaActionPerformed
        Icon icon = new ImageIcon(getClass().getResource("../imgs/finVenta.png"));
        Object tipoPago= comboBoxTipoPago.getSelectedItem();
        if(tipoPago.equals("--")){
            JOptionPane.showMessageDialog(null, "Seleccione el tipo de pago");
        }else{
            //0-> si  ----    1->No
            int resp = JOptionPane.showConfirmDialog(null, "¿Quieres finlizar la mesa "+mesaSeleccionada+"?", "Alerta!", JOptionPane.YES_NO_OPTION);
            if(resp == 0){
                finalizarVenta(idVenta);
                if(tipoPago.equals("Efectivo")){
                    float total = Float.parseFloat(jLTotal.getText());
                    float cambio = Float.parseFloat(txtCambio.getText());
                    mostrarCambio(total,cambio,icon);
                    indexCliente iCliente = new indexCliente();
                    iCliente.setVisible(true);
                    this.dispose();
                }else{
                    JOptionPane.showMessageDialog(rootPane, "¡Se finalizo la venta conrrectamente! \n No tienes que devolver nada","FIN VENTA",JOptionPane.PLAIN_MESSAGE,icon);
                    indexCliente iCliente = new indexCliente();
                    iCliente.setVisible(true);
                    this.dispose();
                }
                
            }
        }
       
    }//GEN-LAST:event_btnFinalizarVentaActionPerformed

    private void comboBoxTipoPagoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_comboBoxTipoPagoMouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_comboBoxTipoPagoMouseClicked

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
                                //ACTUALIZAR VENTA BASE DE DATOSSSSS

                        actualizarVenta(idVenta, modelo);
        }
        
         

    }//GEN-LAST:event_txtBuscarProductoKeyTyped

    private void tableCarritoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableCarritoKeyReleased
         char tecla = evt.getKeyChar();
        if(tecla == KeyEvent.VK_ENTER){
            
            
                       //ACTUALIZAR VENTA BASE DE DATOSSSSS

                        actualizarVenta(idVenta, modelo);
        }
        
        

    }//GEN-LAST:event_tableCarritoKeyReleased

    private void tableCarritoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableCarritoKeyTyped

    }//GEN-LAST:event_tableCarritoKeyTyped

    private void txtBuscarProductoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarProductoKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarProductoKeyReleased

    private void tableCarritoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableCarritoKeyPressed

    }//GEN-LAST:event_tableCarritoKeyPressed

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
            java.util.logging.Logger.getLogger(detallesMesa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(detallesMesa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(detallesMesa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(detallesMesa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new detallesMesa().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnFinalizarVenta;
    private javax.swing.JComboBox<String> comboBoxTipoPago;
    private javax.swing.JLabel jLTotal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JLabel lblAtras;
    private javax.swing.JTable tableCarrito;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtCambio;
    // End of variables declaration//GEN-END:variables
}
