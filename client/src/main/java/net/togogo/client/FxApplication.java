package net.togogo.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.togogo.client.view.SceneCache;
import net.togogo.service.AuthenticationService;
import net.togogo.service.InventoryService;
import net.togogo.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FxApplication extends Application {

    private static ConfigurableApplicationContext springContext;
    private AppContext appContext;
    private ExecutorService executorService;
    private Runnable onLoginSuccess;
    private Runnable onLogout;

    @Override
    public void start(Stage primaryStage) {
        springContext = SpringApplication.run(ClientApplication.class, new String[0]);
        executorService = Executors.newFixedThreadPool(4);

        UserService userService = springContext.getBean(UserService.class);
        InventoryService inventoryService = springContext.getBean(InventoryService.class);
        AuthenticationService authenticationService = springContext.getBean(AuthenticationService.class);
        TokenStore tokenStore = springContext.getBean(TokenStore.class);

        appContext = new AppContext(userService, inventoryService, authenticationService, tokenStore, executorService, primaryStage);

        onLoginSuccess = () -> {
            SceneCache.clear();
            primaryStage.setScene(SceneCache.getMainScene(appContext, onLogout));
        };

        onLogout = () -> {
            appContext.logout();
            SceneCache.clear();
            primaryStage.setScene(SceneCache.getLoginScene(appContext, onLoginSuccess));
        };

        primaryStage.setTitle("仓库管理系统");
        primaryStage.setWidth(1100);
        primaryStage.setHeight(750);
        primaryStage.setScene(SceneCache.getLoginScene(appContext, onLoginSuccess));
        primaryStage.show();
    }

    @Override
    public void stop() {
        executorService.shutdown();
        if (appContext != null) {
            appContext.logout();
        }
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}