package org.example.gamestoreapp.service.impl;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.Order;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.OrderStatus;
import org.example.gamestoreapp.repository.OrderRepository;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.CheckoutService;
import org.example.gamestoreapp.service.EmailService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class CheckoutServiceImpl implements CheckoutService {
    private final UserHelperService userHelperService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartHelperService cartHelperService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public CheckoutServiceImpl(UserHelperService userHelperService, ShoppingCartRepository shoppingCartRepository, CartHelperService cartHelperService, OrderRepository orderRepository, UserRepository userRepository, EmailService emailService) {
        this.userHelperService = userHelperService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartHelperService = cartHelperService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void payment() throws MessagingException {
        User currentUser = userHelperService.getUser();
        Optional<ShoppingCart> cart = shoppingCartRepository.findByCustomer(currentUser);

        if (cart.isPresent()) {
            ShoppingCart shoppingCart = cart.get();
            Set<Game> games = shoppingCart.getGames();
            currentUser.getOwnedGames().addAll(games);

            Order order = new Order();
            order.setCustomer(currentUser);
            order.setOrderDate(LocalDateTime.now());
            order.setBoughtGames(games);
            order.setStatus(OrderStatus.APPROVED);
            order.setTotalPrice(cartHelperService.getTotalPrice());

            orderRepository.save(order);
            userRepository.save(currentUser);

            String subject = "Order Confirmation";
            String body = createConfirmationEmail(order, currentUser, shoppingCart.getGames());

            emailService.sendEmail(currentUser.getEmail(), subject, body);

            shoppingCartRepository.delete(shoppingCart);
        }
    }

    private String createConfirmationEmail(Order order, User customer, Set<Game> games) {
        StringBuilder sb = new StringBuilder();

        for (Game game : games) {
            sb.append("<li>").append(game.getTitle()).append("</li>");
        }

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Order Confirmation</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0;\">\n" +
                "    <div style=\"max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd;\">\n" +
                "        <h2 style=\"color: #2d3748;\">Thank You for Your Purchase!</h2>\n" +
                "        \n" +
                "        <p>Hi <strong>" + customer.getFirstName() + "</strong>,</p>\n" +
                "        <p>Thank you for your recent purchase with <strong>Game Store</strong>! We’re excited to let you know that your order (#<strong>" + order.getId() + "</strong>) has been successfully processed.</p>\n" +
                "        \n" +
                "        <h3 style=\"color: #4a5568;\">Order Details:</h3>\n" +
                "        <ul>\n" +
                "            <li><strong>Item(s) Purchased: </strong></li>\n" +
                "       " + sb + "\n" +
                "            <li><strong>Order Total: </strong>" + order.getTotalPrice() + " лв.</li>\n" +
                "        </ul>\n" +
                "\n" +
                "        <h3 style=\"color: #4a5568;\">What’s Next?</h3>\n" +
                "        <p><strong>Need Help?</strong> If you have any questions or need further assistance, feel free to reach out to us at <a href=\"mailto:[Customer Support Email]\" style=\"color: #3182ce;\">[Customer Support Email]</a> or call us at [Phone Number].</p>\n" +
                "\n" +
                "        <p>We truly appreciate your business and hope you love your new [product]! Keep an eye out for future offers and product updates from us.</p>\n" +
                "\n" +
                "        <p>Thanks again for choosing <strong>Game Store</strong>!</p>\n" +
                "\n" +
                "        <p>Best regards,</p>\n" +
                "        <p><strong>The Game Store Team</strong></p>\n" +
                "        <p>[Company Contact Information]</p>\n" +
                "        <p><a href=\"[Company Social Media Link]\" style=\"color: #3182ce;\">Follow us on social media</a></p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";
    }
}
