package com.railse.hiring.workforcemgmt.model;

import com.railse.hiring.workforcemgmt.model.enums.ReferenceType;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import lombok.Data;

import java.util.List;

@Data
public class TaskManagement {
    private Long id;
    private Long referenceId;
    private ReferenceType referenceType;
    private Task task;
    private String description;
    private TaskStatus status;
    private Long assigneeId; // Simplified from Entity for this assignment
    private Long taskDeadlineTime;
    private Priority priority;
    private List<Activity> activityList;
    private List<Comment> commentList;
}

