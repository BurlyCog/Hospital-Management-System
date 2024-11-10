import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class HospitalManagementSystem extends JFrame {
    private JTextField txtName, txtAge, txtAddress, txtContactNo, txtEmail, txtQualifications, txtTime;
    private JComboBox<String> cmbGender, cmbBloodGroup, cmbPatient, cmbDoctor;

    public HospitalManagementSystem() {
        setTitle("Hospital Management System");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 2));

        // Patient Form
        add(new JLabel("Name:"));
        txtName = new JTextField();
        add(txtName);

        add(new JLabel("Age:"));
        txtAge = new JTextField();
        add(txtAge);

        add(new JLabel("Gender:"));
        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        add(cmbGender);

        add(new JLabel("Address:"));
        txtAddress = new JTextField();
        add(txtAddress);

        add(new JLabel("Contact No:"));
        txtContactNo = new JTextField();
        add(txtContactNo);

        add(new JLabel("Email:"));
        txtEmail = new JTextField();
        add(txtEmail);

        JButton btnAddPatient = new JButton("Add Patient");
        btnAddPatient.addActionListener(e -> addPatient());
        add(btnAddPatient);

        // Doctor Form
        add(new JLabel("Doctor's Name:"));
        txtQualifications = new JTextField();
        add(txtQualifications);

        add(new JLabel("Qualifications:"));
        JTextField txtDoctorQualifications = new JTextField(); // Separate qualifications field
        add(txtDoctorQualifications);

        add(new JLabel("Blood Group:"));
        cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"}); 
        add(cmbBloodGroup);

        JButton btnAddDoctor = new JButton("Add Doctor");
        btnAddDoctor.addActionListener(e -> addDoctor(txtDoctorQualifications.getText()));
        add(btnAddDoctor);

        // Appointment Form
        add(new JLabel("Patient:"));
        cmbPatient = new JComboBox<>();
        loadPatients();
        add(cmbPatient);

        add(new JLabel("Doctor:"));
        cmbDoctor = new JComboBox<>();
        loadDoctors();
        add(cmbDoctor);

        add(new JLabel("Appointment Time:"));
        txtTime = new JTextField();
        add(txtTime);

        JButton btnAddAppointment = new JButton("Add Appointment");
        btnAddAppointment.addActionListener(e -> addAppointment());
        add(btnAddAppointment);

        setLocationRelativeTo(null);
    }

    private void addPatient() {
        String name = txtName.getText().trim();
        String ageStr = txtAge.getText().trim();
        String contactNo = txtContactNo.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || contactNo.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid age.");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }

        try (Connection con = Connect.ConnectDB()) {
            String sql = "INSERT INTO Patient (PatientName, Age, Gender, Address, ContactNo, Email) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setInt(2, age);
            pst.setString(3, (String) cmbGender.getSelectedItem());
            pst.setString(4, txtAddress.getText());
            pst.setString(5, contactNo);
            pst.setString(6, email);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient added successfully!");
            loadPatients(); // Reload patients for the dropdown
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addDoctor(String qualifications) {
        if (txtQualifications.getText().trim().isEmpty() || qualifications.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all doctor fields.");
            return;
        }

        try (Connection con = Connect.ConnectDB()) {
            String sql = "INSERT INTO Doctor (DoctorName, Qualifications, BloodGroup) VALUES (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, txtQualifications.getText());
            pst.setString(2, qualifications);
            pst.setString(3, (String) cmbBloodGroup.getSelectedItem());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor added successfully!");
            loadDoctors(); // Reload doctors for the dropdown
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addAppointment() {
        if (cmbPatient.getSelectedItem() == null || cmbDoctor.getSelectedItem() == null || txtTime.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a patient, a doctor, and provide an appointment time.");
            return;
        }

        try (Connection con = Connect.ConnectDB()) {
            String sql = "INSERT INTO Appointment (PatientID, DoctorID, AppointmentTime) VALUES (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            String selectedPatient = (String) cmbPatient.getSelectedItem();
            int patientID = Integer.parseInt(selectedPatient.split(" - ")[0]);
            String selectedDoctor = (String) cmbDoctor.getSelectedItem();
            int doctorID = Integer.parseInt(selectedDoctor.split(" - ")[0]);
            pst.setInt(1, patientID);
            pst.setInt(2, doctorID);
            pst.setString(3, txtTime.getText());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadPatients() {
        try (Connection con = Connect.ConnectDB()) {
            String sql = "SELECT PatientID, PatientName FROM Patient";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            cmbPatient.removeAllItems();
            while (rs.next()) {
                cmbPatient.addItem(rs.getInt("PatientID") + " - " + rs.getString("PatientName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDoctors() {
        try (Connection con = Connect.ConnectDB()) {
            String sql = "SELECT DoctorID, DoctorName FROM Doctor";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            cmbDoctor.removeAllItems();
            while (rs.next()) {
                cmbDoctor.addItem(rs.getInt("DoctorID") + " - " + rs.getString("DoctorName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"; // Basic regex for email validation
        return email.matches(emailRegex);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HospitalManagementSystem hms = new HospitalManagementSystem();
            hms.setVisible(true);
        });
    }
}

class Connect {
    public static Connection ConnectDB() {
        Connection con = null;
        try {
            String url = "jdbc:mysql://localhost:3306/hospital"; // Update with your DB details
            String user = "root"; // Your DB username
            String password = "password"; // Your DB password
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
}
