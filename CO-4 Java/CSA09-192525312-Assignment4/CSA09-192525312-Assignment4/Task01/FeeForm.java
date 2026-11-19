import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/** The view: collects input and shows results. Every decision is delegated to FeeService. */
public class FeeForm extends JPanel {
    private final FeeService service;
    private final JTextField regNo = new JTextField(14), amount = new JTextField(14);
    private final JButton pay = new JButton("Pay"), clear = new JButton("Clear"), receipt = new JButton("Receipt");
    private final JLabel status = new JLabel(" ");
    private final JTextArea receiptArea = new JTextArea(2, 28);
    private PaymentResult last;

    public FeeForm(FeeService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8)); setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JPanel fields = new JPanel(new GridLayout(2, 2, 6, 6));
        fields.add(new JLabel("Register number")); fields.add(regNo);
        fields.add(new JLabel("Amount (INR)")); fields.add(amount);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.add(pay); buttons.add(clear); buttons.add(receipt);
        receiptArea.setEditable(false); receiptArea.setBorder(BorderFactory.createTitledBorder("Receipt"));
        JPanel south = new JPanel(new BorderLayout(4, 6));
        south.add(buttons, BorderLayout.NORTH); south.add(receiptArea, BorderLayout.CENTER); south.add(status, BorderLayout.SOUTH);
        add(fields, BorderLayout.NORTH); add(south, BorderLayout.CENTER);
        // one small, separate listener per button (lambdas calling named methods)
        pay.addActionListener(e -> onPay());
        clear.addActionListener(e -> onClear());
        receipt.addActionListener(e -> onReceipt());
    }

    private void onPay() {
        String err = service.validate(regNo.getText(), amount.getText());      // fast, runs on the EDT
        if (err != null) { show(err, true); return; }
        pay.setEnabled(false); show("Contacting bank ...", false);              // feedback; window stays responsive
        new SwingWorker<PaymentResult, Void>() {
            @Override protected PaymentResult doInBackground() { return service.pay(regNo.getText(), amount.getText()); }
            @Override protected void done() {
                try { last = get(); show(last.message(), !last.ok()); }
                catch (Exception ex) { show("Unexpected error: " + ex.getMessage(), true); }
                finally { pay.setEnabled(true); }
            }
        }.execute();
    }
    private void onClear() { regNo.setText(""); amount.setText(""); receiptArea.setText(""); show(" ", false); }
    private void onReceipt() { receiptArea.setText(service.receiptText(last)); }
    private void show(String msg, boolean error) { status.setForeground(error ? new Color(0xB00020) : new Color(0x1B5E20)); status.setText(msg); }
    // accessors used by the demo
    JTextField regNoField() { return regNo; } JTextField amountField() { return amount; }
}
