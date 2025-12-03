package com.joaopssouza.fifosystem.dto;

public record UserDTO(
    Long id,
    String fullName,
    String username,
    String sector,
    String roleName,
    Long roleId
) {}