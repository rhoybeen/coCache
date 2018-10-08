package mec.cache.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
public class Node {
    public String label;
    public String id;
    public NodeType type;
    public String ip;
    public String parentId;
}
