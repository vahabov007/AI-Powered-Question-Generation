package com.vahabvahabov.AI_Powered_Question_Generation_Module.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class HuggingFaceRequest {
    private String inputs;
    private Parameters parameters;

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Parameters {

        @JsonProperty("max_new_tokens")
        private Integer maxNewTokens;

        private Double temperature;

        @JsonProperty("return_full_text")
        private boolean returnFullText;

        @JsonProperty("do_sample")
        private boolean doSample;

        @JsonProperty("top_k")
        private Integer topK;

        @JsonProperty("top_p")
        private Double topP;
    }
}
