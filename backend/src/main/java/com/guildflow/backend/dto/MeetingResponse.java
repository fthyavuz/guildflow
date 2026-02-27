package com.guildflow.backend.dto;

import com.guildflow.backend.model.Meeting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {
    private Long id;
    private Long classId;
    private String className;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private boolean recurring;
    private String recurrenceGroupId;

    public static MeetingResponse fromEntity(Meeting meeting) {
        return MeetingResponse.builder()
                .id(meeting.getId())
                .classId(meeting.getMentorClass().getId())
                .className(meeting.getMentorClass().getName())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .location(meeting.getLocation())
                .recurring(meeting.getIsRecurring())
                .recurrenceGroupId(meeting.getRecurrenceGroupId())
                .build();
    }
}
