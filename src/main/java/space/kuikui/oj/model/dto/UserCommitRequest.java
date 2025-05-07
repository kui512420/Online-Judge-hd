package space.kuikui.oj.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCommitRequest {
    private String commitCount;
    private String commitPassCount;
}
