package com.gymcrm.workload.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class YearSummaryDto {
    private int year;
    private List<MonthSummaryDto> months = new ArrayList<>();
}