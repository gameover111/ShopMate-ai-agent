package com.hsc.haiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String content = fileOperationTool.readFile("test.txt");
        System.out.println(content);
        Assertions.assertNotNull(content);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String content = fileOperationTool.writeFile("test.txt", "这是一个测试文件");
        System.out.println(content);
        Assertions.assertNotNull(content);
    }
}