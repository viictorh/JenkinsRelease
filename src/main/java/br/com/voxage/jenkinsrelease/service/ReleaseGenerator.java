package br.com.voxage.jenkinsrelease.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.voxage.jenkinsrelease.bean.Commit;

public enum ReleaseGenerator {
    INSTANCE;
    GitService gitService;

    public void start(String workspace, String tag) throws IOException {
        gitService = new GitService(workspace);
        String commit = gitService.findHashFromTag(tag);
        String endCommit = findStartCommit(commit);
        List<String> hashes = findCommitHashes(commit, endCommit);
        List<Commit> commits = findCommitMessages(hashes);
        processMessages(commits);
    }

    private String findStartCommit(String commit) throws IOException {
        String previousTag = gitService.findPreviousTagFromHash(commit);
        String previousHash = gitService.findHashFromTag(previousTag);
        String lines[] = previousHash.split("\\r?\\n");
        previousHash = lines[0];
        if (!GitService.validHash(previousHash)) {
            previousHash = gitService.findFirstCommit();
        }
        return previousHash;
    }

    private List<String> findCommitHashes(String commit, String endCommit) throws IOException {
        String[] findHashesBetween = gitService.findHashesBetween(commit, endCommit).split(System.lineSeparator());
        return Arrays.asList(findHashesBetween);
    }

    private List<Commit> findCommitMessages(List<String> hashes) throws IOException {
        List<Commit> commits = new ArrayList<>();
        for (String hash : hashes) {
            gitService.findCommitMessage(hash).ifPresent(c -> commits.add(c));
        }
        return commits;
    }

    private void processMessages(List<Commit> commits) {
        int corrigido, novo, alterado, info, demanda, tarefa;
        List<Integer> items = new ArrayList<>();
        for (Commit commit : commits) {
            String message = commit.getMessage();
            corrigido = message.indexOf("@[Corrigido]");
            novo = message.indexOf("@[Novo]");
            alterado = message.indexOf("@[Alterado]");
            info = message.indexOf("@[Informações técnicas]");
            demanda = message.indexOf("@[Demandas]");
            tarefa = message.indexOf("@[Tarefa]");
            items = Arrays.asList(corrigido, novo, alterado, info, demanda, tarefa);
            items.sort((a, b) -> a.compareTo(b));
            if (corrigido == -1) {
                System.out.println(message);
                continue;
            }
            // validate when -1
            String corrigidoBlock = message.substring(corrigido, findNext(items, corrigido, message.length())).replace("@[Corrigido]", "");
            String novoBlock = message.substring(novo, findNext(items, novo, message.length())).replace("@[Novo]", "");
            String alteradoBlock = message.substring(alterado, findNext(items, alterado, message.length())).replace("@[Alterado]", "");
            String infoBlock = message.substring(info, findNext(items, info, message.length())).replace("@[Informações técnicas]", "");
            String demandaBlock = message.substring(demanda, findNext(items, demanda, message.length())).replace("@[Demandas]", "");
            String tarefaBlock = message.substring(tarefa, findNext(items, tarefa, message.length())).replace("@[Tarefa]", "");
            message = message.replace(corrigidoBlock, "");
            message = message.replace(novoBlock, "");
            message = message.replace(alteradoBlock, "");
            message = message.replace(infoBlock, "");
            message = message.replace(demandaBlock, "");
            message = message.replace(tarefaBlock, "");
            System.out.println("MESSAGE: -- " + message);
            // DEPOIS DE PEGAR CADA BLOCO, REALIZO UM REPLACE E O QUE SOBRAR JOGO NO "nÃO IDENTIFICADO/DINOSSAURO"

            System.out.println("Corrigido: " + corrigidoBlock);
            System.out.println("Novo: " + novoBlock);
            System.out.println(alteradoBlock);
            System.out.println(infoBlock);
            System.out.println(demandaBlock);
            System.out.println(tarefaBlock);
        }
    }

    private int findNext(List<Integer> items, int current, int length) {
        int indexOf = items.indexOf(current);
        if (items.size() == (indexOf + 1)) {
            return length;
        }
        return items.get(indexOf + 1);
    }

}
