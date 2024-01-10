package com.heima.model.wemedia.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description UporDownDto
 * @Author Zhilin
 * @Date 2023-11-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UporDownDto implements Serializable {
    private
    Long id;
    private Short Enable;
}
