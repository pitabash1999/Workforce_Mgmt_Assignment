package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.model.*;
import com.railse.hiring.workforcemgmt.model.enums.Priority;



import java.util.List;
import java.util.Optional;


public interface TaskRepository {
    Optional<TaskManagement> findById(Long id);
    TaskManagement save(TaskManagement task);
    List<TaskManagement> findAll();
    List<TaskManagement> findByReferenceIdAndReferenceType(Long referenceId, com.railse.hiring.workforcemgmt.model.enums.ReferenceType referenceType);
    List<TaskManagement> findByAssigneeIdIn(List<Long> assigneeIds);
    void saveTask(ActivityOnTask activityOnTask);
    List<Activity> findActivityByTaskId(Long taskId);
    void saveCommentByTaskId(CommentOnTask commentOnTask);
    List<Comment> findCommentsByTaskId(Long taskId);
}
