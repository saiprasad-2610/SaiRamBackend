package com.example.sairam_tea_backend.service;


import com.example.sairam_tea_backend.dto.ContactFormDTO;
import com.example.sairam_tea_backend.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendContactEmail(ContactFormDTO contactForm) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("sharemyride.app@gmail.com"); // Your email
        mail.setFrom("sharemyride.app@gmail.com");
        mail.setSubject("Contact Form: " + contactForm.getSubject());
        mail.setText(
                "Name: " + contactForm.getName() + "\n" +
                        "Email: " + contactForm.getEmail() + "\n\n" +
                        contactForm.getMessage()
        );
        javaMailSender.send(mail);
    }

    // This method will be called to send the order confirmation email
    public void sendOrderConfirmationEmail(Order order) {
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isEmpty()) {
            System.out.println("No customer email provided for order " + order.getId() + ". Skipping email confirmation.");
            return; // Don't send if no email is available
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(order.getCustomerEmail());
        mail.setFrom("sharemyride.app@gmail.com"); // IMPORTANT: This should be the same as spring.mail.username
        mail.setSubject("Sairam Tea Company: Your Order Confirmation #" + order.getId());

        // Constructing the email body with your provided content, made dynamic
        String emailBody = String.format(
                "Dear %s,\n\n" +
                        "Thank you for your order with Sairam Tea Company!\n\n" +
                        "Your Order ID: %d\n" +
                        "Total Amount: ₹%.2f\n" +
                        "Products Ordered: %s\n" +
                        "Delivery Address: %s\n" +
                        "Payment Method: %s\n" +
                        "Current Status: %s\n\n" +
                        "We are processing your order and will notify you once it's shipped.\n\n" +
                        "For any queries, please contact us at info@sairamtea.com or call +91 98765 43210.\n\n" +
                        "Thank you for choosing Sairam Tea Company.\n\n" +
                        "Best Regards,\n" +
                        "The Sairam Tea Team",
                order.getCustomerName(),
                order.getId(),
                order.getAmount(),
                order.getProductsOrdered(),
                order.getDeliveryAddress(),
                order.getPaymentMethod(),
                order.getStatus()
        );
        mail.setText(emailBody);

        try {
            javaMailSender.send(mail);
            System.out.println("Order confirmation email sent successfully to " + order.getCustomerEmail() + " for order ID " + order.getId());
        } catch (MailException e) {
            System.err.println("Error sending order confirmation email to " + order.getCustomerEmail() + " for order ID " + order.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }


    }
}
