package net.togogo.client;

import javafx.stage.Stage;
import net.togogo.dto.LoginResponse;
import net.togogo.service.AuthenticationService;
import net.togogo.service.BookService;
import net.togogo.service.UserService;

import java.util.concurrent.ExecutorService;

public class AppContext {
    private final UserService userService;
    private final BookService bookService;
    private final AuthenticationService authenticationService;
    private final TokenStore tokenStore;
    private final ExecutorService executorService;
    private final Stage primaryStage;
    private LoginResponse currentUser;

    public AppContext(UserService userService, BookService bookService,
                      AuthenticationService authenticationService, TokenStore tokenStore, 
                      ExecutorService executorService, Stage primaryStage) {
        this.userService = userService;
        this.bookService = bookService;
        this.authenticationService = authenticationService;
        this.tokenStore = tokenStore;
        this.executorService = executorService;
        this.primaryStage = primaryStage;
    }

    public UserService getUserService() { return userService; }
    public BookService getBookService() { return bookService; }
    public AuthenticationService getAuthenticationService() { return authenticationService; }
    public TokenStore getTokenStore() { return tokenStore; }
    public ExecutorService getExecutorService() { return executorService; }
    public Stage getPrimaryStage() { return primaryStage; }

    public LoginResponse getCurrentUser() { return currentUser; }
    public void setCurrentUser(LoginResponse user) { this.currentUser = user; }

    public void logout() {
        currentUser = null;
        tokenStore.clearToken();
        authenticationService.logout();
    }
}