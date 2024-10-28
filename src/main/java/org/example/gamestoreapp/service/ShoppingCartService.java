package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.ShoppingCartDTO;

public interface ShoppingCartService {

    void addToCart(Long gameId);

    ShoppingCartDTO getShoppingCart();

    void remove(Long id);

    void removeAll();

    boolean isGameInCart(Long id);
}
