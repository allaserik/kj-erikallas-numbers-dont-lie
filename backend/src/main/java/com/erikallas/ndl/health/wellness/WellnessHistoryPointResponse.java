package com.erikallas.ndl.health.wellness;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * Weekly wellness trend point for charting wellness evolution.
 */
public class WellnessHistoryPointResponse {

    @JsonProperty("week_start")
    private LocalDate weekStart;

    @JsonProperty("week_end")
    private LocalDate weekEnd;

    @JsonProperty("score")
    private Integer score;

    public WellnessHistoryPointResponse() {
    }

    public WellnessHistoryPointResponse(LocalDate weekStart, LocalDate weekEnd, Integer score) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.score = score;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
