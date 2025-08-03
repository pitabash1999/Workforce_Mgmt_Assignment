package com.railse.hiring.workforcemgmt.controller;

import com.railse.hiring.workforcemgmt.model.CommentOnTask;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.response.Response;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/task-mgmt")
public class TaskManagementController {


    private final TaskManagementService taskManagementService;


    public TaskManagementController(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }


    @GetMapping("/{id}")
    public Response<TaskManagementDto> getTaskById(@PathVariable Long id) {
        return new Response<>(taskManagementService.findTaskById(id));
    }

    //Feature 2 (2nd goal)-> fetch tasks by priority
    @GetMapping("/tasks/priority/{priority}")
    public Response<List<TaskManagementDto>> getTaskByPriority(@PathVariable Priority priority) {
        return new Response<>(taskManagementService.fetchTasksByPriority(priority));
    }




    @PostMapping("/create")
    public Response<List<TaskManagementDto>> createTasks(@RequestBody TaskCreateRequest request) {
        return new Response<>(taskManagementService.createTasks(request));
    }


    @PostMapping("/update")
    public Response<List<TaskManagementDto>> updateTasks(@RequestBody UpdateTaskRequest request) {
        return new Response<>(taskManagementService.updateTasks(request));
    }


    @PostMapping("/assign-by-ref")
    public Response<String> assignByReference(@RequestBody AssignByReferenceRequest request) {
        return new Response<>(taskManagementService.assignByReference(request));
    }


    @PostMapping("/fetch-by-date/v2")
    public Response<List<TaskManagementDto>> fetchByDate(@RequestBody TaskFetchByDateRequest request) {
        return new Response<>(taskManagementService.fetchTasksByDate(request));
    }

    //Feature 2 (1st goal)-> update priority
    @PostMapping("/updatePriority")
    public Response<List<TaskManagementDto>> updateTasksPriority(@RequestBody RequestTaskPriorityUpdate request) {
        return new Response<>(taskManagementService.updateTaskPriority(request));
    }

    @PostMapping("/comment")
    public ResponseEntity<?> commentOnTak(@RequestBody CommentOnTask commentOnTask){

        try{
            taskManagementService.commentOnTask(commentOnTask);
            return new ResponseEntity<>("Comment added.",HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Unable to add comment "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}

