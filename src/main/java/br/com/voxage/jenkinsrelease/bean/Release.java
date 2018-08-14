package br.com.voxage.jenkinsrelease.bean;

import java.util.HashSet;
import java.util.Set;

import br.com.voxage.jenkinsrelease.constant.BlockType;

/**
 * 
 * @author victor.bello
 *
 */
public class Release {
    private boolean           compatibilityBreak;

    private Set<BlockMessage> blockMessages;

    public Release() {
        blockMessages = new HashSet<>();
    }

    public boolean isCompatibilityBreak() {
        return compatibilityBreak;
    }

    public void setCompatibilityBreak(boolean compatibilityBreak) {
        this.compatibilityBreak = compatibilityBreak;
    }

    public void addBlockMessage(BlockType blockType, String message) {
        BlockMessage blockMessage = blockMessages.stream().filter(bm -> bm.getBlockType().equals(blockType)).findFirst().orElse(new BlockMessage(blockType));
        blockMessage.addMessage(message);
        blockMessages.add(blockMessage);
    }

    public boolean containsUnaddressedCommit() {
        return blockMessages.stream().anyMatch(bm -> bm.getBlockType().equals(BlockType.OLD));
    }

    public Set<BlockMessage> getBlockMessages() {
        return blockMessages;
    }

    @Override
    public String toString() {
        return "Release [compatibilityBreak=" + compatibilityBreak + ", blockMessages=" + blockMessages + "]";
    }

}
