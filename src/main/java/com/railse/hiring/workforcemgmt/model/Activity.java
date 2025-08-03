package com.railse.hiring.workforcemgmt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Activity {

    private LocalDateTime localDateTime;
    private String name;
    private String activity;
}
