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
 * ProjectPlatforms
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class ProjectPlatforms {

    @Valid
    private List<@Valid Section> sections = new ArrayList<>();

    public ProjectPlatforms sections(List<@Valid Section> sections) {
        this.sections = sections;
        return this;
    }

    public ProjectPlatforms addSectionsItem(Section sectionsItem) {
        if (this.sections == null) {
            this.sections = new ArrayList<>();
        }
        this.sections.add(sectionsItem);
        return this;
    }

    /**
     * Get sections
     * @return sections
     */
    @Valid
    @Schema(name = "sections", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("sections")
    public List<@Valid Section> getSections() {
        return sections;
    }

    public void setSections(List<@Valid Section> sections) {
        this.sections = sections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectPlatforms projectPlatforms = (ProjectPlatforms) o;
        return Objects.equals(this.sections, projectPlatforms.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sections);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProjectPlatforms {\n");
        sb.append("    sections: ").append(toIndentedString(sections)).append("\n");
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

        private ProjectPlatforms instance;

        public Builder() {
            this(new ProjectPlatforms());
        }

        protected Builder(ProjectPlatforms instance) {
            this.instance = instance;
        }

        protected Builder copyOf(ProjectPlatforms value) {
            this.instance.setSections(value.sections);
            return this;
        }

        public ProjectPlatforms.Builder sections(List<@Valid Section> sections) {
            this.instance.sections(sections);
            return this;
        }

        /**
         * returns a built ProjectPlatforms instance.
         *
         * The builder is not reusable (NullPointerException)
         */
        public ProjectPlatforms build() {
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
    public static ProjectPlatforms.Builder builder() {
        return new ProjectPlatforms.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ProjectPlatforms.Builder toBuilder() {
        ProjectPlatforms.Builder builder = new ProjectPlatforms.Builder();
        return builder.copyOf(this);
    }

}

