package br.com.voxage.jenkinsrelease.start;

import java.io.IOException;

import br.com.voxage.jenkinsrelease.service.ReleaseGenerator;

public class Start {

    public static void main(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (args.length < 2) {
            sb.append("Execute informando a variavel %WORKSPACE% e %GIT_COMMIT% ").append(System.lineSeparator());
            throw new IllegalArgumentException(sb.toString());
        } else {
            ReleaseGenerator.INSTANCE.start(args[0], args[1]);
        }
    }
}
