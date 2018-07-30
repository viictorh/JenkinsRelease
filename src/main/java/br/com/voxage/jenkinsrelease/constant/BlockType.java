package br.com.voxage.jenkinsrelease.constant;

/**
 * 
 * @author victor.bello
 *
 */

public enum BlockType {

    SUMMARY("@[Resumo]", HtmlType.LIST, false),
    DEV("@[Desenvolvedores]", HtmlType.SPAN, false),
    FIX("@[Corrigido]", HtmlType.LIST, true),
    NEW("@[Novo]", HtmlType.LIST, true),
    UPDATE("@[Alterado]", HtmlType.LIST, true),
    INFO("@[Informações técnicas]", HtmlType.LIST, true),
    DEMAND("@[Demandas]", HtmlType.SPAN, true),
    TASK("@[Tarefa]", HtmlType.SPAN, true),
    OLD("@[Mauro]", HtmlType.LIST, false),;

    private String   blockName;
    private HtmlType htmlType;
    private boolean  automatic;

    private BlockType(String blockName, HtmlType htmlType, boolean automatic) {
        this.blockName = blockName;
        this.htmlType = htmlType;
        this.automatic = automatic;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public HtmlType getHtmlType() {
        return htmlType;
    }

    public void setHtmlType(HtmlType htmlType) {
        this.htmlType = htmlType;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public enum HtmlType {
        LIST,
        SPAN
    }

}