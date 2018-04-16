package br.com.voxage.jenkinsrelease.start;

import java.io.IOException;

import br.com.voxage.service.Generator;
import br.com.voxage.service.PageGenerator;
import br.com.voxage.util.ReadResource;

/**
 * 
 * @author victor.bello
 *
 */
public class Main {

    public static void main(String[] args) throws Exception {
        proccessArgs(args).process();
        System.out.println("Execução finalizada com sucesso.");
    }

    private static Generator proccessArgs(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (args.length == 0) {
            sb.append("Execute este jar de uma das duas maneiras abaixo: ").append(System.lineSeparator());
            sb.append("1 - Para visualizar o template dos release notes: java -jar JenkinsRelease.jar \"PRINT\"").append(System.lineSeparator());
            sb.append("2 - Para criar a pagina principal com indices e todos os release: java -jar JenkinsRelease.jar <caminho dos release notes> <branch> ").append(System.lineSeparator());

            throw new IllegalArgumentException(sb.toString());
        } else if ("PRINT".equals(args[0])) {
            return () -> System.out.println(ReadResource.read("release_template.html"));
        } else if (args.length == 2) {
            return new PageGenerator(args[0], args[1]);
        } else {
            return new PageGenerator(args[0]);
        }
    }

}
