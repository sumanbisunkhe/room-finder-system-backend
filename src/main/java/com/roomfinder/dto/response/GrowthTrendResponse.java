package com.roomfinder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrowthTrendResponse {
    private String period;
    private Long userCount;
    private String periodLabel;
}