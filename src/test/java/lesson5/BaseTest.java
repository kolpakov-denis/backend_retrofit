package lesson5;

import lombok.SneakyThrows;

import java.io.*;
import java.util.Properties;


public class BaseTest {

    Properties props = new Properties();
    private static InputStream parameters;


    static {
        try {
            parameters = new FileInputStream("src/main/resources/test.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @SneakyThrows
    public void setSavedId(String newId) {
        props.load(parameters);
        props.setProperty("NewID", newId);
        FileOutputStream flow = new FileOutputStream("src/main/resources/test.properties");
        props.store(flow, "This is my test prop file");
    }
    @SneakyThrows
    public Integer getSavedId() {
        props.load(parameters);
        return Integer.parseInt(props.getProperty("NewID"));

    }


}
