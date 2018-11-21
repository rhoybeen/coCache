package bupt.wspn.cache.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class SortableClientEntity implements Comparable<SortableClientEntity>{
    private String clientId;
    private int capacity;
    private Double preferenceScore;
    private Map<String,SortableVideoEntity> preferenceMap;
    private List<SortableVideoEntity> acceptList;

    @Override
    public int compareTo(SortableClientEntity o) {
        return this.preferenceScore.compareTo(o.preferenceScore);
    }

    public boolean isFull(){
        return acceptList.size() >= capacity;
    }

    public String tryAccept(@NonNull SortableVideoEntity videoEntity){
        final SortableVideoEntity sortableVideoEntity = preferenceMap.get(videoEntity.getName());
        acceptList.add(sortableVideoEntity);
        Collections.sort(acceptList);
        //If client has reached its capacity limit, reject the least preferred content.
        String removed = null;
        if(acceptList.size() > capacity){
            removed = acceptList.remove(capacity).getName();
        }
        return removed;
    }
}
