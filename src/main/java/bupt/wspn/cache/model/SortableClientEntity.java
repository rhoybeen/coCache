package bupt.wspn.cache.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Setter
@Getter
@AllArgsConstructor
public class SortableClientEntity implements Comparable<SortableClientEntity>{
    private String clientId;
    private int capacity;
    private Double preferenceScore;
    private Map<String,SortableVideoEntity> preferenceMap;
    private List<SortableVideoEntity> acceptList;

    //缓存节点的偏好分数应该为视频的点击量，越高越好
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
        log.info(clientId + " try accept " + videoEntity.getName() + " accept list size:" + acceptList.size() + " removed :" + removed);
        return removed;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.clientId);
        stringBuilder.append(" :");
        for(SortableVideoEntity sortableVideoEntity : preferenceMap.values()){
            stringBuilder.append(sortableVideoEntity.getName() + "(" + sortableVideoEntity.getPreferenceScore().intValue() + ")|");
        }
        stringBuilder.append("Accepted list:");
        for(SortableVideoEntity sortableVideoEntity : acceptList){
            stringBuilder.append(sortableVideoEntity.getName()+",");
        }
        return stringBuilder.toString();
    }

    public String getAcceptListString(){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(clientId + " accept list : ");
        for (SortableVideoEntity sortableVideoEntity : acceptList){
            stringBuilder.append(sortableVideoEntity.getName() + " (" + sortableVideoEntity.getPreferenceScore().intValue() + ")");
        }
        return stringBuilder.toString();
    }
}
