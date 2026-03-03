package com.mycompany.carritogrpc;


import com.google.protobuf.Empty;
import com.tienda.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class CarritoGRPCclientGUI extends JFrame {
    private JTable tablaCatalogo;
    private DefaultTableModel modelCatalogo;
    private List<Producto> carritoLocal = new ArrayList<>();
    private CarritoServiceGrpc.CarritoServiceBlockingStub stub;

    public CarritoGRPCclientGUI() {
        setTitle("Tienda del Cliente");
        setSize(500, 400);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Configurar gRPC
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        stub = CarritoServiceGrpc.newBlockingStub(channel);

        // UI
        modelCatalogo = new DefaultTableModel(new Object[]{"ID", "Nombre", "Precio"}, 0);
        tablaCatalogo = new JTable(modelCatalogo);
        add(new JLabel("Catálogo de Productos:"));
        add(new JScrollPane(tablaCatalogo));

        JButton btnAgregar = new JButton("Agregar Seleccionado al Carrito");
        JButton btnComprar = new JButton("Finalizar Compra");
        add(btnAgregar);
        add(btnComprar);

        // Cargar Catálogo
        cargarCatalogo();

        btnAgregar.addActionListener(e -> {
            int row = tablaCatalogo.getSelectedRow();
            if (row != -1) {
                String cantStr = JOptionPane.showInputDialog("Cantidad:");
                try {
                    int cant = Integer.parseInt(cantStr);
                    Producto p = Producto.newBuilder()
                            .setId(modelCatalogo.getValueAt(row, 0).toString())
                            .setNombre(modelCatalogo.getValueAt(row, 1).toString())
                            .setPrecio((Double) modelCatalogo.getValueAt(row, 2))
                            .setCantidad(cant).build();
                    carritoLocal.add(p);
                    JOptionPane.showMessageDialog(this, "Agregado al carrito local.");
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Cantidad inválida."); }
            }
        });

        btnComprar.addActionListener(e -> {
            CarritoRequest request = CarritoRequest.newBuilder()
                    .setUsuarioId("Sebastian_Moreno")
                    .addAllItems(carritoLocal)
                    .build();
            
            CarritoResponse res = stub.procesarCarrito(request);
            
            if (res.getEstado().equals("EXITOSO")) {
                JOptionPane.showMessageDialog(this, "¡Éxito!\nTransacción: " + res.getTransaccionId() + "\nTotal: $" + res.getTotalPagar());
                carritoLocal.clear();
            } else {
                JOptionPane.showMessageDialog(this, res.getEstado(), "Error de Compra", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    private void cargarCatalogo() {
        // Cambiado Empty por EmptyRequest para coincidir con el .proto
        CatalogoResponse res = stub.obtenerCatalogo(EmptyRequest.newBuilder().build());
        res.getProductosList().forEach(p -> {
            modelCatalogo.addRow(new Object[]{p.getId(), p.getNombre(), p.getPrecio()});
        });
    }

    public static void main(String[] args) { new CarritoGRPCclientGUI(); }
}