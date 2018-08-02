package br.com.voxage.jenkinsrelease.constant;

import java.util.regex.Pattern;

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
    DEMAND("@[Demandas]", HtmlType.SPAN, "\\d*", true),
    TASK("@[Tarefa]", HtmlType.SPAN, "\\d*", true),
    OLD("@[Mauro]", HtmlType.LIST, false),;

    private String   blockName;
    private HtmlType htmlType;
    private boolean  automatic;
    private Pattern  regexValidation;

    private BlockType(String blockName, HtmlType htmlType, boolean automatic) {
        this(blockName, htmlType, ".*", automatic);
    }

    private BlockType(String blockName, HtmlType htmlType, String regex, boolean automatic) {
        this.blockName = blockName;
        this.htmlType = htmlType;
        this.automatic = automatic;
        this.regexValidation = Pattern.compile(regex, Pattern.DOTALL);
    }

    public String getBlockName() {
        return blockName;
    }

    public HtmlType getHtmlType() {
        return htmlType;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public Pattern getRegexValidation() {
        return regexValidation;
    }

    public enum HtmlType {
        LIST,
        SPAN
    }

}