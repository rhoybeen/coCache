package bupt.wspn.cache.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Edge {
    public double delay;
    public EdgeType type;
}
