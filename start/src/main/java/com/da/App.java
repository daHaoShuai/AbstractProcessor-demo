package com.da;

import com.da.web.core.DApp;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-23
 * @Time: 14:23
 */
public class App {
    public static void main(String[] args) {
        final DApp app = new DApp(App.class);
        app.listen();
    }
}
