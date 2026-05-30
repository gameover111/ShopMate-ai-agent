package com.hsc.haiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间日期获取工具
 */
public class TimeOperationTool {

    @Tool(description = "Get the current system date and time. Crucial when the user asks about today, yesterday, tomorrow, or requires time-based calculations.")
    public String getCurrentDateTime(
            @ToolParam(description = "Optional date-time format pattern, e.g., 'yyyy-MM-dd HH:mm:ss EEEE'. Leave blank for default format.") String formatPattern) {

        LocalDateTime now = LocalDateTime.now();
        if (formatPattern == null || formatPattern.isBlank()) {
            formatPattern = "yyyy-MM-dd HH:mm:ss EEEE";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return now.format(formatter);
        } catch (Exception e) {
            return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " (Invalid pattern format, used default)";
        }
    }
}