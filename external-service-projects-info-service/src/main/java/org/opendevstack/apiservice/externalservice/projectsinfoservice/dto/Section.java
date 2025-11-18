package org.opendevstack.apiservice.externalservice.projectsinfoservice.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Section
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class Section {

    private String section;

    private String tooltip;

    @Valid
    private List<@Valid Link> links = new ArrayList<>();

    public Section() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public Section(String section, List<@Valid Link> links) {
        this.section = section;
        this.links = links;
    }

    public Section section(String section) {
        this.section = section;
        return this;
    }

    /**
     * Get section
     * @return section
     */
    @NotNull
    @Schema(name = "section", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("section")
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Section tooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    /**
     * Get tooltip
     * @return tooltip
     */

    @Schema(name = "tooltip", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("tooltip")
    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public Section links(List<@Valid Link> links) {
        this.links = links;
        return this;
    }

    public Section addLinksItem(Link linksItem) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(linksItem);
        return this;
    }

    /**
     * Get links
     * @return links
     */
    @NotNull @Valid
    @Schema(name = "links", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("links")
    public List<@Valid Link> getLinks() {
        return links;
    }

    public void setLinks(List<@Valid Link> links) {
        this.links = links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Section section = (Section) o;
        return Objects.equals(this.section, section.section) &&
                Objects.equals(this.tooltip, section.tooltip) &&
                Objects.equals(this.links, section.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, tooltip, links);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Section {\n");
        sb.append("    section: ").append(toIndentedString(section)).append("\n");
        sb.append("    tooltip: ").append(toIndentedString(tooltip)).append("\n");
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    public static class Builder {

        private Section instance;

        public Builder() {
            this(new Section());
        }

        protected Builder(Section instance) {
            this.instance = instance;
        }

        protected Builder copyOf(Section value) {
            this.instance.setSection(value.section);
            this.instance.setTooltip(value.tooltip);
            this.instance.setLinks(value.links);
            return this;
        }

        public Section.Builder section(String section) {
            this.instance.section(section);
            return this;
        }

        public Section.Builder tooltip(String tooltip) {
            this.instance.tooltip(tooltip);
            return this;
        }

        public Section.Builder links(List<@Valid Link> links) {
            this.instance.links(links);
            return this;
        }

        /**
         * returns a built Section instance.
         *
         * The builder is not reusable (NullPointerException)
         */
        public Section build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field (except for the default values).
     */
    public static Section.Builder builder() {
        return new Section.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public Section.Builder toBuilder() {
        Section.Builder builder = new Section.Builder();
        return builder.copyOf(this);
    }

}

