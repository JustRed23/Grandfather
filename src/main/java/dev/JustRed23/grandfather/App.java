package dev.JustRed23.grandfather;

import dev.JustRed23.stonebrick.app.Application;
import dev.JustRed23.stonebrick.data.FileStructure;

public class App extends Application {

    protected void init() throws Exception {
        FileStructure.discover(GFS.class);
    }

    protected void start() throws Exception {

    }

    protected void stop() throws Exception {

    }

    public static void main(String[] args) {
        launch(args);
    }
}
