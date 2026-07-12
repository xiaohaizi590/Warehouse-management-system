package net.togogo.client.view;

import javafx.scene.Scene;
import net.togogo.client.AppContext;

/**
 * 缓存 Scene 避免重复创建导致的内存泄漏
 */
public class SceneCache {
    private static Scene loginScene;
    private static Scene mainScene;

    public static Scene getLoginScene(AppContext ctx, Runnable onLoginSuccess) {
        if (loginScene == null) {
            loginScene = new Scene(new LoginView(ctx, onLoginSuccess).build());
        }
        return loginScene;
    }

    public static Scene getMainScene(AppContext ctx, Runnable onLogout) {
        if (mainScene == null) {
            mainScene = new Scene(new MainView(ctx, onLogout).build());
        }
        return mainScene;
    }

    public static void clear() {
        loginScene = null;
        mainScene = null;
    }
}
