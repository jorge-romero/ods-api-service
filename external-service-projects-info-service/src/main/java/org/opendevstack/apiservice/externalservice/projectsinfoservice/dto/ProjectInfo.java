package org.opendevstack.apiservice.externalservice.projectsinfoservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ProjectInfo
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class ProjectInfo {

  private String projectKey;

  @Valid
  private List<String> clusters = new ArrayList<>();

  public ProjectInfo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ProjectInfo(String projectKey, List<String> clusters) {
    this.projectKey = projectKey;
    this.clusters = clusters;
  }

  public ProjectInfo projectKey(String projectKey) {
    this.projectKey = projectKey;
    return this;
  }

  /**
   * Project KEY.
   * @return projectKey
   */
  @NotNull 
  @Schema(name = "projectKey", description = "Project KEY.", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("projectKey")
  public String getProjectKey() {
    return projectKey;
  }

  public void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }

  public ProjectInfo clusters(List<String> clusters) {
    this.clusters = clusters;
    return this;
  }

  public ProjectInfo addClustersItem(String clustersItem) {
    if (this.clusters == null) {
      this.clusters = new ArrayList<>();
    }
    this.clusters.add(clustersItem);
    return this;
  }

  /**
   * List of clusters associated to the project.
   * @return clusters
   */
  @NotNull 
  @Schema(name = "clusters", description = "List of clusters associated to the project.", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clusters")
  public List<String> getClusters() {
    return clusters;
  }

  public void setClusters(List<String> clusters) {
    this.clusters = clusters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectInfo projectInfo = (ProjectInfo) o;
    return Objects.equals(this.projectKey, projectInfo.projectKey) &&
        Objects.equals(this.clusters, projectInfo.clusters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectKey, clusters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProjectInfo {\n");
    sb.append("    projectKey: ").append(toIndentedString(projectKey)).append("\n");
    sb.append("    clusters: ").append(toIndentedString(clusters)).append("\n");
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

    private ProjectInfo instance;

    public Builder() {
      this(new ProjectInfo());
    }

    protected Builder(ProjectInfo instance) {
      this.instance = instance;
    }

    protected Builder copyOf(ProjectInfo value) { 
      this.instance.setProjectKey(value.projectKey);
      this.instance.setClusters(value.clusters);
      return this;
    }

    public Builder projectKey(String projectKey) {
      this.instance.projectKey(projectKey);
      return this;
    }
    
    public Builder clusters(List<String> clusters) {
      this.instance.clusters(clusters);
      return this;
    }
    
    /**
    * returns a built ProjectInfo instance.
    *
    * The builder is not reusable (NullPointerException)
    */
    public ProjectInfo build() {
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
  public static Builder builder() {
    return new Builder();
  }

  /**
  * Create a builder with a shallow copy of this instance.
  */
  public Builder toBuilder() {
    Builder builder = new Builder();
    return builder.copyOf(this);
  }

}

