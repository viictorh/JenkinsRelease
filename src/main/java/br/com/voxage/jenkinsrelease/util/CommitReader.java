package br.com.voxage.jenkinsrelease.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import br.com.voxage.jenkinsrelease.bean.Commit;
import br.com.voxage.jenkinsrelease.bean.Release;
import br.com.voxage.jenkinsrelease.constant.BlockType;

public class CommitReader {

    private List<Commit>         commits;
    private Release              release;
    private static final Pattern COMPATIBILITY_PATTERN = Pattern.compile("@\\[Quebra.*\\]");
    private final static Logger  LOGGER                = Logger.getLogger(CommitReader.class);

    public CommitReader(List<Commit> commits) {
        this.commits = commits;
        Map<BlockType, Set<String>> blockCommits = new EnumMap<>(BlockType.class);
        for (BlockType blockType : BlockType.values()) {
            blockCommits.put(blockType, new HashSet<>());
        }
        release = new Release(blockCommits);
    }

    public Release read() {
        List<Block> items = new ArrayList<>();
        for (Commit commit : commits) {
            String message = commit.getMessage();
            Matcher matcher = COMPATIBILITY_PATTERN.matcher(message);
            if (matcher.find()) {
                String compatibility = matcher.group();
                if (compatibility.toLowerCase().contains("@[quebra(s)]")) {
                    release.setCompatibilityBreak(true);
                }
                message = message.replace(compatibility, "");
            }

            release.getBlockCommits().get(BlockType.DEV).add(commit.getAuthorName());
            release.getBlockCommits().get(BlockType.SUMMARY).add(commit.getTitle());

            for (BlockType type : BlockType.values()) {
                if (type.isAutomatic()) {
                    items.add(new Block(type, message.indexOf(type.getBlockName())));
                }
            }
            items.sort((i1, i2) -> i1.position.compareTo(i2.position));
            for (Block block : items) {
                block.message = getText(message, items, block);
                List<String> blocks = splitLines(block.message);
                release.getBlockCommits().get(block.type).addAll(blocks);
            }
            for (Block block : items) {
                message = message.replace(block.message, "").replace(block.type.getBlockName(), "");
            }

            if (!message.trim().isEmpty()) {
                release.getBlockCommits().get(BlockType.OLD).add(message.trim());
                release.setOld(true);
            }
            items.clear();
        }
        return release;
    }

    private String getText(String message, List<Block> items, Block block) {
        LOGGER.debug(message);
        LOGGER.debug(block);
        if (block.position != -1) {
            int next = message.length();
            int indexOf = items.indexOf(block);
            if (items.size() != (indexOf + 1)) {
                next = items.get(indexOf + 1).position;
            }
            String blockMessage = message.substring(block.position, next).replace(block.type.getBlockName(), "").trim();
            LOGGER.debug(blockMessage);
            return blockMessage;
        }
        return "N/A";
    }

    private List<String> splitLines(String message) {
        List<String> lines = new ArrayList<>();
        String[] split = message.split("\\n");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < split.length; i++) {
            String line = split[i].trim();
            if (i != 0 && line.endsWith(":") && (line.startsWith("@[Mantis(") || line.startsWith("-"))) {
                lines.add(sb.toString());
                sb = new StringBuilder();
            }
            sb.append(split[i]);
        }
        lines.add(sb.toString());
        return lines;
    }

    private static class Block {
        BlockType type;
        Integer   position;
        String    message;

        public Block(BlockType type, int position) {
            this.type = type;
            this.position = position;
            this.message = "";
        }

        @Override
        public String toString() {
            return "Block [type=" + type + ", position=" + position + ", message=" + message + "]";
        }
    }
}
