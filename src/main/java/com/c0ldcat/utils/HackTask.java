package com.c0ldcat.utils;

import fi.iki.elonen.NanoHTTPD;


public interface HackTask {
    NanoHTTPD.Response handle(TaskHelper th);
}
