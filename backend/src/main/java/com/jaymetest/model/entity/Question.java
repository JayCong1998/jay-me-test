package com.jaymetest.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 题目实体
 */
@Data
@TableName("question")
public class Question implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类: LYRICS | WORKS | SCREEN | KNOWLEDGE */
    private String category;

    /** 所属专辑 (NULL = 跨专辑通用) */
    private String album;

    /** 难度: EASY | MEDIUM */
    private String difficulty;

    /** 题目正文 */
    private String questionText;

    /** 选项 A */
    private String optionA;

    /** 选项 B */
    private String optionB;

    /** 选项 C */
    private String optionC;

    /** 选项 D */
    private String optionD;

    /** 正确答案: A | B | C | D */
    private String correctOption;

    /** 答案解析 */
    private String explanation;

    /** 创建时间 */
    private String createdAt;

    /** 更新时间 */
    private String updatedAt;

    /**
     * 获取选项列表（含 A. 前缀）
     */
    public List<String> getOptionsAsList() {
        return Arrays.asList(
                "A. " + optionA,
                "B. " + optionB,
                "C. " + optionC,
                "D. " + optionD
        );
    }
}
