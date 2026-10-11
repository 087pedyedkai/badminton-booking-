package com.example.badminton.gui;

import com.example.badminton.model.Member;
import com.example.badminton.service.MemberService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;

/**
 * หน้าต่างสมัครสมาชิก (เปิดได้ทั้งจากหน้าเข้าสู่ระบบ และแท็บ "สมาชิก")
 *
 * การทำงาน: กรอกชื่อและเบอร์โทร -> กด "สมัครสมาชิก"
 * -> เรียก MemberService.registerMember() ซึ่งตรวจข้อมูลและออกรหัสสมาชิกที่ไม่ซ้ำ
 * -> ถ้าข้อมูลผิด (เบอร์ไม่ใช่ตัวเลข 9-10 หลัก หรือเบอร์ซ้ำ) จะแสดงข้อความสีแดง และยังไม่ปิดหน้าต่าง
 */
public class RegisterMemberDialog extends JDialog {

    private final MemberService memberService;
    private final JTextField nameField = new JTextField(22);
    private final JTextField phoneField = new JTextField(22);
    private final JLabel errorLabel = new JLabel(" ");
    private Member registeredMember; // สมาชิกที่สมัครสำเร็จ (null ถ้ากดยกเลิก)

    private RegisterMemberDialog(Window owner, MemberService memberService) {
        super(owner, "สมัครสมาชิกใหม่", ModalityType.APPLICATION_MODAL);
        this.memberService = memberService;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        UiHelper.addFormWide(form, 0, UiHelper.title("สมัครสมาชิก"));
        UiHelper.addFormRow(form, 1, "ชื่อ-นามสกุล:", nameField);
        UiHelper.addFormRow(form, 2, "เบอร์โทรศัพท์:", phoneField);
        errorLabel.setForeground(UiHelper.ERROR);
        UiHelper.addFormWide(form, 3, errorLabel);

        JButton registerButton = UiHelper.primaryButton("สมัครสมาชิก");
        JButton cancelButton = new JButton("ยกเลิก");
        registerButton.addActionListener(e -> register());
        cancelButton.addActionListener(e -> dispose());
        phoneField.addActionListener(e -> register()); // กด Enter ที่ช่องเบอร์โทร = สมัคร

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(registerButton);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(registerButton);
        pack();
        setLocationRelativeTo(owner);
    }

    private void register() {
        try {
            registeredMember = memberService.registerMember(nameField.getText(), phoneField.getText().trim());
            UiHelper.showInfo(this, "สมัครสมาชิกสำเร็จ",
                    "สมัครสมาชิกสำเร็จ!\nรหัสสมาชิกคือ " + registeredMember.getMemberId() + "\n\n"
                            + registeredMember.getInfoText());
            dispose();
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    /**
     * เปิดหน้าต่างสมัครสมาชิก แล้วรอจนกว่าจะปิด
     * คืนค่า Member ที่สมัครสำเร็จ หรือ null ถ้ายกเลิก
     */
    public static Member showDialog(Component parent, MemberService memberService) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        RegisterMemberDialog dialog = new RegisterMemberDialog(owner, memberService);
        dialog.setVisible(true); // หน้าต่างแบบ modal: โค้ดจะหยุดรอที่บรรทัดนี้จนกว่าจะปิดหน้าต่าง
        return dialog.registeredMember;
    }
}
