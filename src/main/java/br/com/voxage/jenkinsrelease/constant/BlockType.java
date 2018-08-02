package br.com.voxage.jenkinsrelease.constant;

import java.text.MessageFormat;

/**
 * 
 * @author victor.bello
 *
 */

public enum BlockType {

    SUMMARY("@[Resumo]", Html.LIST, false),
    DEV("@[Desenvolvedores]", Html.SPAN, false),
    FIX("@[Corrigido]", Html.LIST, true),
    NEW("@[Novo]", Html.LIST, true),
    UPDATE("@[Alterado]", Html.LIST, true),
    INFO("@[Informações técnicas]", Html.LIST, true),
    DEMAND("@[Demandas]", Html.SPAN, true),
    TASK("@[Tarefa]", Html.SPAN, true),
    OLD("@[Mauro]", Html.LIST, false),;

    private String  blockName;
    private Html    htmlType;
    private boolean automatic;

    private BlockType(String blockName, Html htmlType, boolean automatic) {
        this.blockName = blockName;
        this.htmlType = htmlType;
        this.automatic = automatic;
    }

    public String getBlockName() {
        return blockName;
    }

    public Html getHtmlType() {
        return htmlType;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public enum Html {
        LIST("<ul><li>{0}</li></ul>"),
        SPAN("<span class=\"multi-item-line pipe-border\">{0}</span>"),
        LINK("<a href=\"{0}\" target=\"_blank\" >{1}</a>");

        private String template;

        private Html(String template) {
            this.template = template;
        }

        public String asHtml(Object... message) {
            return MessageFormat.format(template, message);
        }

    }

}