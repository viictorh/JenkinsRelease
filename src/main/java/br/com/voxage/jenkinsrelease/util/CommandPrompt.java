package br.com.voxage.jenkinsrelease.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 
 * @author victor.bello
 *
 */
public class CommandPrompt {

    private static String getDiskUnity(String command) {
        String diskUnity = null;
        int indexOf = command.indexOf(":\\");
        if (indexOf != -1) {
            diskUnity = command.substring(indexOf - 1, indexOf);
        }
        return diskUnity;
    }

    private static BufferedReader createProcess(String command) throws IOException {
        String diskUnity = getDiskUnity(command);
        if (diskUnity != null) {
            command = diskUnity + ": && " + command;
        }
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")));
        return r;
    }

    // TODO pegar a unidade de maneira geral.
    public static String executeCommand(String cmd) throws IOException {
        BufferedReader r = createProcess(cmd);
        String line;
        StringBuilder builder = new StringBuilder();
        String lineSeparator = "";
        while ((line = r.readLine()) != null) {
            builder.append(lineSeparator).append(line);
            lineSeparator = System.lineSeparator();
        }
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getDiskUnity("cd \"D:\\VosAplicativos/VosProxyCMM\" && VosProxycmm install"));
        executeCommand("ping -i 5 uol.com.br");
    }

}