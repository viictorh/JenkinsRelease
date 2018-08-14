package br.com.voxage.jenkinsrelease.util;

import static br.com.voxage.jenkinsrelease.util.Log.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.voxage.jenkinsrelease.bean.Commit;
import br.com.voxage.jenkinsrelease.bean.Release;
import br.com.voxage.jenkinsrelease.constant.BlockType;

public class CommitReader {

    private List<Commit>         commits;
    private Release              release;
    private static final Pattern COMPATIBILITY_PATTERN = Pattern.compile("@\\[Quebra.*\\]", Pattern.CASE_INSENSITIVE);

    public CommitReader(List<Commit> commits) {
        this.commits = commits;
        release = new Release();
    }

    public Release read() {
        Set<Block> blockOrder = new TreeSet<>();
        for (Commit commit : commits) {
            String messageCopy = commit.getMessage();
            String message = commit.getMessage();
            Matcher matcher = COMPATIBILITY_PATTERN.matcher(message);
            if (matcher.find()) {
                String compatibility = matcher.group();
                if (compatibility.toLowerCase().contains("@[quebra(s)]")) {
                    release.setCompatibilityBreak(true);
                }
                message = message.replace(compatibility, "");
                messageCopy = message;
            }
            release.addBlockMessage(BlockType.DEV, commit.getAuthorName());
            release.addBlockMessage(BlockType.SUMMARY, commit.getTitle());
            for (BlockType type : BlockType.values()) {
                if (type.isAutomatic()) {
                    blockOrder.add(new Block(type, message.indexOf(type.getBlockName())));
                }
            }

            for (Block block : blockOrder) {
                String text = getText(message, new ArrayList<Block>(blockOrder), block);
                messageCopy = messageCopy.replace(text, "").replace(block.type.getBlockName(), "");
                release.addBlockMessage(block.type, text);
            }

            if (!messageCopy.trim().isEmpty()) {
                release.addBlockMessage(BlockType.OLD, messageCopy.trim());
            }
            blockOrder.clear();
        }
        log.trace(release);
        return release;
    }

    private String getText(String message, ArrayList<Block> arrayList, Block block) {
        log.trace(message);
        log.trace(block);
        if (block.position != -1) {
            int next = message.length();
            int indexOf = arrayList.indexOf(block);
            if (arrayList.size() != (indexOf + 1)) {
                next = arrayList.get(indexOf + 1).position;
            }
            return message.substring(block.position, next).replace(block.type.getBlockName(), "").trim();
        }
        return "N/A";
    }

    private static class Block implements Comparable<Block> {
        BlockType type;
        Integer   position;

        public Block(BlockType type, int position) {
            this.type = type;
            this.position = position;
        }

        @Override
        public int compareTo(Block o) {
            int compareTo = this.position.compareTo(o.position);
            if (compareTo == 0) {
                return this.type.compareTo(o.type);
            }
            return compareTo;
        }

        @Override
        public String toString() {
            return "Block [type=" + type + ", position=" + position + "]";
        }

    }
}
