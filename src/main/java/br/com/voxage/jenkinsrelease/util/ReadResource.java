package br.com.voxage.jenkinsrelease.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author victor.bello
 *
 */
public class ReadResource {

    private ReadResource() {
    }

    public static String read(String fileName) throws IOException {
        InputStream htmlInputStream = ReadResource.class.getClassLoader().getResourceAsStream(fileName);
        try (ByteArrayOutputStream baous = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            for (int n; 0 < (n = htmlInputStream.read(buf));) {
                baous.write(buf, 0, n);
            }
            return new String(baous.toByteArray(), "UTF-8");
        }
    }

}
