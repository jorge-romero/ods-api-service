package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import lombok.Data;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.Link;

import java.util.Map;

/**
 * Represents a link from a specific platform associated to a project.
 */
@Data
public class PlatformSectionLink {

    private String label;
    private String url;
    private String tooltip;
    private String type;
    private String abbreviation;
    private Boolean disabled;

    public PlatformSectionLink() {
        // default constructor
    }

    public PlatformSectionLink(Link link) {
        this.label = link.getLabel();
        this.url = link.getUrl();
        this.tooltip = link.getTooltip();
        this.type = link.getType();
        this.abbreviation = link.getAbbreviation();
        this.disabled = link.getDisabled();
    }

    /**
     * Creates a ProjectPlatformSectionLink from a raw map.
     *
     * @param rawLink the raw map containing link data
     * @return a new ProjectPlatformSectionLink instance
     */
    public static PlatformSectionLink fromMap(Map<String, Object> rawLink) {
        PlatformSectionLink link = new PlatformSectionLink();

        if (rawLink.containsKey("label")) {
            link.setLabel((String) rawLink.get("label"));
        }

        if (rawLink.containsKey("url") && rawLink.get("url") != null) {
            link.setUrl((String) rawLink.get("url"));
        }

        if (rawLink.containsKey("tooltip")) {
            link.setTooltip((String) rawLink.get("tooltip"));
        }

        if (rawLink.containsKey("type")) {
            link.setType((String) rawLink.get("type"));
        }

        if (rawLink.containsKey("abbreviation")) {
            link.setAbbreviation((String) rawLink.get("abbreviation"));
        }

        if (rawLink.containsKey("disabled")) {
            link.setDisabled((Boolean) rawLink.get("disabled"));
        }

        return link;
    }
}
