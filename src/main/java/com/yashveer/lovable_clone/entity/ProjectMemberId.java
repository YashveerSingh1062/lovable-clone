package com.yashveer.lovable_clone.entity;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ProjectMemberId {
    Long projectId;
    Long userId;
}
