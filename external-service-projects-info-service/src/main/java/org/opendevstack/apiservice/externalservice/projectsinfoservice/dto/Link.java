package org.opendevstack.apiservice.externalservice.projectsinfoservice.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.*;
import jakarta.annotation.Generated;

/**
 * Link
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class Link {

    private String label;

    private String url;

    private String tooltip;

    private String type;

    private String abbreviation;

    private Boolean disabled;

    public Link() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public Link(String label, String url) {
        this.label = label;
        this.url = url;
    }

    public Link label(String label) {
        this.label = label;
        return this;
    }

    /**
     * Get label
     * @return label
     */
    @NotNull
    @Schema(name = "label", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Link url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get url
     * @return url
     */
    @NotNull
    @Schema(name = "url", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Link tooltip(String tooltip) {
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

    public Link type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * @return type
     */

    @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Link abbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
        return this;
    }

    /**
     * Get abbreviation
     * @return abbreviation
     */

    @Schema(name = "abbreviation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("abbreviation")
    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Link disabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Get disabled
     * @return disabled
     */

    @Schema(name = "disabled", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Link link = (Link) o;
        return Objects.equals(this.label, link.label) &&
                Objects.equals(this.url, link.url) &&
                Objects.equals(this.tooltip, link.tooltip) &&
                Objects.equals(this.type, link.type) &&
                Objects.equals(this.abbreviation, link.abbreviation) &&
                Objects.equals(this.disabled, link.disabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, url, tooltip, type, abbreviation, disabled);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Link {\n");
        sb.append("    label: ").append(toIndentedString(label)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    tooltip: ").append(toIndentedString(tooltip)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    abbreviation: ").append(toIndentedString(abbreviation)).append("\n");
        sb.append("    disabled: ").append(toIndentedString(disabled)).append("\n");
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

        private Link instance;

        public Builder() {
            this(new Link());
        }

        protected Builder(Link instance) {
            this.instance = instance;
        }

        protected Builder copyOf(Link value) {
            this.instance.setLabel(value.label);
            this.instance.setUrl(value.url);
            this.instance.setTooltip(value.tooltip);
            this.instance.setType(value.type);
            this.instance.setAbbreviation(value.abbreviation);
            this.instance.setDisabled(value.disabled);
            return this;
        }

        public Link.Builder label(String label) {
            this.instance.label(label);
            return this;
        }

        public Link.Builder url(String url) {
            this.instance.url(url);
            return this;
        }

        public Link.Builder tooltip(String tooltip) {
            this.instance.tooltip(tooltip);
            return this;
        }

        public Link.Builder type(String type) {
            this.instance.type(type);
            return this;
        }

        public Link.Builder abbreviation(String abbreviation) {
            this.instance.abbreviation(abbreviation);
            return this;
        }

        public Link.Builder disabled(Boolean disabled) {
            this.instance.disabled(disabled);
            return this;
        }

        /**
         * returns a built Link instance.
         *
         * The builder is not reusable (NullPointerException)
         */
        public Link build() {
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
    public static Link.Builder builder() {
        return new Link.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public Link.Builder toBuilder() {
        Link.Builder builder = new Link.Builder();
        return builder.copyOf(this);
    }

}

