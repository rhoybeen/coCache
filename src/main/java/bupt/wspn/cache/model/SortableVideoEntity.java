package bupt.wspn.cache.model;

import com.sun.org.apache.regexp.internal.RE;
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
    private boolean isAccepted = false;
    private Map<String,SortableClientEntity> preferenceMap;
    private List<SortableClientEntity> preferenceList;

    //视频的偏好分数为整体的服务延迟，因此越低越好
    @Override
    public int compareTo(SortableVideoEntity o) {
        return o.preferenceScore.compareTo(this.preferenceScore);
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Video " + this.getName() + " preference list : ");
        for(SortableClientEntity sortableClientEntity : preferenceList){
            stringBuilder.append(sortableClientEntity.getClientId() + "("+ sortableClientEntity.getPreferenceScore().intValue() +")|");
        }
        return stringBuilder.toString();
    }
}
