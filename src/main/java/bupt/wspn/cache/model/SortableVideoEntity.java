package bupt.wspn.cache.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class SortableVideoEntity implements Comparable<SortableVideoEntity>{
    private String name;
    private Double preferenceScore;
    private boolean isAccepted;
    private Map<String,SortableClientEntity> preferenceMap;
    private List<SortableClientEntity> preferenceList;

    @Override
    public int compareTo(SortableVideoEntity o) {
        return o.preferenceScore.compareTo(this.preferenceScore);
    }
}
