package bupt.wspn.cache.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Define request entity for http request.
 */
@Data
@Builder
public class RequestEntity {

    @NonNull
    private String type;
    private Object params;

    public String toJSONString() {
        final JSONObject jsonObj = (JSONObject) JSONObject.toJSON(this);
        return jsonObj.toString();
    }
}
