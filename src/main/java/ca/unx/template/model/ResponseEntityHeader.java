package ca.unx.template.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseEntityHeader {
    private Boolean isSuccess;
    private Boolean isRetryable;
    private String errorMessage;

    public JSONObject toJSONObject() {
        return (JSONObject) JSONObject.toJSON(this);
    }
}
