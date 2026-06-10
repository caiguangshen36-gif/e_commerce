package com.e_commerce.module.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleMenuAssignDto {
    private Long roleId;
    private List<Long> menuIds;
}