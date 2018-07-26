package br.com.voxage.jenkinsrelease.start;

import java.io.IOException;

import br.com.voxage.jenkinsrelease.constant.Type;
import br.com.voxage.jenkinsrelease.service.CommitIdentifier;
import br.com.voxage.jenkinsrelease.service.Generator;
import br.com.voxage.jenkinsrelease.service.PageGenerator;

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
            sb.append("Execute este jar de uma das 3 maneiras abaixo: ").append(System.lineSeparator());
            sb.append("1 - Para gerar a tag inicial do commit: java -jar JenkinsRelease.jar \"COMMIT\" \"<caminho workspace>\"").append(System.lineSeparator());
            sb.append("2 - Para visualizar o template dos release notes: java -jar JenkinsRelease.jar \"PRINT\"").append(System.lineSeparator());
            sb.append("3 - Para criar a pagina principal com indices e todos os release: java -jar JenkinsRelease.jar <caminho dos release notes> <branch> ").append(System.lineSeparator());

            throw new IllegalArgumentException(sb.toString());
        } else if ("COMMIT".equals(args[0])) {
            if (args.length < 2) {
                throw new IllegalArgumentException("Informe o workspace que será procurado o commit inicial");
            }
            return new CommitIdentifier(args[1]);
        } else if (args.length == 3) {
            Type type = Type.valueOf(args[1]);
            if (type == null) {
                System.out.println("Type (segundo parametro), não identificado. Ele deve ter o valor 'TAG' ou 'BRANCH'");
                return new PageGenerator(args[0]);
            } else {
                return new PageGenerator(args[0], type.releaseType(args[2]));
            }
        } else {
            return new PageGenerator(args[0]);
        }
    }

}
