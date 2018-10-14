package bupt.wspn.cache.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Video implements Comparable<Video>{
    public String name;
    public int clickNum;

    @Override
    public int compareTo(Video o) {
        return this.clickNum - o.clickNum;
    }
}
