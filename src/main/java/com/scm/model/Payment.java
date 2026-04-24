package com.scm.model;

import com.scm.enums.PaymentStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Payment for a course enrollment.
 */
@Entity
@Table(name = "payments")
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String paymentId;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column
    private float amount;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payment_date")
    private Date paymentDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_date")
    private Date dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column
    private PaymentStatus status;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    public Payment() {
    }

    public Payment(String paymentId, Student student, Course course, float amount) {
        this.paymentId = paymentId;
        this.student = student;
        this.course = course;
        this.amount = amount;
        this.paymentDate = null;
        this.dueDate = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
        this.status = PaymentStatus.PENDING;
        this.transactionId = null;
        this.paymentMethod = null;
        this.receiptNumber = null;
    }

    public boolean processPayment(String paymentMethod) {
        return processPayment(paymentMethod, false);
    }

    public boolean processPayment(String paymentMethod, boolean simulateFailure) {
        this.status = PaymentStatus.PROCESSING;
        this.paymentMethod = paymentMethod;
        if (simulateFailure) {
            this.status = PaymentStatus.FAILED;
            this.transactionId = null;
            this.receiptNumber = null;
            return false;
        }
        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = new Date();
        this.transactionId = "TXN" + System.currentTimeMillis();
        this.receiptNumber = "RCP" + System.currentTimeMillis();
        return true;
    }

    public boolean verifyPayment() {
        return status == PaymentStatus.COMPLETED && transactionId != null;
    }

    public String generateReceipt() {
        if (status != PaymentStatus.COMPLETED) {
            return "Payment not completed";
        }
        return "Receipt " + receiptNumber + "\nAmount: $" + amount + "\nTransaction: " + transactionId;
    }

    public boolean refund() {
        if (status == PaymentStatus.COMPLETED) {
            this.status = PaymentStatus.REFUNDED;
            return true;
        }
        return false;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public float getAmount() {
        return amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }
}
