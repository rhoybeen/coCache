package mec.cache.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ResponseEntity {
    private static String RESPONSE_PAYLOAD = "payload";
    @NonNull
    private ResponseEntityHeader header;

    private Object payload;

    public String toJSONString() {
        final JSONObject jsonObj = this.header.toJSONObject();
        jsonObj.put(RESPONSE_PAYLOAD, payload);
        return jsonObj.toString();
    }

    public static ResponseEntity successEntityWithPayload(final Object payload) {
        final ResponseEntityHeader header = ResponseEntityHeader.builder().isSuccess(true).isRetryable(null).errorMessage(null).build();
        return ResponseEntity.builder()
                .header(header)
                .payload(payload)
                .build();
    }

    public static ResponseEntity retryableFailEntity(@NonNull final String errorMessage) {
        final ResponseEntityHeader header = ResponseEntityHeader.builder().isSuccess(false).isRetryable(true)
                .errorMessage(errorMessage).build();
        return ResponseEntity.builder()
                .header(header)
                .payload(new Object())
                .build();
    }

    public static ResponseEntity nonRetryableFailEntity(@NonNull final String errorMessage) {
        final ResponseEntityHeader header = ResponseEntityHeader.builder().isSuccess(false).isRetryable(false)
                .errorMessage(errorMessage).build();
        return ResponseEntity.builder()
                .header(header)
                .payload(new Object())
                .build();
    }
}
