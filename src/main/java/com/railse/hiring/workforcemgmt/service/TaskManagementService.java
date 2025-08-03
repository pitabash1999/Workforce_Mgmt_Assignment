package com.railse.hiring.workforcemgmt.service;

import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.CommentOnTask;
import com.railse.hiring.workforcemgmt.model.enums.Priority;


import java.util.List;


public interface TaskManagementService {
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<TaskManagementDto> updateTasks(UpdateTaskRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
    TaskManagementDto findTaskById(Long id);
    //Feature 2
    List<TaskManagementDto> updateTaskPriority(RequestTaskPriorityUpdate request);
    List<TaskManagementDto> fetchTasksByPriority(Priority priority);
    void commentOnTask(CommentOnTask commentOnTask);
}

