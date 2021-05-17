package uz.pdp.appclickup.payload;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RoleDTO {
    @NotNull
    private String name;
    @NotNull
    private Integer id;
}
