import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {


    final private Font mainFont = new Font("Arial", Font.BOLD, 14);
    JTextField tfCliente, tfCaja, tfCadete;
    JTextArea lbInfo;
    public String textInfoClientes;
    public String textInfoCajeros;
    public String textInfoDepositos;
    public String textInfoEnvios;

    public void initializa() {
        /* Main FORM */
        JLabel lbCliente = new JLabel("Cantidad clientes");
        lbCliente.setFont(mainFont);
        tfCliente = new JTextField();
        tfCliente.setFont(mainFont);

        JLabel lbCaja = new JLabel("Cantidad cajas");
        lbCaja.setFont(mainFont);
        tfCaja = new JTextField();
        tfCaja.setFont(mainFont);

        JLabel lbCadete = new JLabel("Cantidad cadetes");
        lbCadete.setFont(mainFont);
        tfCadete = new JTextField();
        lbCadete.setFont(mainFont);

        JPanel fPanel = new JPanel();
        fPanel.setLayout(new GridLayout(3, 1, 5, 5));
        fPanel.add(lbCliente);
        fPanel.add(tfCliente);
        fPanel.add(lbCaja);
        fPanel.add(tfCaja);
        fPanel.add(lbCadete);
        fPanel.add(tfCadete);

        /* Start Form */
        lbInfo = new JTextArea();
        JScrollPane scroll = new JScrollPane (lbInfo, 
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        lbInfo.setFont(mainFont);
        //lbInfo.setText("<html><textarea>");

        /* Buttons */
        JButton btnOk = new JButton("Ok");
        btnOk.setFont(mainFont);
        btnOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String nombre = tfCliente.getText();
                String caja = tfCaja.getText();
                String cadete = tfCadete.getText();
                if(nombre.length()>0 && caja.length()>0 && cadete.length()>0)
                    //lbInfo.setText("Producto agregado " + nombre + " $" + precio);
                    {
                        try{
                            App.Simulacion(Integer.parseInt(caja), Integer.parseInt(nombre), Integer.parseInt(cadete));
                        }
                        catch (NumberFormatException e){
                            lbInfo.setText( e.getMessage() );
                        }
                        catch (Exception e){
                            lbInfo.setText( e.getMessage() );
                        }
                        
                    }
            }
        });

        JButton btnClear = new JButton("Borrar");
        btnClear.setFont(mainFont);
        btnClear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                tfCliente.setText("");
                tfCaja.setText("");
                tfCadete.setText("");
                lbInfo.setText("");
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1,2,5,5));
        buttonsPanel.add(btnOk);
        buttonsPanel.add(btnClear);


        JPanel mPanel = new JPanel();
        mPanel.setLayout(new BorderLayout());
        mPanel.add(fPanel, BorderLayout.NORTH);
        mPanel.add(scroll, BorderLayout.CENTER);
        mPanel.add(buttonsPanel, BorderLayout.SOUTH);
        mPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        setTitle("Supermarket");
        setSize(800,600);
        setMinimumSize(new Dimension(800,600));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        add(mPanel);
    }

    public void setInfo(String info) {
        
        lbInfo.setText(lbInfo.getText()+ "\n" + info);
    }
}