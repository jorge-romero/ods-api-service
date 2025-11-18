package org.opendevstack.apiservice.externalservice.uipath.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Request model for adding a queue item to UIPath Orchestrator.
 */
@Data
public class UiPathQueueItemRequest {
    @JsonProperty("itemData")
    private ItemData itemData;

    public UiPathQueueItemRequest() {}
    public UiPathQueueItemRequest(ItemData itemData) {
        this.itemData = itemData;
    }

    /**
     * Inner class representing the item data for the queue item.
     */
    @Data
    public static class ItemData {
        @JsonProperty("Name")
        private String name;
        @JsonProperty("Priority")
        private String priority = "Normal";
        @JsonProperty("SpecificContent")
        private Map<String, Object> specificContent;
        @JsonProperty("Reference")
        private String reference;
        @JsonProperty("DueDate")
        private LocalDateTime dueDate;
        @JsonProperty("DeferDate")
        private LocalDateTime deferDate;

        public ItemData() {}
        public ItemData(String name, String reference, Map<String, Object> specificContent) {
            this.name = name;
            this.reference = reference;
            this.specificContent = specificContent;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public Map<String, Object> getSpecificContent() {
            return specificContent;
        }

        public void setSpecificContent(Map<String, Object> specificContent) {
            this.specificContent = specificContent;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
        }

        public LocalDateTime getDeferDate() {
            return deferDate;
        }

        public void setDeferDate(LocalDateTime deferDate) {
            this.deferDate = deferDate;
        }
    }

    /**
     * Builder pattern for creating queue item requests.
     */
    public static class Builder {
        private final ItemData itemData = new ItemData();

        public Builder queueName(String name) {
            itemData.setName(name);
            return this;
        }

        public Builder reference(String reference) {
            itemData.setReference(reference);
            return this;
        }

        public Builder priority(String priority) {
            itemData.setPriority(priority);
            return this;
        }

        public Builder specificContent(Map<String, Object> content) {
            itemData.setSpecificContent(content);
            return this;
        }

        public Builder dueDate(LocalDateTime dueDate) {
            itemData.setDueDate(dueDate);
            return this;
        }

        public Builder deferDate(LocalDateTime deferDate) {
            itemData.setDeferDate(deferDate);
            return this;
        }

        public UiPathQueueItemRequest build() {
            return new UiPathQueueItemRequest(itemData);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
