package ca.unx.template.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Node {
    public String label;
    public String id;
    public NodeType type;
    public String ip;
    public String parentId;
}
