package com.zachery.windowscalendarenhanced;

import java.time.LocalDateTime;

public record AlarmRecord(LocalDateTime time, String title, String desc) {}
