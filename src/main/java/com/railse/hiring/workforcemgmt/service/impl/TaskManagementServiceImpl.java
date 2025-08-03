package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.Activity;
import com.railse.hiring.workforcemgmt.model.ActivityOnTask;
import com.railse.hiring.workforcemgmt.model.CommentOnTask;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {


    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;


    public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }


    /*
    *Feature 3,goal 3
    * When a user fetches the details for a single task,
    * the API response must include its complete activity
    * history and all user comments, sorted
    *
    * */
    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setActivityList(taskRepository.findActivityByTaskId(id)
                .stream()
                .sorted((a,b)->sortBasedOnDate(a.getLocalDateTime(),b.getLocalDateTime()))
                .toList());
        task.setCommentList(taskRepository.findCommentsByTaskId(id)
                .stream()
                .sorted((a,b)->sortBasedOnDate(a.getLocalDateTime(),b.getLocalDateTime()))
                .toList());
        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> updateTaskPriority(RequestTaskPriorityUpdate request) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (RequestTaskPriorityUpdate.UpdatePriority item : request.getUpdateProrityList()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));



            task.setPriority(item.getPriority());

            log.info("User {} changed the priority to {} of task_id {}",request.getUserName(),item.getPriority()
            ,item.getTaskId());
            taskRepository.saveTask(
                    new ActivityOnTask(
                            item.getTaskId(),
                            new Activity(LocalDateTime.now(),
                                    request.getUserName(),
                                    String.format("User %s changed the priority to %s of task_id %s.",
                                            request.getUserName(),
                                            item.getPriority(),
                                            item.getTaskId()
                                    ))
                    )
            );
            TaskManagement savedTask=taskRepository.save(task);
            savedTask.setActivityList(taskRepository.findActivityByTaskId(savedTask.getId())
                    .stream()
                    .sorted((a,b)->sortBasedOnDate(a.getLocalDateTime(),b.getLocalDateTime()))
                    .toList());
            updatedTasks.add(savedTask);
        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }
    //feature 2 is implemented here i.e. fetch all tasks of a specific priority
    @Override
    public List<TaskManagementDto> fetchTasksByPriority(Priority priority) {

        return taskRepository.findAll()
                .stream()
                .filter(taskManagement ->taskManagement.getPriority().equals(priority))
                .map(taskMapper::modelToDto)
                .toList();
    }


    /*
    *
    * Feature 3 -> goal 2
    * Allow users to add free-text comments to a task.
     * */
    @Override
    public void commentOnTask(CommentOnTask commentOnTask) {
        commentOnTask.getComments().setLocalDateTime(LocalDateTime.now());
        taskRepository.saveCommentByTaskId(commentOnTask);
    }


    //feature 3 implemented here i.e. any update on task will be logged on console
    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");
            TaskManagement savedTask=taskRepository.save(newTask);
            log.info("User {} created this task_id {}({}).",createRequest.getUserName(),savedTask.getId(),savedTask.getTask());
            taskRepository.saveTask(
                    new ActivityOnTask(
                            savedTask.getId(),
                            new Activity(LocalDateTime.now(),
                                    createRequest.getUserName(),
                                    String.format("User %s created the task_id %s.",
                                            createRequest.getUserName(),
                                            savedTask.getId()
                                    ))
                    )
            );
            newTask.setActivityList(taskRepository.findActivityByTaskId(savedTask.getId()));
            createdTasks.add(savedTask);
        }
        return taskMapper.modelListToDtoList(createdTasks);
    }

    private int sortBasedOnDate(LocalDateTime a, LocalDateTime b){

        if(a.isAfter(b))return 1;
        if(a.isBefore(b))return -1;
        return 0;
    }

    //feature 3 implemented here i.e. any update on task will be logged on console
    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));


            if (item.getTaskStatus() != null) {
                task.setStatus(item.getTaskStatus());
            }
            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }

            log.info("User {} updated the task_id {}",updateRequest.getUserName(),item.getTaskId());
            taskRepository.saveTask(
                    new ActivityOnTask(
                            item.getTaskId(),
                            new Activity(LocalDateTime.now(),
                                    updateRequest.getUserName(),
                                    String.format("User %s updated the task_id %s.",
                                            updateRequest.getUserName(),
                                            item.getTaskId()
                                    ))
                    )
            );
            TaskManagement savedTask=taskRepository.save(task);
            savedTask.setActivityList(taskRepository.findActivityByTaskId(savedTask.getId())
                    .stream()
                    .sorted((a,b)->sortBasedOnDate(a.getLocalDateTime(),b.getLocalDateTime()))
                    .toList());
            updatedTasks.add(savedTask);
        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }


    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
        List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(request.getReferenceId(), request.getReferenceType());


        for (Task taskType : applicableTasks) {
            List<TaskManagement> tasksOfType = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED)
                    .toList();


            // BUG #1 is here. It should assign one and cancel the rest.
            // Instead, it reassigns ALL of them.
            if (!tasksOfType.isEmpty()) {
                //Fix->1
                // Reassign the first task
                TaskManagement taskToAssign = tasksOfType.get(0);
                taskToAssign.setAssigneeId(request.getAssigneeId());
                taskToAssign.setStatus(TaskStatus.ASSIGNED);
                log.info("User {} assigned the task_id {}({}) to assigned_id {}.",request.getUserName(),taskToAssign.getId(),taskToAssign.getTask(),request.getAssigneeId());

                taskRepository.save(taskToAssign);

                // Cancel the rest
                for (int i = 1; i < tasksOfType.size(); i++) {
                    TaskManagement taskToCancel = tasksOfType.get(i);
                    taskToCancel.setStatus(TaskStatus.CANCELLED);
                    taskRepository.save(taskToCancel);
                }
            } else {
                // Create a new task if none exist
                TaskManagement newTask = new TaskManagement();
                newTask.setReferenceId(request.getReferenceId());
                newTask.setReferenceType(request.getReferenceType());
                newTask.setTask(taskType);
                newTask.setAssigneeId(request.getAssigneeId());
                newTask.setStatus(TaskStatus.ASSIGNED);
                taskRepository.save(newTask);
            }
        }
        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }

    /*
    Here bug 2 and feature 1 (All active tasks that started within that range
    and all active tasks that started before the range but are still open and not yet completed.) implemented
     */
    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        LocalDateTime startDate = Instant.ofEpochMilli(request.getStartDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime endDate = Instant.ofEpochMilli(request.getEndDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // BUG #2 is here. It should filter out CANCELLED tasks but doesn't.
        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> {
                    // This logic is incomplete for the assignment.
                    // It should check against startDate and endDate.
                    // For now, it just returns all tasks for the assignees.
                    //return true;
                    // Exclude cancelled or completed tasks
                    if (task.getStatus() == TaskStatus.CANCELLED || task.getStatus() == TaskStatus.COMPLETED) {
                        return false;
                    };//Fixed->2

                    LocalDateTime taskEndDate=Instant.ofEpochMilli(task.getTaskDeadlineTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    return taskEndDate.isBefore(endDate);
                })
                .collect(Collectors.toList());


        return taskMapper.modelListToDtoList(filteredTasks);
    }
}
