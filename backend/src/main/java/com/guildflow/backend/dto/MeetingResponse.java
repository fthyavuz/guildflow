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
    private String mentorName;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private boolean recurring;
    private String recurrenceGroupId;
    private Long roomId;
    private String roomTitle;
    private Long roomBookingId;

    public static MeetingResponse fromEntity(Meeting meeting) {
        String mentorName = null;
        if (meeting.getMentorClass().getMentor() != null) {
            mentorName = meeting.getMentorClass().getMentor().getFirstName()
                    + " " + meeting.getMentorClass().getMentor().getLastName();
        }
        return MeetingResponse.builder()
                .id(meeting.getId())
                .classId(meeting.getMentorClass().getId())
                .className(meeting.getMentorClass().getName())
                .mentorName(mentorName)
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .location(meeting.getLocation())
                .recurring(meeting.getIsRecurring())
                .recurrenceGroupId(meeting.getRecurrenceGroupId())
                .roomId(meeting.getRoom() != null ? meeting.getRoom().getId() : null)
                .roomTitle(meeting.getRoom() != null ? meeting.getRoom().getTitle() : null)
                .roomBookingId(meeting.getRoomBooking() != null ? meeting.getRoomBooking().getId() : null)
                .build();
    }
}
