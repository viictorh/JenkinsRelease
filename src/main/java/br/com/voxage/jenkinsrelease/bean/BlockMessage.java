package br.com.voxage.jenkinsrelease.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import br.com.voxage.jenkinsrelease.constant.BlockType;

/**
 * 
 * @author victor.bello
 *
 */
public class BlockMessage {
    private BlockType    blockType;
    private List<String> messageItems;

    public BlockMessage(BlockType blockType) {
        this.blockType = blockType;
        this.messageItems = new ArrayList<>();
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public void addMessage(String message) {
        List<String> splitLines = Collections.emptyList();
        switch (blockType.getHtmlType()) {
            case LIST:
                splitLines = identifyBullets(message);
                break;
            case SPAN:
                splitLines = identifySpans(message);
                break;
            default:
                break;

        }
        for (String splitedMessage : splitLines) {
            String text = splitedMessage.trim().toLowerCase();
            if (StringUtils.isNotBlank(text) && messageItems.stream().noneMatch(m -> m.trim().toLowerCase().equals(text))) {
                messageItems.add(splitedMessage);
            }
        }

        if (messageItems.size() > 1) {
            messageItems.remove("N/A");
        }
    }

    private List<String> identifySpans(String message) {
        String[] split = message.split(System.lineSeparator() + "|,");
        List<String> lines = new ArrayList<>();
        for (String line : split) {
            line = line.trim();
            if (blockType == BlockType.TASK || blockType == BlockType.DEMAND) {
                if (line.matches("\\d+")) {
                    lines.add(line);
                }
            } else {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<String> identifyBullets(String message) {
        List<String> lines = new ArrayList<>();
        String[] split = message.split(System.lineSeparator());
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < split.length; i++) {
            String line = split[i].trim();
            if (StringUtils.isBlank(line)) {
                continue;
            }
            if (i != 0 && (line.startsWith("@[Mantis(") || line.startsWith("-"))) {
                String trim = sb.toString().trim();
                lines.add(trim.startsWith("-") ? trim.substring(1) : trim);
                sb = new StringBuilder();
            }
            sb.append(split[i]).append(System.lineSeparator());
        }
        String trim = sb.toString().trim();
        lines.add(trim.startsWith("-") ? trim.substring(1) : trim);
        return lines;
    }

    public List<String> getMessageItems() {
        return Collections.unmodifiableList(messageItems);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((blockType == null) ? 0 : blockType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BlockMessage other = (BlockMessage) obj;
        if (blockType != other.blockType)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BlockMessage [blockType=" + blockType + ", messageItems=" + messageItems + "]";
    }

}
