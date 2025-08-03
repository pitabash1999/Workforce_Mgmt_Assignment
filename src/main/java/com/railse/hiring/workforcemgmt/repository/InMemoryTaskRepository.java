package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.model.*;
import com.railse.hiring.workforcemgmt.model.enums.ReferenceType;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Repository
public class InMemoryTaskRepository implements TaskRepository {


    private final Map<Long, TaskManagement> taskStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);
    private final Map<Long, List<Activity>> activityOnTaskMap=new ConcurrentHashMap<>();
    private final Map<Long, List<Comment>> commentOnTaskMap=new ConcurrentHashMap<>();

    public InMemoryTaskRepository() {
        // Seed data
        createSeedTask(101L, ReferenceType.ORDER, Task.CREATE_INVOICE, 1L, TaskStatus.ASSIGNED, Priority.HIGH);
        createSeedTask(101L, ReferenceType.ORDER, Task.ARRANGE_PICKUP, 1L, TaskStatus.COMPLETED, Priority.HIGH);
        createSeedTask(102L, ReferenceType.ORDER, Task.CREATE_INVOICE, 2L, TaskStatus.ASSIGNED, Priority.MEDIUM);
        createSeedTask(201L, ReferenceType.ENTITY, Task.ASSIGN_CUSTOMER_TO_SALES_PERSON, 2L, TaskStatus.ASSIGNED, Priority.LOW);
        createSeedTask(201L, ReferenceType.ENTITY, Task.ASSIGN_CUSTOMER_TO_SALES_PERSON, 3L, TaskStatus.ASSIGNED, Priority.LOW); // Duplicate for Bug #1
        createSeedTask(103L, ReferenceType.ORDER, Task.COLLECT_PAYMENT, 1L, TaskStatus.CANCELLED, Priority.MEDIUM); // For Bug #2
    }


    private void createSeedTask(Long refId, ReferenceType refType, Task task, Long assigneeId, TaskStatus status, Priority priority) {
        long newId = idCounter.incrementAndGet();
        TaskManagement newTask = new TaskManagement();
        newTask.setId(newId);
        newTask.setReferenceId(refId);
        newTask.setReferenceType(refType);
        newTask.setTask(task);
        newTask.setAssigneeId(assigneeId);
        newTask.setStatus(status);
        newTask.setPriority(priority);
        newTask.setDescription("This is a seed task.");
        newTask.setTaskDeadlineTime(System.currentTimeMillis() + 86400000); // 1 day from now
        taskStore.put(newId, newTask);
    }


    @Override
    public Optional<TaskManagement> findById(Long id) {
        return Optional.ofNullable(taskStore.get(id));
    }


    @Override
    public TaskManagement save(TaskManagement task) {
        if (task.getId() == null) {
            task.setId(idCounter.incrementAndGet());
        }
        taskStore.put(task.getId(), task);
        return task;
    }




    @Override
    public List<TaskManagement> findAll() {
        return List.copyOf(taskStore.values());
    }


    @Override
    public List<TaskManagement> findByReferenceIdAndReferenceType(Long referenceId, ReferenceType referenceType) {
        return taskStore.values().stream()
                .filter(task -> task.getReferenceId().equals(referenceId) && task.getReferenceType().equals(referenceType))
                .collect(Collectors.toList());
    }


    @Override
    public List<TaskManagement> findByAssigneeIdIn(List<Long> assigneeIds) {
        return taskStore.values().stream()
                .filter(task -> assigneeIds.contains(task.getAssigneeId()))
                .collect(Collectors.toList());
    }



    @Override
    public void saveTask(ActivityOnTask activityOnTask) {
        if(!activityOnTaskMap.containsKey(activityOnTask.getTaskId())){
            activityOnTaskMap.put(activityOnTask.getTaskId(),new ArrayList<>());
            activityOnTaskMap.get(activityOnTask.getTaskId()).add(activityOnTask.getActivity());
            return;
        }
        activityOnTaskMap.get(activityOnTask.getTaskId()).add(activityOnTask.getActivity());
    }

    @Override
    public List<Activity> findActivityByTaskId(Long taskId) {
        if(!activityOnTaskMap.containsKey(taskId))return List.of();
        return activityOnTaskMap.get(taskId);
    }

    @Override
    public void saveCommentByTaskId(CommentOnTask commentOnTask) {
        if(!commentOnTaskMap.containsKey(commentOnTask.getTaskId())){
            commentOnTaskMap.put(commentOnTask.getTaskId(),new ArrayList<>());
            commentOnTaskMap.get(commentOnTask.getTaskId()).add(commentOnTask.getComments());
            return;
        }
        commentOnTaskMap.get(commentOnTask.getTaskId()).add(commentOnTask.getComments());
    }

    @Override
    public List<Comment> findCommentsByTaskId(Long taskId) {
        if(!commentOnTaskMap.containsKey(taskId))return List.of();
        return commentOnTaskMap.get(taskId);
    }
}
